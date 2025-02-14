package github.lianyutian.cshop.cart.mq.consumer.listener;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import github.lianyutian.cshop.cart.mapper.CartMapper;
import github.lianyutian.cshop.cart.model.po.Cart;
import github.lianyutian.cshop.cart.mq.message.CartDeleteMessage;
import github.lianyutian.cshop.common.utils.JsonUtil;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 购物车sku删除消息监听
 *
 * @author lianyutian
 * @since 2025-02-13 16:34:03
 * @version 1.0
 */
@Component
@AllArgsConstructor
@Slf4j
public class CartAsyncDeleteListener implements MessageListenerConcurrently {

  private final CartMapper cartMapper;

  @Override
  @Transactional
  public ConsumeConcurrentlyStatus consumeMessage(
      List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
    try {
      for (MessageExt msg : msgs) {
        String message = new String(msg.getBody());
        CartDeleteMessage cartDeleteMessage = JsonUtil.fromJson(message, CartDeleteMessage.class);

        log.info("购物车 MQ 异步删除 sku -执行购物车持久化内容：{}", cartDeleteMessage);

        cartMapper.delete(
            new LambdaUpdateWrapper<Cart>()
                .eq(Cart::getUserId, cartDeleteMessage.getUserId())
                .in(Cart::getSkuId, cartDeleteMessage.getSkuIdList()));
      }
    } catch (Exception e) {
      // 本次消费失败，下次重新消费
      log.error("购物车 MQ 异步删除 sku consume error, 消费失败", e);
      return ConsumeConcurrentlyStatus.RECONSUME_LATER;
    }
    log.info("购物车 MQ 异步删除 sku-信息消费成功, result: {}", ConsumeConcurrentlyStatus.CONSUME_SUCCESS);
    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
  }
}
