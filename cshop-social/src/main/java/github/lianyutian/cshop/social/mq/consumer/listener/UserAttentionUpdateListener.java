package github.lianyutian.cshop.social.mq.consumer.listener;

import github.lianyutian.cshop.common.utils.IDUtil;
import github.lianyutian.cshop.common.utils.JsonUtil;
import github.lianyutian.cshop.social.mapper.UserAttentionMapper;
import github.lianyutian.cshop.social.model.po.UserAttention;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * 用户关注监听
 *
 * @author lianyutian
 * @since 2025-01-21 14:11:58
 * @version 1.0
 */
@Component
@AllArgsConstructor
@Slf4j
public class UserAttentionUpdateListener implements MessageListenerConcurrently {

  private final UserAttentionMapper userAttentionMapper;

  private final int BATCH_SIZE = 500;

  @Override
  @Transactional
  public ConsumeConcurrentlyStatus consumeMessage(
      List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
    try {
      Set<UserAttention> userAttentionSet =
          msgs.stream()
              .map(body -> JsonUtil.fromJson(new String(body.getBody()), UserAttention.class))
              .collect(Collectors.toSet());

      // 用户关注列表
      List<Long> userAttentionIdList = userAttentionMapper.selectAttentionIdList(userAttentionSet);

      List<UserAttention> insertUserAttentionList = new ArrayList<>();
      List<UserAttention> updateUserAttentionList = new ArrayList<>();
      userAttentionSet.forEach(
          userAttention -> {
            if (userAttentionIdList.contains(userAttention.getAttentionId())) {
              updateUserAttentionList.add(userAttention);
            } else {
              userAttention.setId(IDUtil.getId());
              insertUserAttentionList.add(userAttention);
            }
          });
      if (!CollectionUtils.isEmpty(insertUserAttentionList)) {
        batchInsertUserAttention(insertUserAttentionList);
        log.info("用户关注插入成功");
      }
      if (!CollectionUtils.isEmpty(updateUserAttentionList)) {
        batchUpdateUserAttention(updateUserAttentionList);
        log.info("用户关注更新成功");
      }
    } catch (Exception e) {
      log.error("用户关注更新失败", e);
      return ConsumeConcurrentlyStatus.RECONSUME_LATER;
    }
    log.info("用户关注消息消费完成");
    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
  }

  @Transactional
  protected void batchUpdateUserAttention(List<UserAttention> updateUserAttentionList) {
    if (CollectionUtils.isEmpty(updateUserAttentionList)) {
      return;
    }
    int totalSize = updateUserAttentionList.size();
    // 计算需要的批次数量
    int batchCount = (totalSize + BATCH_SIZE - 1) / BATCH_SIZE;

    for (int i = 0; i < batchCount; i++) {
      // 获取当前批次的起始索引和结束索引
      int startIndex = i * BATCH_SIZE;
      int endIndex = Math.min(startIndex + BATCH_SIZE, totalSize);

      // 截取子列表作为当前批次数据
      List<UserAttention> batchList = updateUserAttentionList.subList(startIndex, endIndex);

      // 执行批量插入操作
      userAttentionMapper.batchUpdate(batchList);
    }
  }

  @Transactional
  protected void batchInsertUserAttention(List<UserAttention> insertUserAttentionList) {
    if (CollectionUtils.isEmpty(insertUserAttentionList)) {
      return;
    }
    int totalSize = insertUserAttentionList.size();
    // 计算需要的批次数量
    int batchCount = (totalSize + BATCH_SIZE - 1) / BATCH_SIZE;

    for (int i = 0; i < batchCount; i++) {
      // 获取当前批次的起始索引和结束索引
      int startIndex = i * BATCH_SIZE;
      int endIndex = Math.min(startIndex + BATCH_SIZE, totalSize);

      // 截取子列表作为当前批次数据
      List<UserAttention> batchList = insertUserAttentionList.subList(startIndex, endIndex);

      // 执行批量插入操作
      userAttentionMapper.batchInsert(batchList);
    }
  }
}
