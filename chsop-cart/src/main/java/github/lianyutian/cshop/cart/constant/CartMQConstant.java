package github.lianyutian.cshop.cart.constant;

/**
 * @author lianyutian
 * @since 2025-02-13 09:29:11
 * @version 1.0
 */
public class CartMQConstant {
  /** 购物车服务 producer group */
  public static final String CART_DEFAULT_PRODUCER_GROUP = "cart_default_producer_group";

  /** 购物车服务 consumer group */
  public static final String CART_UPDATE_CONSUMER_GROUP = "cart_update_consumer_group";

  public static final String CART_DELETE_CONSUMER_GROUP = "cart_delete_consumer_group";

  /** 购物车商品异步落库 topic */
  public static final String CART_ASYNC_PERSISTENCE_TOPIC = "cart_async_persistence_topic";
}
