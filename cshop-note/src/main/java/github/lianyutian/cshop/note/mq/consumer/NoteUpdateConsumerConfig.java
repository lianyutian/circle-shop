package github.lianyutian.cshop.note.mq.consumer;

import github.lianyutian.cshop.common.mq.RocketMQConfig;
import github.lianyutian.cshop.note.constant.NoteRocketMQConstant;
import github.lianyutian.cshop.note.mq.consumer.listener.NoteUpdateListener;
import github.lianyutian.cshop.note.mq.producer.listener.NoteAddListener;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.remoting.RPCHook;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * 默认消费者
 *
 * @author lianyutian
 * @since 2025-01-15 10:32:28
 * @version 1.0
 */
@Component
public class NoteUpdateConsumerConfig {

  @Bean
  public DefaultMQPushConsumer noteUpdateConsumer(
      RocketMQConfig rocketMQConfig,
      NoteUpdateListener noteUpdateListener,
      NoteAddListener noteAddListener,
      RPCHook rpcHook)
      throws MQClientException {
    DefaultMQPushConsumer consumer =
        new DefaultMQPushConsumer(null, NoteRocketMQConstant.NOTE_UPDATE_PRODUCER_GROUP, rpcHook);
    consumer.setNamesrvAddr(rocketMQConfig.getNameServer());
    consumer.subscribe(NoteRocketMQConstant.NOTE_UPDATE_TOPIC, "*");
    consumer.registerMessageListener(noteUpdateListener);
    consumer.start();

    return consumer;
  }
}
