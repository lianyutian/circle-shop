package github.lianyutian.cshop.admin.handler;

import github.lianyutian.cshop.admin.enums.CouponTarget;
import github.lianyutian.cshop.admin.model.vo.req.CouponTemplateReqVO;
import github.lianyutian.cshop.common.enums.BizCodeEnum;
import github.lianyutian.cshop.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 商品校验处理器
 *
 * @author lianyutian
 * @since 2025/3/11
 * @version 1.0
 */
@Component
@Order(400)
@Slf4j
public class ProductValidatorHandler implements CouponTemplateHandler {

  private CouponTemplateHandler next;

  @Override
  public void handle(CouponTemplateContext context) {
    CouponTemplateReqVO couponTemplateReqVO = context.getCouponTemplateReqVO();

    CouponTarget couponTarget = CouponTarget.fromCodeNum(couponTemplateReqVO.getCouponTarget());
    log.info("责任链创建优惠券模板--商品校验处理器 request：{}, target：{}", couponTemplateReqVO, couponTarget);
    if (couponTarget == CouponTarget.PRODUCT && couponTemplateReqVO.getProductId().isEmpty()) {
      log.info(
          "责任链创建优惠券模板--商品校验处理器 target：{}, productId：{}",
          couponTarget,
          couponTemplateReqVO.getProductId());
      throw new BizException(BizCodeEnum.ADMIN_COUPON_PRODUCT_ERROR);
    }
    // 设置商品id
    context.setProductId(couponTemplateReqVO.getProductId());
    if (next != null) {
      next.handle(context);
    }
  }

  @Override
  public void setNextHandler(CouponTemplateHandler handler) {
    this.next = handler;
  }
}
