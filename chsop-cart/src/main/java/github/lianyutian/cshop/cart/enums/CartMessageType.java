package github.lianyutian.cshop.cart.enums;

import lombok.Getter;

/**
 * @author lianyutian
 * @since 2025-02-13 10:31:30
 * @version 1.0
 */
@Getter
public enum CartMessageType {
  CART_ADD("CART_ADD", "购物车新增商品"),
  CART_DELETE("CART_DELETE", "购物车删除商品");

  private final String type;
  private final String message;

  CartMessageType(String type, String message) {
    this.type = type;
    this.message = message;
  }
}
