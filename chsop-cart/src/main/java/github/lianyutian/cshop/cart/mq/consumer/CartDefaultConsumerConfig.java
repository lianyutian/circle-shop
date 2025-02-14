package github.lianyutian.cshop.cart.mq.consumer;

import github.lianyutian.cshop.cart.constant.CartMQConstant;
import github.lianyutian.cshop.cart.enums.CartMessageType;
import github.lianyutian.cshop.cart.mq.consumer.listener.CartAsyncDeleteListener;
import github.lianyutian.cshop.cart.mq.consumer.listener.CartAsyncUpdateListener;
import github.lianyutian.cshop.common.mq.RocketMQConfig;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.remoting.RPCHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * 购物车服务消费者配置
 *
 * @author lianyutian
 * @since 2025-01-21 14:10:45
 * @version 1.0
 */
@Component
public class CartDefaultConsumerConfig {
  private static final Logger logger = LoggerFactory.getLogger(CartDefaultConsumerConfig.class);

  private DefaultMQPushConsumer createConsumer(
      RocketMQConfig rocketMQConfig,
      String consumerGroup,
      String topic,
      String tag,
      RPCHook rpcHook)
      throws MQClientException {
    DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(null, consumerGroup, rpcHook);
    consumer.setNamesrvAddr(rocketMQConfig.getNameServer());
    consumer.subscribe(topic, tag);
    return consumer;
  }

  @Bean("cartAsyncUpdateConsumer")
  public DefaultMQPushConsumer cartAsyncUpdateConsumer(
      RocketMQConfig rocketMQConfig,
      CartAsyncUpdateListener cartAsyncUpdateListener,
      RPCHook rpcHook) {
    try {
      DefaultMQPushConsumer consumer =
          createConsumer(
              rocketMQConfig,
              CartMQConstant.CART_UPDATE_CONSUMER_GROUP,
              CartMQConstant.CART_ASYNC_PERSISTENCE_TOPIC,
              CartMessageType.CART_UPDATE.getType(),
              rpcHook);
      consumer.registerMessageListener(cartAsyncUpdateListener);
      consumer.start();
      return consumer;
    } catch (MQClientException e) {
      logger.error("Failed to start cartAsyncUpdateConsumer", e);
      throw new RuntimeException("Failed to start cartAsyncUpdateConsumer", e);
    }
  }

  @Bean("cartAsyncDeleteConsumer")
  public DefaultMQPushConsumer cartAsyncDeleteConsumer(
      RocketMQConfig rocketMQConfig,
      CartAsyncDeleteListener cartAsyncDeleteListener,
      RPCHook rpcHook) {
    try {
      DefaultMQPushConsumer consumer =
          createConsumer(
              rocketMQConfig,
              CartMQConstant.CART_DELETE_CONSUMER_GROUP,
              CartMQConstant.CART_ASYNC_PERSISTENCE_TOPIC,
              CartMessageType.CART_DELETE.getType(),
              rpcHook);
      consumer.registerMessageListener(cartAsyncDeleteListener);
      consumer.start();
      return consumer;
    } catch (MQClientException e) {
      logger.error("Failed to start cartAsyncUpdateConsumer", e);
      throw new RuntimeException("Failed to start cartAsyncUpdateConsumer", e);
    }
  }
}
