package github.lianyutian.cshop.social.mq.consumer.listener;

import github.lianyutian.cshop.common.utils.IDUtil;
import github.lianyutian.cshop.common.utils.JsonUtil;
import github.lianyutian.cshop.social.mapper.UserFollowerMapper;
import github.lianyutian.cshop.social.model.po.UserFollower;
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
 * 用户粉丝更新监听
 *
 * @author lianyutian
 * @since 2025-01-22 09:36:10
 * @version 1.0
 */
@Component
@AllArgsConstructor
@Slf4j
public class UserFollowerUpdateListener implements MessageListenerConcurrently {

  private final UserFollowerMapper userFollowerMapper;

  private final int BATCH_SIZE = 500;

  @Override
  @Transactional
  public ConsumeConcurrentlyStatus consumeMessage(
      List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
    try {
      Set<UserFollower> userFollowerSet =
          msgs.stream()
              .map(body -> JsonUtil.fromJson(new String(body.getBody()), UserFollower.class))
              .collect(Collectors.toSet());

      // 用户粉丝列表
      List<Long> userFollowerIdList = userFollowerMapper.selectFollowerIdList(userFollowerSet);

      List<UserFollower> insertUserFollowerList = new ArrayList<>();
      List<UserFollower> updateUserFollowerList = new ArrayList<>();
      userFollowerSet.forEach(
          userFollower -> {
            if (userFollowerIdList.contains(userFollower.getFollowerId())) {
              updateUserFollowerList.add(userFollower);
            } else {
              userFollower.setId(IDUtil.getId());
              insertUserFollowerList.add(userFollower);
            }
          });
      if (!CollectionUtils.isEmpty(insertUserFollowerList)) {
        batchInsertUserFollower(insertUserFollowerList);
        log.info("用户粉丝插入成功");
      }
      if (!CollectionUtils.isEmpty(updateUserFollowerList)) {
        batchUpdateUserFollower(updateUserFollowerList);
        log.info("用户粉丝更新成功");
      }

    } catch (Exception e) {
      log.error("用户粉丝更新失败", e);
      return ConsumeConcurrentlyStatus.RECONSUME_LATER;
    }
    log.info("用户粉丝更新消息消费完成");
    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
  }

  @Transactional
  protected void batchUpdateUserFollower(List<UserFollower> updateUserFollowerList) {
    if (CollectionUtils.isEmpty(updateUserFollowerList)) {
      return;
    }
    int totalSize = updateUserFollowerList.size();
    // 计算需要的批次数量
    int batchCount = (totalSize + BATCH_SIZE - 1) / BATCH_SIZE;

    for (int i = 0; i < batchCount; i++) {
      // 获取当前批次的起始索引和结束索引
      int startIndex = i * BATCH_SIZE;
      int endIndex = Math.min(startIndex + BATCH_SIZE, totalSize);

      // 截取子列表作为当前批次数据
      List<UserFollower> batchList = updateUserFollowerList.subList(startIndex, endIndex);

      // 执行批量插入操作
      userFollowerMapper.batchUpdate(batchList);
    }
  }

  @Transactional
  protected void batchInsertUserFollower(List<UserFollower> insertUserFollowerList) {
    if (CollectionUtils.isEmpty(insertUserFollowerList)) {
      return;
    }
    int totalSize = insertUserFollowerList.size();
    // 计算需要的批次数量
    int batchCount = (totalSize + BATCH_SIZE - 1) / BATCH_SIZE;

    for (int i = 0; i < batchCount; i++) {
      // 获取当前批次的起始索引和结束索引
      int startIndex = i * BATCH_SIZE;
      int endIndex = Math.min(startIndex + BATCH_SIZE, totalSize);

      // 截取子列表作为当前批次数据
      List<UserFollower> batchList = insertUserFollowerList.subList(startIndex, endIndex);

      // 执行批量插入操作
      userFollowerMapper.batchInsert(batchList);
    }
  }
}
