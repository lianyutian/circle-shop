package github.lianyutian.cshop.cart.mq.consumer.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import github.lianyutian.cshop.cart.mapper.CartMapper;
import github.lianyutian.cshop.cart.model.po.Cart;
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
 * 用户关注监听
 *
 * @author lianyutian
 * @since 2025-01-21 14:11:58
 * @version 1.0
 */
@Component
@AllArgsConstructor
@Slf4j
public class CartAsyncUpdateListener implements MessageListenerConcurrently {

  private final CartMapper cartMapper;

  @Override
  @Transactional
  public ConsumeConcurrentlyStatus consumeMessage(
      List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
    try {
      for (MessageExt messageExt : msgs) {
        String msg = new String(messageExt.getBody());
        Cart cartMessage = JsonUtil.fromJson(msg, Cart.class);
        log.info("购物车 MQ 异步更新-执行购物车持久化内容：{}", cartMessage);

        Cart cartFromDB =
            cartMapper.selectOne(
                new LambdaQueryWrapper<Cart>()
                    .eq(Cart::getUserId, cartMessage.getUserId())
                    .eq(Cart::getSkuId, cartMessage.getSkuId()));
        if (cartFromDB != null) {
          // 更新
          cartFromDB.setSkuCount(cartMessage.getSkuCount());
          cartFromDB.setSkuPrice(cartFromDB.getSkuPrice() + cartMessage.getSkuPrice());
          Integer salePrice =
              cartMessage.getSkuSalePrice() == null ? 0 : cartMessage.getSkuSalePrice();
          cartFromDB.setSkuSalePrice(salePrice + cartFromDB.getSkuSalePrice());
          cartMapper.update(
              cartFromDB, new LambdaUpdateWrapper<Cart>().eq(Cart::getId, cartFromDB.getId()));
          log.info("购物车 MQ 异步更新-更新购物车：{}, cartDO：{}", cartMessage, cartMessage);
        } else {
          // 插入
          int rows = cartMapper.insert(cartMessage);
          log.info("购物车 MQ 异步更新-插入购物车：data={}, rows={}", cartMessage, rows);
        }
      }
    } catch (Exception e) {
      // 本次消费失败，下次重新消费
      log.error("购物车 MQ 异步更新 consume error, 消费失败", e);
      return ConsumeConcurrentlyStatus.RECONSUME_LATER;
    }
    log.info("购物车 MQ 异步更新-信息消费成功, result: {}", ConsumeConcurrentlyStatus.CONSUME_SUCCESS);
    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
  }
}
