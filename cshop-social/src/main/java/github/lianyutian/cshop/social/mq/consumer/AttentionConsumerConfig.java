package github.lianyutian.cshop.social.mq.consumer;

import github.lianyutian.cshop.common.mq.RocketMQConfig;
import github.lianyutian.cshop.social.constant.SocialMQConstant;
import github.lianyutian.cshop.social.mq.consumer.listener.UserRelationUpdateListener;
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
public class AttentionConsumerConfig {
  @Bean
  public DefaultMQPushConsumer noteUpdateConsumer(
      RocketMQConfig rocketMQConfig,
      UserRelationUpdateListener userAttentionListener,
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
}
