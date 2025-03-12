package github.lianyutian.cshop.admin.strategy;

import github.lianyutian.cshop.admin.model.vo.req.CouponTemplateReqVO;
import github.lianyutian.cshop.common.utils.JsonUtil;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * 满减规则生成策略
 *
 * @author lianyutian
 * @since 2025/3/11
 * @version 1.0
 */
@Component("fullReductionRuleStrategy")
public class FullReductionRuleStrategy implements CouponRuleStrategy {
  @Override
  public String generateRule(CouponTemplateReqVO couponTemplateReqVO) {
    Map<String, Object> rule = new HashMap<>();
    rule.put("type", "FULL_REDUCTION");
    // 使用条件 满 x 元可用
    rule.put("useLimit", new BigDecimal(couponTemplateReqVO.getCouponUseLimit()));
    // 最大优惠券金额
    rule.put("maxAmountLimit", new BigDecimal(couponTemplateReqVO.getCouponMaxAmountLimit()));
    // 自领取优惠券后有效时间，单位小时
    rule.put("timeLimitPeriod", couponTemplateReqVO.getCouponTimeLimitPeriod());
    // 排除的商品类目
    rule.put("excludeCategories", "");
    return JsonUtil.toJson(rule);
  }
}
