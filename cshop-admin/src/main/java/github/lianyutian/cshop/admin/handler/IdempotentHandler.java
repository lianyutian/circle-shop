package github.lianyutian.cshop.admin.handler;

import github.lianyutian.cshop.admin.constant.AdminCacheKeyConstant;
import github.lianyutian.cshop.admin.model.vo.req.CouponTemplateReqVO;
import github.lianyutian.cshop.common.enums.BizCodeEnum;
import github.lianyutian.cshop.common.exception.BizException;
import github.lianyutian.cshop.common.redis.RedisCache;
import github.lianyutian.cshop.common.utils.CommonUtil;
import github.lianyutian.cshop.common.utils.JsonUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 幂等校验处理器
 *
 * @author lianyutian
 * @since 2025/3/6
 * @version 1.0
 */
@Component
@Order(100)
@Slf4j
public class IdempotentHandler implements CouponTemplateHandler {

  @Resource private RedisCache redisCache;

  // 下一个处理器
  private CouponTemplateHandler next;

  @Override
  public void handle(CouponTemplateContext context) {
    CouponTemplateReqVO couponTemplateReqVO = context.getCouponTemplateReqVO();
    HttpServletRequest servletRequest = context.getServletRequest();
    String clientId = generateUniqueKey(servletRequest);
    log.info(
        "责任链创建优惠券模板--幂等请求校验 request：{}, clientId：{}",
        JsonUtil.toJson(couponTemplateReqVO),
        clientId);
    String idempotentKey = AdminCacheKeyConstant.COUPON_IDEMPOTENT_KEY_PREFIX + clientId;
    Boolean res = redisCache.setIfAbsent(idempotentKey, "1", 300 * 1000);

    log.info("责任链创建优惠券模板--幂等请求校验 redis key：{}, result：{}", idempotentKey, res);
    if (Boolean.FALSE.equals(res)) {
      throw new BizException(BizCodeEnum.COMMON_OP_REPEAT);
    }

    // 将幂等键存入上下文用于后续清理
    context.setIdempotentKey(idempotentKey);

    if (next != null) {
      next.handle(context);
    }
  }

  /**
   * 设置下一个处理器
   *
   * @param next
   */
  @Override
  public void setNextHandler(CouponTemplateHandler next) {
    this.next = next;
  }

  /**
   * 生成唯一key
   *
   * @param request request
   * @return 唯一 key
   */
  public String generateUniqueKey(HttpServletRequest request) {
    String clientIp = CommonUtil.getRemoteIpAddr(request);
    return clientIp + "_" + System.currentTimeMillis();
  }
}
