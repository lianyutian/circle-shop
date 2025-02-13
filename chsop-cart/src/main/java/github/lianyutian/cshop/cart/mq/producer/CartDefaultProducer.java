package github.lianyutian.cshop.cart.mq.producer;

import github.lianyutian.cshop.cart.constant.CartMQConstant;
import github.lianyutian.cshop.cart.enums.CartMessageType;
import github.lianyutian.cshop.common.exception.BizException;
import github.lianyutian.cshop.common.mq.RocketMQConfig;
import github.lianyutian.cshop.common.mq.rocketmq.DelayLevel;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.RPCHook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 购物车服务默认生产者
 *
 * @author lianyutian
 * @since 2025-01-21 14:01:46
 * @version 1.0
 */
@Component
@Slf4j
public class CartDefaultProducer {
  private final TransactionMQProducer producer;

  @Autowired
  public CartDefaultProducer(RocketMQConfig rocketMQConfig, RPCHook rpcHook) {
    // 初始化事务生产者客户端，设置对应的生产者组
    producer = new TransactionMQProducer(CartMQConstant.CART_DEFAULT_PRODUCER_GROUP, rpcHook);
    // 设置 nameserver
    producer.setNamesrvAddr(rocketMQConfig.getNameServer());
    // 启动生产者服务
    start();
  }

  /** 启动 rocketmq 生产者服务 该对象在使用之前必须要调用一次，只能初始化一次 */
  public void start() {
    try {
      this.producer.start();
    } catch (MQClientException e) {
      log.error("rocketmq producer start error", e);
    }
  }

  /** 关闭 rocketmq 生产者 */
  public void shutdown() {
    this.producer.shutdown();
  }

  /**
   * 同步发送单条消息
   *
   * @param topic 主题
   * @param tag tag
   * @param message 消息
   * @param type 类型
   */
  public void sendMessage(String topic, String tag, String message, CartMessageType type) {
    sendDelayMessage(topic, tag, message, DelayLevel.LEVEL_0, type);
  }

  /**
   * 发送单条延迟消息
   *
   * @param topic topic
   * @param tag tag
   * @param message 消息
   * @param delayTimeLevel 延迟等级
   * @param type 类型
   */
  public void sendDelayMessage(
      String topic, String tag, String message, DelayLevel delayTimeLevel, CartMessageType type) {
    Message msg = new Message(topic, tag, message.getBytes(StandardCharsets.UTF_8));
    try {
      if (delayTimeLevel.getValue() > 0) {
        msg.setDelayTimeLevel(delayTimeLevel.getValue());
      }
      SendResult send = producer.send(msg);
      if (SendStatus.SEND_OK == send.getSendStatus()) {
        log.info("发送 MQ 消息成功, type:{}, message:{}", type.getType(), message);
      } else {
        throw new BizException(send.getSendStatus().toString());
      }
    } catch (Exception e) {
      log.error("发送 MQ 消息失败：", e);
      throw new BizException("发送 MQ 消息失败");
    }
  }
}
