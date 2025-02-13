package github.lianyutian.cshop.cart.mq.consumer;

import github.lianyutian.cshop.cart.constant.CartMQConstant;
import github.lianyutian.cshop.cart.mq.consumer.listener.CartAsyncUpdateListener;
import github.lianyutian.cshop.common.mq.RocketMQConfig;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.remoting.RPCHook;
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
  @Bean("cartAsyncUpdateConsumer")
  public DefaultMQPushConsumer cartAsyncUpdateConsumer(
      RocketMQConfig rocketMQConfig,
      CartAsyncUpdateListener cartAsyncUpdateListener,
      RPCHook rpcHook)
      throws MQClientException {
    DefaultMQPushConsumer consumer =
        new DefaultMQPushConsumer(null, CartMQConstant.CART_DEFAULT_CONSUMER_GROUP, rpcHook);
    consumer.setNamesrvAddr(rocketMQConfig.getNameServer());
    consumer.subscribe(CartMQConstant.CART_ASYNC_PERSISTENCE_TOPIC, "*");
    consumer.registerMessageListener(cartAsyncUpdateListener);
    consumer.start();

    return consumer;
  }
}
