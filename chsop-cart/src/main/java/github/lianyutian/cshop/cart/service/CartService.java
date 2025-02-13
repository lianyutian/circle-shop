package github.lianyutian.cshop.cart.service;

import github.lianyutian.cshop.cart.model.param.CartAddParam;

/**
 * CartService
 *
 * @author lianyutian
 * @since 2025-02-12 15:56:30
 * @version 1.0
 */
public interface CartService {
  /**
   * 添加购物车
   *
   * @param cartAddParam 购物车入参
   */
  void addCart(CartAddParam cartAddParam);
}
