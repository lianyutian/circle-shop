package github.lianyutian.cshop.cart.service;

import github.lianyutian.cshop.cart.model.param.CartDeleteParam;
import github.lianyutian.cshop.cart.model.param.CartUpdateParam;

/**
 * CartService
 *
 * @author lianyutian
 * @since 2025-02-12 15:56:30
 * @version 1.0
 */
public interface CartService {
  /**
   * 更新购物车
   *
   * @param cartUpdateParam 购物车入参
   */
  void updateCart(CartUpdateParam cartUpdateParam);

  /**
   * 删除购物车
   *
   * @param cartDeleteParam cartDeleteParam
   */
  void deleteCart(CartDeleteParam cartDeleteParam);
}
