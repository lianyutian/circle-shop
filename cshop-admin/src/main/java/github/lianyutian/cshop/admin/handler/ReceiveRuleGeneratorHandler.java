package github.lianyutian.cshop.admin.handler;

import github.lianyutian.cshop.admin.model.vo.req.CouponTemplateReqVO;
import github.lianyutian.cshop.admin.strategy.CouponRuleStrategy;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 优惠券领取规则处理器
 *
 * @author lianyutian
 * @since 2025/3/11
 * @version 1.0
 */
@Component
@Order(600)
@Slf4j
public class ReceiveRuleGeneratorHandler implements CouponTemplateHandler {

  private CouponTemplateHandler next;

  private Map<Integer, CouponRuleStrategy> strategies;
  ;

  public ReceiveRuleGeneratorHandler(
      @Qualifier("receiveRuleStrategy") CouponRuleStrategy receiveRuleStrategy) {
    strategies = Map.of(1, receiveRuleStrategy);
  }

  @Override
  public void handle(CouponTemplateContext context) {
    CouponTemplateReqVO couponTemplateReqVO = context.getCouponTemplateReqVO();
  }

  @Override
  public void setNextHandler(CouponTemplateHandler handler) {
    this.next = handler;
  }
}
