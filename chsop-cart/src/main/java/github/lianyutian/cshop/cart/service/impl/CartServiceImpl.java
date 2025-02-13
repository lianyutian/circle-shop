package github.lianyutian.cshop.cart.service.impl;

import github.lianyutian.cshop.cart.constant.CartCacheKeyConstant;
import github.lianyutian.cshop.cart.constant.CartConstant;
import github.lianyutian.cshop.cart.constant.CartMQConstant;
import github.lianyutian.cshop.cart.conver.CartSkuInfoVOToCartConverter;
import github.lianyutian.cshop.cart.enums.CartMessageType;
import github.lianyutian.cshop.cart.enums.CheckStatusEnum;
import github.lianyutian.cshop.cart.model.entity.SkuInfoEntity;
import github.lianyutian.cshop.cart.model.param.CartAddParam;
import github.lianyutian.cshop.cart.model.po.Cart;
import github.lianyutian.cshop.cart.model.vo.CartSkuInfoVO;
import github.lianyutian.cshop.cart.mq.producer.CartDefaultProducer;
import github.lianyutian.cshop.cart.service.CartService;
import github.lianyutian.cshop.common.enums.BizCodeEnum;
import github.lianyutian.cshop.common.exception.BizException;
import github.lianyutian.cshop.common.interceptor.LoginInterceptor;
import github.lianyutian.cshop.common.model.LoginUserInfo;
import github.lianyutian.cshop.common.redis.RedisCache;
import github.lianyutian.cshop.common.utils.CommonUtil;
import github.lianyutian.cshop.common.utils.JsonUtil;
import java.util.Date;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author lianyutian
 * @since 2025-02-12 15:57:51
 * @version 1.0
 */
@Service
@Slf4j
@AllArgsConstructor
public class CartServiceImpl implements CartService {

  private final RedisCache redisCache;
  private final CartDefaultProducer cartDefaultProducer;
  private final CartSkuInfoVOToCartConverter cartSkuInfoVOToCartConverter;

  @Override
  public void addCart(CartAddParam cartAddParam) {
    // 1、构造购物车商品数据
    CartSkuInfoVO cartSkuInfoVO = buildCartSkuInfo(cartAddParam);

    // 2、校验商品是否可售: 库存、上下架状态 TODO 等商品服务构建后完善
    // 购物车是否达到最大限制等
    checkSellable(cartSkuInfoVO);

    // 3、检查商品是否已经存在
    if (checkCartSkuExist(cartSkuInfoVO)) {
      // 重新计算购物车 sku 数量
      cartSkuInfoVO = recalculateCount(cartSkuInfoVO);
    }

    // 4、更新购物车 Redis 缓存
    updateCartCache(cartSkuInfoVO);
    // 5、发送更新消息到 MQ
    sendAsyncUpdateMessage(cartSkuInfoVO);
  }

  /**
   * 构造购物车商品数据
   *
   * @param cartAddParam cartAddParam
   * @return CartSkuInfoVO
   */
  private CartSkuInfoVO buildCartSkuInfo(CartAddParam cartAddParam) {
    // 获取商品数据 TODO 待商品服务完成后完善
    SkuInfoEntity skuInfo = getSkuInfo(cartAddParam.getSkuId());

    // 获取用户登录信息 这里还是通过 ThreadLocal 进行获取
    // TODO 后续使用 Redis 分布式缓存替代
    LoginUserInfo loginUser = LoginInterceptor.USER_THREAD_LOCAL.get();
    // 返回构造数据
    return CartSkuInfoVO.builder()
        .skuId(cartAddParam.getSkuId())
        .userId(loginUser.getId())
        .title(skuInfo.getSkuName())
        .price(skuInfo.getPrice())
        .image(skuInfo.getSkuImage())
        .updateTime(skuInfo.getUpdateTime())
        .checkStatus(CheckStatusEnum.NO_CHECKED.getCode())
        .buyCount(
            cartAddParam.getBuyCount() != null
                ? cartAddParam.getBuyCount()
                : CartConstant.DEFAULT_ADD_CART_SKU_COUNT)
        .build();
  }

  /**
   * 获取商品信息
   *
   * @param skuId skuId
   * @return SkuInfoEntity
   */
  private SkuInfoEntity getSkuInfo(Long skuId) {
    // TODO 等商品构建后完善
    SkuInfoEntity info = new SkuInfoEntity();
    info.setSkuId(skuId);
    info.setSkuName("小米·新品 Note13 12+256G全网通快充游戏千元原装快充旗舰手机MAX" + CommonUtil.genRandomInt(1, 90));
    info.setPrice(CommonUtil.genRandomInt(41900, 89000));
    info.setVipPrice(CommonUtil.genRandomInt(28900, 58000));
    info.setSkuStatus(1);
    info.setMainUrl(
        "https://sns-webpic-qc.xhscdn.com/202411041152/88f24f543171e8462ed80cfb08b5955d/1040g0083128dbkcs56305n6ullk5snhm8oeqefo!nc_n_webp_mw_1");
    info.setUpdateTime(new Date());
    return info;
  }

  /**
   * 校验商品是否可售
   *
   * @param cartSkuInfoVO cartSkuInfoVO
   */
  private void checkSellable(CartSkuInfoVO cartSkuInfoVO) {
    // 1、检查购物车商品数量是否达到上限
    checkCartSkuThreshold(cartSkuInfoVO.getUserId());

    // 获取商品可售状态
    Boolean saleable = getSkuSaleableStatus(cartSkuInfoVO);
    if (!saleable) {
      // 该商品目前未开放销售
      throw new BizException(BizCodeEnum.CART_SKU_SELL_STATUS_ERROR);
    }
  }

  /**
   * 检测购物车商品数量上限
   *
   * @param userId userId
   */
  private void checkCartSkuThreshold(Long userId) {
    // 这里直接从 Redis 获取当前购物车商品 sku 数量
    Long len = redisCache.getHashSize(CartCacheKeyConstant.SHOPPING_CART_COUNT_PREFIX + userId);
    // 购物车里能加入多少商品是有限制的，最大支持 100
    if (len > CartConstant.DEFAULT_CART_MAX_SKU_COUNT) {
      throw new BizException(BizCodeEnum.CART_SKU_COUNT_THRESHOLD_ERROR);
    }
  }

  /**
   * 获取商品 sku 可售状态
   *
   * @param cartSkuInfoVO cartSkuInfoVo
   * @return 可售状态
   */
  private Boolean getSkuSaleableStatus(CartSkuInfoVO cartSkuInfoVO) {
    // TODO 等商品构建后完善 这里先默认都可售
    return true;
  }

  /**
   * 检查商品是否存在
   *
   * @param cartSkuInfoVO cartSkuInfoVO
   * @return true/false
   */
  private boolean checkCartSkuExist(CartSkuInfoVO cartSkuInfoVO) {
    // 这里只需要从 Redis Hash 中查找是否存在即可
    return redisCache.isFiledExistOfHash(
        CartCacheKeyConstant.SHOPPING_CART_EXTRA_PREFIX + cartSkuInfoVO.getUserId(),
        String.valueOf(cartSkuInfoVO.getSkuId()));
  }

  /**
   * 检查加购的商品是否在购物车中存在,如果存在则商品数量 +1
   *
   * @param cartSkuInfoVO cartSkuInfoVO
   * @return CartSkuInfoVO
   */
  private CartSkuInfoVO recalculateCount(CartSkuInfoVO cartSkuInfoVO) {
    // 获取缓存中存在的 SKU 扩展信息
    String cartSkuInfoString =
        redisCache.hGet(
            CartCacheKeyConstant.SHOPPING_CART_EXTRA_PREFIX + cartSkuInfoVO.getUserId(),
            String.valueOf(cartSkuInfoVO.getSkuId()));

    CartSkuInfoVO oldCartSkuInfoVO = JsonUtil.fromJson(cartSkuInfoString, CartSkuInfoVO.class);
    if (Objects.isNull(oldCartSkuInfoVO)) {
      return cartSkuInfoVO;
    }
    log.info(
        "recalculateCount oldCartSkuInfoVo:{}, cartSkuInfoVO:{}", oldCartSkuInfoVO, cartSkuInfoVO);
    // 商品数量累加
    oldCartSkuInfoVO.setBuyCount(oldCartSkuInfoVO.getBuyCount() + cartSkuInfoVO.getBuyCount());
    return oldCartSkuInfoVO;
  }

  /**
   * 更新购物车 Redis 缓存 总共包含 3 个数据结构
   *
   * <p>1、更新购物车 sku 数量
   *
   * <p>2、更新购物车 sku 扩展信息
   *
   * <p>3、更新购物车 sku 商品排序时间
   *
   * @param cartSkuInfoVO cartSkuInfoVO
   */
  private void updateCartCache(CartSkuInfoVO cartSkuInfoVO) {
    // 更新购物车 sku 数量
    updateCartNumCache(cartSkuInfoVO);
    // 更新购物车 sku 扩展信息
    updateCartExtraCache(cartSkuInfoVO);
    // 更新购物车 sku 商品排序时间
    updateCartSortCache(cartSkuInfoVO);
  }

  /**
   * 更新购物车 sku 数量
   *
   * @param cartSkuInfoVO cartSkuInfoVO
   */
  private void updateCartNumCache(CartSkuInfoVO cartSkuInfoVO) {
    // 购物车 sku 数量 hash key
    String numKey = CartCacheKeyConstant.SHOPPING_CART_COUNT_PREFIX + cartSkuInfoVO.getUserId();
    Integer count = cartSkuInfoVO.getBuyCount();
    String field = String.valueOf(cartSkuInfoVO.getSkuId());
    // 更新缓存中的商品数量，这里使用 redis hash 数据结构
    // shopping_cart_count:{userId} -> {
    //   {skuId}: 2,
    //   {skuId}: 3,
    //   {skuId}: 5
    // }
    redisCache.hPut(numKey, field, String.valueOf(count));
    log.info("更新缓存购物车 sku 数量, key: {}, field: {}, value: {}", numKey, field, count);
  }

  /**
   * 更新购物车 sku 扩展信息
   *
   * @param cartSkuInfoVO cartSkuInfoVO
   */
  private void updateCartExtraCache(CartSkuInfoVO cartSkuInfoVO) {
    // 购物车 sku 扩展信息 hash key
    String extraKey = CartCacheKeyConstant.SHOPPING_CART_EXTRA_PREFIX + cartSkuInfoVO.getUserId();
    String field = String.valueOf(cartSkuInfoVO.getSkuId());
    // 更新缓存中的商品扩展信息，这里也使用 redis hash 数据结构
    // shopping_cart_extra_hash:{userId} {
    //  {skuId}: {skuInfo},
    //  {skuId}: {skuInfo}
    // }
    redisCache.hPut(extraKey, field, JsonUtil.toJson(cartSkuInfoVO));
    log.info(
        "更新缓存购物车 sku 扩展信息, key: {}, field: {}, value: {}",
        extraKey,
        field,
        JsonUtil.toJson(cartSkuInfoVO));
  }

  /**
   * 更新购物车 sku 商品排序时间
   *
   * @param cartSkuInfoVO cartSkuInfoVO
   */
  private void updateCartSortCache(CartSkuInfoVO cartSkuInfoVO) {
    // 购物车 sku 操作时间 zset key
    String sortKey = CartCacheKeyConstant.SHOPPING_CART_SORT_PREFIX + cartSkuInfoVO.getUserId();
    String field = String.valueOf(cartSkuInfoVO.getSkuId());
    // 把每个 skuId 和其加入购物车的时间，写入到了 zset 里面去
    // zset 结构: [{skuId -> score(当前时间)}, {skuId -> score(当前时间)}]
    redisCache.zAdd(sortKey, field);
    log.info(
        "更新缓存购物车 sku 商品排序, key: {}, field: {}, value: {}",
        sortKey,
        field,
        System.currentTimeMillis());
  }

  /**
   * 发布购物车异步变更消息事件
   *
   * @param cartSkuInfoVO cartSkuInfoVO
   */
  private void sendAsyncUpdateMessage(CartSkuInfoVO cartSkuInfoVO) {
    // 需要落库的购物车实体对象
    Cart cart = cartSkuInfoVOToCartConverter.toPO(cartSkuInfoVO);

    // 发送消息到MQ
    log.info(
        "发送购物车变更消息到MQ, topic: {}, cart: {}",
        CartMQConstant.CART_ASYNC_PERSISTENCE_TOPIC,
        JsonUtil.toJson(cart));

    cartDefaultProducer.sendMessage(
        CartMQConstant.CART_ASYNC_PERSISTENCE_TOPIC,
        CartMessageType.CART_ADD.getType(),
        JsonUtil.toJson(cart),
        CartMessageType.CART_ADD);
  }
}
