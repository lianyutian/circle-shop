package github.lianyutian.cshop.note.mq.consumer.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import github.lianyutian.cshop.common.redis.RedisCache;
import github.lianyutian.cshop.common.redis.RedisLock;
import github.lianyutian.cshop.common.utils.BeanUtil;
import github.lianyutian.cshop.common.utils.JsonUtil;
import github.lianyutian.cshop.note.constant.NoteCacheKeyConstant;
import github.lianyutian.cshop.note.enums.NoteStatusEnum;
import github.lianyutian.cshop.note.mapper.NoteMapper;
import github.lianyutian.cshop.note.model.po.Note;
import github.lianyutian.cshop.note.model.vo.NoteShowVO;
import github.lianyutian.cshop.note.mq.message.NoteUpdateMessage;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.stereotype.Component;

/**
 * 笔记更新消息监听器
 *
 * @author lianyutian
 * @since 2025-01-15 10:34:58
 * @version 1.0
 */
@Slf4j
@Component
@AllArgsConstructor
public class NoteUpdateListener implements MessageListenerConcurrently {
  /** 每页 20 条 */
  private static final int PAGE_SIZE = 2;

  private final RedisCache redisCache;

  private final RedisLock redisLock;

  private final NoteMapper noteMapper;

  @Override
  public ConsumeConcurrentlyStatus consumeMessage(
      List<MessageExt> noteMessageList, ConsumeConcurrentlyContext context) {

    try {
      for (MessageExt noteMessage : noteMessageList) {
        String msg = new String(noteMessage.getBody());
        Long userId;

        NoteUpdateMessage noteUpdateMessage = JsonUtil.fromJson(msg, NoteUpdateMessage.class);
        userId = noteUpdateMessage.getUserId();
        log.info("笔记 MQ 异步更新-执行博主笔记分页缓存数据异步更新，消息内容：{}", JsonUtil.toJson(noteUpdateMessage));

        // 这里增加分布式锁，同一个用户同时间只能操作一次，避免重复请求
        String noteUpdateLockKey = NoteCacheKeyConstant.NOTE_UPDATE_LOCK_KEY_PREFIX + userId;
        boolean locked = false;
        try {
          // 这里通过阻塞的方式进行加锁，跟新增/更新笔记操作互斥，并发操作时进行阻塞
          locked = redisLock.blockedLock(noteUpdateLockKey);
          updateCache(userId);
        } finally {
          if (locked) {
            redisLock.unlock(noteUpdateLockKey);
          }
        }
      }
    } catch (Exception e) {
      // 本次消费失败，下次重新消费
      log.error("笔记 MQ 异步更新 consume error, 更新博主笔记缓存数据消费失败", e);
      return ConsumeConcurrentlyStatus.RECONSUME_LATER;
    }
    log.info("笔记 MQ 异步更新-博主笔记缓存数据消费成功, result: {}", ConsumeConcurrentlyStatus.CONSUME_SUCCESS);
    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
  }

  private void updateCache(Long userId) {
    // 此时我们需要对博主的笔记列表进行分页缓存重新构建
    // 新增需要构建所有笔记分页缓存
    // 1、计算我的笔记列表总数
    String userNoteTotalKey = NoteCacheKeyConstant.NOTE_TOTAL_KEY_PREFIX + userId;
    Long size = redisCache.getLong(userNoteTotalKey);
    log.info("笔记 MQ 异步更新-key:{}, value:{}", userNoteTotalKey, size);
    // 计算总分页数
    int pageTotal = (int) Math.ceil((double) size / PAGE_SIZE) + 1;
    for (int pageNum = 1; pageNum <= pageTotal; pageNum++) {
      buildPageCache(userId, pageNum);
    }
  }

  private void buildPageCache(Long userId, int pageNum) {
    // 笔记分页缓存 key
    String notePageKey = NoteCacheKeyConstant.NOTE_SHOW_PAGE_KEY_PREFIX + userId + ":" + pageNum;
    // 先从缓存中获取一下
    String notePageCache = redisCache.get(notePageKey);
    // 如果分页缓存为空，跳过
    if (StringUtils.isBlank(notePageCache)) {
      return;
    }
    // 只有缓存中存在这一页的数据才会去数据库读取最新的笔记信息去更新
    // 查询我的未删除笔记列表
    LambdaQueryWrapper<Note> lambdaQueryWrapper = new LambdaQueryWrapper<>();
    lambdaQueryWrapper
        .eq(Note::getUserId, userId)
        .eq(Note::getStatus, NoteStatusEnum.PUBLISHED)
        .orderByDesc(Note::getUpdateTime);

    Page<Note> notePageParam = new Page<>(pageNum, PAGE_SIZE);
    Page<Note> notePage = noteMapper.selectPage(notePageParam, lambdaQueryWrapper);

    List<Note> noteList = notePage.getRecords();
    log.info(
        "笔记 MQ 异步更新-从数据库获取我的笔记列表，pageNum:{}, total:{}, size：{}, userId: {}",
        notePage.getPages(),
        notePage.getTotal(),
        notePage.getSize(),
        userId);

    List<NoteShowVO> noteShowVOList = BeanUtil.copyList(noteList, NoteShowVO.class);

    // 写入分页缓存数据
    redisCache.set(
        notePageKey, noteShowVOList, RedisCache.generateCacheExpire(), TimeUnit.MILLISECONDS);
  }
}
