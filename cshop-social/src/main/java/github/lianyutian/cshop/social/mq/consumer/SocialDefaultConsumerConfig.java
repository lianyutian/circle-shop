package github.lianyutian.cshop.social.mq.consumer;

import github.lianyutian.cshop.common.mq.RocketMQConfig;
import github.lianyutian.cshop.social.constant.SocialMQConstant;
import github.lianyutian.cshop.social.mq.consumer.listener.UserAttentionUpdateListener;
import github.lianyutian.cshop.social.mq.consumer.listener.UserFollowerUpdateListener;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.remoting.RPCHook;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * 关注服务消费者配置
 *
 * @author lianyutian
 * @since 2025-01-21 14:10:45
 * @version 1.0
 */
@Component
public class SocialDefaultConsumerConfig {
  @Bean("attentionUpdateConsumer")
  public DefaultMQPushConsumer attentionUpdateConsumer(
      RocketMQConfig rocketMQConfig,
      UserAttentionUpdateListener userAttentionListener,
      RPCHook rpcHook)
      throws MQClientException {
    DefaultMQPushConsumer consumer =
        new DefaultMQPushConsumer(null, SocialMQConstant.ATTENTION_DEFAULT_CONSUMER_GROUP, rpcHook);
    consumer.setNamesrvAddr(rocketMQConfig.getNameServer());
    consumer.subscribe(SocialMQConstant.ATTENTION_TOPIC, "*");
    consumer.registerMessageListener(userAttentionListener);
    consumer.start();

    return consumer;
  }

  @Bean("followerUpdateConsumer")
  public DefaultMQPushConsumer followerUpdateConsumer(
      RocketMQConfig rocketMQConfig,
      UserFollowerUpdateListener userFollowerUpdateListener,
      RPCHook rpcHook)
      throws MQClientException {
    DefaultMQPushConsumer consumer =
        new DefaultMQPushConsumer(null, SocialMQConstant.FOLLOWER_DEFAULT_CONSUMER_GROUP, rpcHook);
    consumer.setNamesrvAddr(rocketMQConfig.getNameServer());
    consumer.subscribe(SocialMQConstant.FOLLOWER_TOPIC, "*");
    consumer.registerMessageListener(userFollowerUpdateListener);
    consumer.start();

    return consumer;
  }
}
