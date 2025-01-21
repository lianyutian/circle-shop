package github.lianyutian.cshop.social.mq.consumer.listener;

import github.lianyutian.cshop.common.utils.JsonUtil;
import github.lianyutian.cshop.social.mapper.UserAttentionMapper;
import github.lianyutian.cshop.social.model.po.UserAttention;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.stereotype.Component;

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
public class UserRelationUpdateListener implements MessageListenerConcurrently {

  private final UserAttentionMapper userAttentionMapper;

  @Override
  public ConsumeConcurrentlyStatus consumeMessage(
      List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
    List<UserAttention> userAttentionList =
        msgs.stream()
            .map(MessageExt::getBody)
            .map(msg -> JsonUtil.fromJson(new String(msg), UserAttention.class))
            .toList();
    try {
      userAttentionMapper.insertOrUpdate(userAttentionList);
    } catch (Exception e) {
      log.error("用户关注更新失败，消息内容：{}", JsonUtil.toJson(userAttentionList), e);
      return ConsumeConcurrentlyStatus.RECONSUME_LATER;
    }
    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
  }
}
