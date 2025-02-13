package github.lianyutian.cshop.cart.constant;

/**
 * @author lianyutian
 * @since 2025-02-12 16:10:12
 * @version 1.0
 */
public class CartCacheKeyConstant {
  /** 购物车 sku 数量 hash key */
  public static final String SHOPPING_CART_COUNT_PREFIX = "shopping_cart_count:";

  /** 购物车 sku 扩展信息 hash key */
  public static final String SHOPPING_CART_EXTRA_PREFIX = "shopping_cart_extra:";

  /** 购物车 sku 操作时间 zset key */
  public static final String SHOPPING_CART_SORT_PREFIX = "shopping_cart_sort:";

  /** 购物车商品 sku 空缓存 key */
  public static final String SHOPPING_CART_EMPTY_PREFIX = "shopping_cart_empty:";
}
