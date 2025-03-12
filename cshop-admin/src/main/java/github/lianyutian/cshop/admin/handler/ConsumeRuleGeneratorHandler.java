package github.lianyutian.cshop.admin.handler;

import github.lianyutian.cshop.admin.enums.CouponType;
import github.lianyutian.cshop.admin.model.vo.req.CouponTemplateReqVO;
import github.lianyutian.cshop.admin.strategy.CouponRuleStrategy;
import github.lianyutian.cshop.common.enums.BizCodeEnum;
import github.lianyutian.cshop.common.exception.BizException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 优惠券使用规则生成处理器
 *
 * @author lianyutian
 * @since 2025/3/11
 * @version 1.0
 */
@Component
@Order(500)
@Slf4j
public class ConsumeRuleGeneratorHandler implements CouponTemplateHandler {

  private CouponTemplateHandler next;

  private final Map<CouponType, CouponRuleStrategy> strategies;

  public ConsumeRuleGeneratorHandler(
      @Qualifier("directReductionRuleStrategy") CouponRuleStrategy directReductionRuleStrategy,
      @Qualifier("discountRuleStrategy") CouponRuleStrategy discountStrategy,
      @Qualifier("fullReductionRuleStrategy") CouponRuleStrategy fullReductionStrategy) {

    strategies =
        Map.of(
            CouponType.INSTANT, directReductionRuleStrategy,
            CouponType.DISCOUNT, discountStrategy,
            CouponType.FULL_REDUCTION, fullReductionStrategy);
  }

  @Override
  public void handle(CouponTemplateContext context) {
    if (context == null) {
      log.error("责任链创建优惠券模板--优惠券消耗规则生成处理器 context：{}", context);
      throw new IllegalArgumentException("CouponTemplateContext cannot be null");
    }
    CouponTemplateReqVO couponTemplateReqVO = context.getCouponTemplateReqVO();
    if (couponTemplateReqVO == null) {
      throw new IllegalArgumentException("CouponTemplateReqVO in context cannot be null");
    }
    CouponType couponType = CouponType.fromCodeNum(couponTemplateReqVO.getCouponType());
    CouponRuleStrategy couponRuleStrategy = strategies.get(couponType);
    log.info("责任链创建优惠券模板--优惠券消耗规则生成处理器 couponType：{}, strategy：{}", couponType, couponRuleStrategy);
    if (couponRuleStrategy == null) {
      throw new BizException(BizCodeEnum.ADMIN_COUPON_CONSUME_RULE_GENERATE_ERROR);
    }
    String rule = couponRuleStrategy.generateRule(couponTemplateReqVO);
    context.getCouponTemplateResVO().setCouponConsumeRule(rule);
    if (next != null) {
      next.handle(context);
    }
  }

  @Override
  public void setNextHandler(CouponTemplateHandler handler) {
    this.next = handler;
  }
}
