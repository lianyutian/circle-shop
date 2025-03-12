package github.lianyutian.cshop.admin.handler;

import github.lianyutian.cshop.admin.enums.CouponTarget;
import github.lianyutian.cshop.admin.model.vo.req.CouponTemplateReqVO;
import github.lianyutian.cshop.common.enums.BizCodeEnum;
import github.lianyutian.cshop.common.exception.BizException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 参数校验处理器
 *
 * @author lianyutian
 * @since 2025/3/6
 * @version 1.0
 */
@Component
@Order(200)
@Slf4j
public class ParamValidatorHandler implements CouponTemplateHandler {

  private CouponTemplateHandler next;

  @Override
  public void handle(CouponTemplateContext context) {
    // 获取请求
    CouponTemplateReqVO request = context.getCouponTemplateReqVO();
    log.info("责任链创建优惠券模板--参数请求校验：{}", request);
    // JSR303校验
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    Set<ConstraintViolation<CouponTemplateReqVO>> violations =
        factory.getValidator().validate(request);

    log.info("责任链创建优惠券模板--参数请求校验 violations：{}", violations);

    if (!violations.isEmpty()) {
      String errorMsg =
          violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(";"));
      log.error("责任链创建优惠券模板--参数请求校验异常：{}", errorMsg);
      throw new BizException(BizCodeEnum.COMMON_PARAM_ERROR + ":" + errorMsg);
    }

    if (request.getCouponStartTime().after(request.getCouponEndTime())) {
      log.error(
          "责任链创建优惠券模板--有效期设置错误 startTime：{}, endTime：{}",
          request.getCouponStartTime(),
          request.getCouponEndTime());
      throw new BizException("有效期设置错误");
    }

    // 校验优惠券对象逻辑 优惠对象 0:商品专属 1:全店通用 2: 无门槛
    CouponTarget target = CouponTarget.fromCodeNum(request.getCouponTarget());
    if (target == CouponTarget.PRODUCT && request.getProductId().isEmpty()) {
      log.error("责任链创建优惠券模板--校验优惠券对象逻辑错误 target：{}, productId：{}", target, request.getProductId());
      throw new BizException(BizCodeEnum.ADMIN_COUPON_PRODUCT_ERROR);
    }

    // 其他基础校验... TODO
    if (next != null) next.handle(context);
  }

  @Override
  public void setNextHandler(CouponTemplateHandler handler) {
    this.next = handler;
  }
}
