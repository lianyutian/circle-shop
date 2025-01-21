package github.lianyutian.cshop.social.mq.producer;

import github.lianyutian.cshop.common.exception.BizException;
import github.lianyutian.cshop.common.mq.RocketMQConfig;
import github.lianyutian.cshop.common.mq.rocketmq.DelayLevel;
import github.lianyutian.cshop.social.constant.SocialMQConstant;
import github.lianyutian.cshop.social.enums.SocialMessageType;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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
 * 社交服务默认生产者
 *
 * @author lianyutian
 * @since 2025-01-21 14:01:46
 * @version 1.0
 */
@Component
@Slf4j
public class SocialDefaultProducer {
  private final TransactionMQProducer producer;

  @Autowired
  public SocialDefaultProducer(RocketMQConfig rocketMQConfig, RPCHook rpcHook) {
    // 初始化事务生产者客户端，设置对应的生产者组
    producer = new TransactionMQProducer(SocialMQConstant.RELATION_DEFAULT_PRODUCER_GROUP, rpcHook);
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
   * 发送事务消息
   *
   * @param topic topic
   * @param tag tag
   * @param message 消息
   * @param arg 额外参数
   * @param type 消息类型
   */
  public void sendTransactionMessage(
      String topic, String tag, String message, Object arg, SocialMessageType type) {
    Message msg = new Message(topic, tag, message.getBytes(StandardCharsets.UTF_8));
    try {
      SendResult send = producer.sendMessageInTransaction(msg, arg);
      if (SendStatus.SEND_OK == send.getSendStatus()) {
        log.info("发送 MQ 消息成功, type:{}, message:{}, arg:{}", type.getMessage(), message, arg);
      } else {
        throw new BizException(send.getSendStatus().toString());
      }
    } catch (Exception e) {
      log.error("发送 MQ 消息失败：", e);
      throw new BizException("发送 MQ 消息失败");
    }
  }

  /**
   * 同步发送单条消息
   *
   * @param topic 主题
   * @param tag tag
   * @param message 消息
   * @param type 类型
   */
  public void sendMessage(String topic, String tag, String message, SocialMessageType type) {
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
      String topic, String tag, String message, DelayLevel delayTimeLevel, SocialMessageType type) {
    Message msg = new Message(topic, tag, message.getBytes(StandardCharsets.UTF_8));
    try {
      if (delayTimeLevel.getValue() > 0) {
        msg.setDelayTimeLevel(delayTimeLevel.getValue());
      }
      SendResult send = producer.send(msg);
      if (SendStatus.SEND_OK == send.getSendStatus()) {
        log.info("发送 MQ 消息成功, type:{}, message:{}", type.getMessage(), message);
      } else {
        throw new BizException(send.getSendStatus().toString());
      }
    } catch (Exception e) {
      log.error("发送 MQ 消息失败：", e);
      throw new BizException("发送 MQ 消息失败");
    }
  }

  /**
   * 批量发送延迟消息
   *
   * @param topic topic
   * @param tag tag
   * @param messages 多个消息
   * @param delayTimeLevel 延迟等级
   * @param type 类型
   */
  public void sendDelayMessages(
      String topic,
      String tag,
      List<String> messages,
      DelayLevel delayTimeLevel,
      SocialMessageType type) {

    List<Message> list = new ArrayList<>();
    for (String message : messages) {
      Message msg = new Message(topic, tag, message.getBytes(StandardCharsets.UTF_8));
      if (delayTimeLevel.getValue() > 0) {
        msg.setDelayTimeLevel(delayTimeLevel.getValue());
      }
      list.add(msg);
    }
    try {
      SendResult send = producer.send(list);
      if (SendStatus.SEND_OK == send.getSendStatus()) {
        log.info("发送 MQ 消息成功, type:{}", type.getMessage());
      } else {
        throw new BizException(send.getSendStatus().toString());
      }
    } catch (Exception e) {
      log.error("发送 MQ 消息失败：", e);
      throw new BizException("发送 MQ 消息失败");
    }
  }
}
