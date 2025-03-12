package github.lianyutian.cshop.admin.strategy;

import github.lianyutian.cshop.admin.model.vo.req.CouponTemplateReqVO;
import github.lianyutian.cshop.common.utils.JsonUtil;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * 优惠券折扣规则生成策略
 *
 * @author lianyutian
 * @since 2025/3/11
 * @version 1.0
 */
@Component("discountRuleStrategy")
public class DiscountRuleStrategy implements CouponRuleStrategy {

  @Override
  public String generateRule(CouponTemplateReqVO couponTemplateReqVO) {
    Map<String, Object> rule = new HashMap<>();
    rule.put("type", "DISCOUNT");
    // 折扣率，例如 0.8 表示 8 折
    rule.put("discountRateLimit", new BigDecimal(couponTemplateReqVO.getCouponDiscountRateLimit()));
    // 最大优惠券金额
    rule.put("maxAmountLimit", new BigDecimal(couponTemplateReqVO.getCouponMaxAmountLimit()));
    // 自领取优惠券后有效时间，单位小时
    rule.put("timeLimitPeriod", couponTemplateReqVO.getCouponTimeLimitPeriod());
    // 排除的商品类目
    rule.put("excludeCategories", "");
    return JsonUtil.toJson(rule);
  }
}
