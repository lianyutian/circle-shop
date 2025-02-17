package github.lianyutian.cshop.cart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import github.lianyutian.cshop.cart.constant.CartCacheKeyConstant;
import github.lianyutian.cshop.cart.constant.CartConstant;
import github.lianyutian.cshop.cart.constant.CartMQConstant;
import github.lianyutian.cshop.cart.conver.CartSkuInfoVOToCartConverter;
import github.lianyutian.cshop.cart.enums.CartMessageType;
import github.lianyutian.cshop.cart.enums.CheckStatusEnum;
import github.lianyutian.cshop.cart.mapper.CartMapper;
import github.lianyutian.cshop.cart.model.entity.SkuInfoEntity;
import github.lianyutian.cshop.cart.model.param.CartDeleteParam;
import github.lianyutian.cshop.cart.model.param.CartUpdateParam;
import github.lianyutian.cshop.cart.model.po.Cart;
import github.lianyutian.cshop.cart.model.vo.BillingInfoVO;
import github.lianyutian.cshop.cart.model.vo.CartListVO;
import github.lianyutian.cshop.cart.model.vo.CartSkuInfoVO;
import github.lianyutian.cshop.cart.mq.message.CartDeleteMessage;
import github.lianyutian.cshop.cart.mq.producer.CartDefaultProducer;
import github.lianyutian.cshop.cart.service.CartService;
import github.lianyutian.cshop.common.enums.BizCodeEnum;
import github.lianyutian.cshop.common.exception.BizException;
import github.lianyutian.cshop.common.interceptor.LoginInterceptor;
import github.lianyutian.cshop.common.model.LoginUserInfo;
import github.lianyutian.cshop.common.redis.RedisCache;
import github.lianyutian.cshop.common.redis.RedisLock;
import github.lianyutian.cshop.common.utils.CommonUtil;
import github.lianyutian.cshop.common.utils.JsonUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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
  private final CartMapper cartMapper;
  private final RedisLock redisLock;

  /** 购物车数据变更分布式锁 */
  public static final String CART_CHANGE_LOCK_KEY = "CART_CHANGE_LOCK_KEY:";

  @Override
  public void updateCart(CartUpdateParam cartUpdateParam) {
    boolean lock = false;
    // 对当前用户加购的某个 sku 商品进行加锁，即同一个商品只能并发操作一次
    String cartChangeLockKey =
        CART_CHANGE_LOCK_KEY + cartUpdateParam.getUserId() + ":" + cartUpdateParam.getSkuId();
    try {
      // 使用阻塞加锁方式防止多次并发点击
      lock = redisLock.blockedLock(CART_CHANGE_LOCK_KEY + cartUpdateParam.getUserId());
      if (!lock) {
        throw new BizException(BizCodeEnum.CART_SKU_CHANGE_LOCK_FAIL);
      }
      // 1、构造购物车商品数据
      CartSkuInfoVO cartSkuInfoVO = buildCartSkuInfo(cartUpdateParam);

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
    } finally {
      if (lock) {
        redisLock.unlock(cartChangeLockKey);
      }
    }
  }

  @Override
  public void deleteCart(CartDeleteParam cartDeleteParam) {
    // 1、获取用户登录信息 这里还是通过 ThreadLocal 进行获取
    LoginUserInfo loginUserInfo = LoginInterceptor.USER_THREAD_LOCAL.get();

    if (CollectionUtils.isEmpty(cartDeleteParam.getSkuIdList())) {
      throw new BizException(BizCodeEnum.COMMON_PARAM_ERROR);
    }

    // 2、清空缓存
    for (Long skuId : cartDeleteParam.getSkuIdList()) {
      clearCartCache(loginUserInfo.getId(), skuId);
    }

    // 4、发送删除购物车商品消息
    sendAsyncDeleteMessage(loginUserInfo.getId(), cartDeleteParam.getSkuIdList());
  }

  @Override
  @Transactional
  public void clearCart() {
    LoginUserInfo loginUserInfo = LoginInterceptor.USER_THREAD_LOCAL.get();
    long id = loginUserInfo.getId();
    String userId = String.valueOf(loginUserInfo.getId());
    // 删除用户购物车 sku 数量缓存
    redisCache.delete(CartCacheKeyConstant.SHOPPING_CART_COUNT_PREFIX + userId);
    // 删除用户购物车 sku 扩展信息缓存
    redisCache.delete(CartCacheKeyConstant.SHOPPING_CART_EXTRA_PREFIX + userId);
    // 删除用户购物车 sku 操作时间缓存
    redisCache.delete(CartCacheKeyConstant.SHOPPING_CART_SORT_PREFIX + userId);

    cartMapper.delete(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, id));
  }

  @Override
  public CartListVO listCart() {
    LoginUserInfo loginUserInfo = LoginInterceptor.USER_THREAD_LOCAL.get();
    CartListVO cartListVOFromCache = queryFromCache(loginUserInfo.getId());
    if (cartListVOFromCache != null) {
      return cartListVOFromCache;
    }
    return queryFromDB(loginUserInfo.getId());
  }

  private CartListVO queryFromDB(long userId) {
    List<Cart> cartList =
        cartMapper.selectList(
            new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .orderByDesc(Cart::getUpdateTime));

    if (CollectionUtils.isEmpty(cartList)) {
      return null;
    }

    Set<CartSkuInfoVO> cartSkuInfoVOList =
        cartList.stream().map(cartSkuInfoVOToCartConverter::toVO).collect(Collectors.toSet());

    // 更新 Redis 缓存中的购物车商品
    updateCartCacheFromDB(userId, cartSkuInfoVOList);

    return getCartListVO(
        cartSkuInfoVOList, userId, "queryFromDB billingInfoVo:{}, skuList:{}, disabledSkuList:{}");
  }

  private void updateCartCacheFromDB(long userId, Set<CartSkuInfoVO> cartSkuInfoVOList) {
    // 数量信息 hash
    Map<String, String> cartNumsMap = new HashMap<>();
    // 扩展信息hash
    Map<String, String> cartExtrasMap = new HashMap<>();

    // 遍历购物车商品
    for (CartSkuInfoVO cartSkuInfoVo : cartSkuInfoVOList) {
      // 添加 sku 数量信息
      String skuId = String.valueOf(cartSkuInfoVo.getSkuId());
      cartNumsMap.put(skuId, String.valueOf(cartSkuInfoVo.getBuyCount()));
      // 添加 sku 扩展信息
      cartExtrasMap.put(skuId, JsonUtil.toJson(cartSkuInfoVo));

      // 添加购物车 sku 操作时间缓存
      String orderKey = CartCacheKeyConstant.SHOPPING_CART_SORT_PREFIX + userId;
      redisCache.zAdd(orderKey, skuId, cartSkuInfoVo.getUpdateTime().getTime());
      log.info("从数据库中查到购物车数据, 写入 zset 排序缓存, key: {}", orderKey);
    }

    // 添加购物车 sku 数量缓存
    String numKey = CartCacheKeyConstant.SHOPPING_CART_COUNT_PREFIX + userId;
    redisCache.hPutAll(numKey, cartNumsMap);
    log.info(
        "从数据库中查到购物车数据, 写入购物车 sku 数量缓存, key: {}, value: {}", numKey, JsonUtil.toJson(cartNumsMap));

    // 添加购物车 sku 扩展信息缓存
    String extraKey = CartCacheKeyConstant.SHOPPING_CART_EXTRA_PREFIX + userId;
    redisCache.hPutAll(extraKey, cartExtrasMap);
    log.info(
        "从数据库中查到购物车数据, 写入购物车 sku 扩展信息缓存, key: {}, value: {}",
        numKey,
        JsonUtil.toJson(cartExtrasMap));
  }

  private CartListVO getCartListVO(Set<CartSkuInfoVO> cartSkuInfoVOList, long userId, String s) {
    // 构建未失效的商品列表
    List<CartSkuInfoVO> skuList = new ArrayList<>();
    // 构建失效的商品列表
    List<CartSkuInfoVO> disabledSkuList = new ArrayList<>();

    // 拆分购物车商品列表为：失效的商品列表、未失效的商品列表
    splitCartSkuList(cartSkuInfoVOList, skuList, disabledSkuList);

    // 根据未失效的商品列表计算优惠后的结算价格
    BillingInfoVO billingInfoVo = calculateCartTotalPriceByCoupon(userId, skuList);

    log.info(s, billingInfoVo, skuList, disabledSkuList);

    // 返回购物车数据结构
    return CartListVO.builder()
        .cartSkuList(skuList)
        .disabledCartSkuList(disabledSkuList)
        .billing(billingInfoVo)
        .build();
  }

  private CartListVO queryFromCache(Long userId) {

    Set<CartSkuInfoVO> totalSkuList = getListCartFromCache(userId);
    if (CollectionUtils.isEmpty(totalSkuList)) {
      return null;
    }

    return getCartListVO(
        totalSkuList,
        userId,
        "queryCartFromCache billingInfoVo:{}, skuList:{}, disabledSkuList:{}");
  }

  /**
   * 购物车价格结算
   *
   * @param userId 用户ID
   * @param skuList 未失效的商品
   * @return 结算信息
   */
  private BillingInfoVO calculateCartTotalPriceByCoupon(Long userId, List<CartSkuInfoVO> skuList) {
    // 计算商品总价格（未减免优惠券）
    Integer totalPrice = calculateCartTotalPrice(skuList);
    // 获取优惠金额
    Integer couponAmount = getCouponAmount(userId, totalPrice);
    log.info(
        "calculateCartTotalPriceByCoupon totalPrice:{}, couponAmount:{}, skuList:{}",
        totalPrice,
        couponAmount,
        skuList);

    // 构造结算信息DTO
    return BillingInfoVO.builder()
        // 优惠后总价格
        .totalPrice(totalPrice - couponAmount)
        // 优惠价格
        .salePrice(couponAmount)
        .build();
  }

  private Integer getCouponAmount(Long userId, Integer totalPrice) {
    // 如果购物车金额为0
    if (totalPrice == 0) {
      return 0;
    }
    // TODO 调用优惠服务匹配最优优惠券，等优惠券服务构建后完善 这里先写死优惠券价格
    // 返回优惠券金额 到分
    return CommonUtil.genRandomInt(3000, 10000);
  }

  /**
   * 计算购物车中选中商品的金额，未减免优惠券
   *
   * @param skuList 购物车商品
   * @return 商品价格
   */
  private Integer calculateCartTotalPrice(List<CartSkuInfoVO> skuList) {
    // 1、获取已经选中的购物车商品列表
    List<CartSkuInfoVO> checkedSkuList =
        skuList.stream()
            .filter(cartSkuInfoVo -> Objects.nonNull(cartSkuInfoVo.getCheckStatus()))
            .filter(
                cartSkuInfoVo ->
                    cartSkuInfoVo.getCheckStatus().equals(CheckStatusEnum.CHECKED.getCode()))
            .collect(Collectors.toList());

    // 2、根据未失效的商品列表计算价格
    int totalPrice = 0;
    for (CartSkuInfoVO cartSkuInfoVo : checkedSkuList) {
      // 单个商品总价 = 单个商品价格 * 加购数量
      int price = cartSkuInfoVo.getPrice() * cartSkuInfoVo.getBuyCount();
      // 累计总价
      totalPrice += price;
    }
    log.info(
        "calculateCartTotalPrice checkedSkuList:{}, totalPrice:{}", checkedSkuList, totalPrice);
    return totalPrice;
  }

  private void splitCartSkuList(
      Set<CartSkuInfoVO> totalSkuSet,
      List<CartSkuInfoVO> skuList,
      List<CartSkuInfoVO> disabledSkuList) {
    // 遍历全部的购物车商品集合
    for (CartSkuInfoVO cartSkuInfoVo : totalSkuSet) {
      // 进行商品校验，是否可售等
      Boolean saleable = getSkuSaleableStatus(cartSkuInfoVo);
      if (saleable) {
        skuList.add(cartSkuInfoVo);
      } else {
        disabledSkuList.add(cartSkuInfoVo);
      }
    }
    log.info("splitCartSkuList skuList:{}, disabledSkuList:{}", skuList, disabledSkuList);
  }

  private Set<CartSkuInfoVO> getListCartFromCache(Long userId) {
    Set<String> skuExtraSet =
        redisCache.listZSet(CartCacheKeyConstant.SHOPPING_CART_EXTRA_PREFIX + userId);
    if (CollectionUtils.isEmpty(skuExtraSet)) {
      return null;
    }
    return skuExtraSet.stream()
        .filter(StringUtils::isNotEmpty)
        .filter(skuId -> !skuId.equals(CartConstant.EMPTY_CACHE_IDENTIFY))
        .map(extra -> JsonUtil.fromJson(extra, CartSkuInfoVO.class))
        .collect(Collectors.toSet());
  }

  /**
   * 构造购物车商品数据
   *
   * @param cartUpdateParam cartAddParam
   * @return CartSkuInfoVO
   */
  private CartSkuInfoVO buildCartSkuInfo(CartUpdateParam cartUpdateParam) {
    // 获取商品数据 TODO 待商品服务完成后完善
    SkuInfoEntity skuInfo = getSkuInfo(cartUpdateParam.getSkuId());

    // 获取用户登录信息 这里还是通过 ThreadLocal 进行获取
    // TODO 后续使用 Redis 分布式缓存替代
    LoginUserInfo loginUser = LoginInterceptor.USER_THREAD_LOCAL.get();
    // 返回构造数据
    return CartSkuInfoVO.builder()
        .skuId(cartUpdateParam.getSkuId())
        .userId(loginUser.getId())
        .title(skuInfo.getSkuName())
        .price(skuInfo.getPrice())
        .image(skuInfo.getSkuImage())
        .updateTime(skuInfo.getUpdateTime())
        .checkStatus(CheckStatusEnum.NO_CHECKED.getCode())
        .buyCount(
            cartUpdateParam.getBuyCount() != null
                ? cartUpdateParam.getBuyCount()
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
        CartMessageType.CART_UPDATE.getType(),
        JsonUtil.toJson(cart),
        CartMessageType.CART_UPDATE);
  }

  /**
   * 更新购物车请求数量为 0，删除缓存
   *
   * @param userId userId
   * @param skuId skuId
   */
  private void clearCartCache(Long userId, Long skuId) {
    String newSkuId = String.valueOf(skuId);
    // 删除购物车 sku 数量缓存
    redisCache.hDel(CartCacheKeyConstant.SHOPPING_CART_COUNT_PREFIX + userId, newSkuId);
    // 删除购物车 sku 扩展信息缓存
    redisCache.hDel(CartCacheKeyConstant.SHOPPING_CART_EXTRA_PREFIX + userId, newSkuId);
    // 删除购物车 sku 操作时间缓存
    redisCache.zRemove(CartCacheKeyConstant.SHOPPING_CART_SORT_PREFIX + userId, newSkuId);
  }

  /**
   * 发布购物车异步变更消息事件
   *
   * @param userId userId
   * @param skuIdList skuIdList
   */
  private void sendAsyncDeleteMessage(Long userId, List<Long> skuIdList) {

    // 发送消息到MQ
    log.info(
        "发送购物车 sku 删除消息到 MQ, topic: {}, userId: {}, sukIdList: {}",
        CartMQConstant.CART_ASYNC_PERSISTENCE_TOPIC,
        userId,
        JsonUtil.toJson(skuIdList));

    CartDeleteMessage cartDeleteMessage = new CartDeleteMessage();
    cartDeleteMessage.setUserId(userId);
    cartDeleteMessage.setSkuIdList(skuIdList);

    cartDefaultProducer.sendMessage(
        CartMQConstant.CART_ASYNC_PERSISTENCE_TOPIC,
        CartMessageType.CART_DELETE.getType(),
        JsonUtil.toJson(cartDeleteMessage),
        CartMessageType.CART_DELETE);
  }
}
