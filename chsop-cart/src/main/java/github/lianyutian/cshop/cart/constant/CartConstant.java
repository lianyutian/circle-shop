package github.lianyutian.cshop.cart.constant;

/**
 * @author lianyutian
 * @since 2025-02-12 16:06:19
 * @version 1.0
 */
public class CartConstant {
  /** 购物车加购默认数量 */
  public static final Integer DEFAULT_ADD_CART_SKU_COUNT = 1;

  /** 最大的默认购物车 sku 数量 */
  public static final Integer DEFAULT_CART_MAX_SKU_COUNT = 100;

  /** 未使用的优惠券状态 */
  public static final Integer UNUSED_COUPON_STATUS = 0;

  /** 生效的优惠券状态 */
  public static final Integer AVAILABLE_COUPON_STATUS = 0;

  /** redis空key标识 */
  public static final String EMPTY_CACHE_IDENTIFY = "$";
}
