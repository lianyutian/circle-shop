package github.lianyutian.cshop.admin.strategy;

import github.lianyutian.cshop.admin.model.vo.req.CouponTemplateReqVO;
import github.lianyutian.cshop.common.utils.JsonUtil;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * 优惠券领取规则生成策略
 *
 * @author lianyutian
 * @since 2025/3/11
 * @version 1.0
 */
@Component("receiveRuleStrategy")
public class ReceiveRuleStrategy implements CouponRuleStrategy {
  @Override
  public String generateRule(CouponTemplateReqVO couponTemplateReqVO) {
    Map<String, Object> rule = new HashMap<>();
    rule.put("type", "RECEIVE_RULE");
    // 每人限领个数
    rule.put("perUserLimit", new BigDecimal(couponTemplateReqVO.getCouponPerUserLimit()));
    // 使用说明 这个自定义吧
    rule.put("usageInstructions", "限时购、闪购等商品不可用");
    return JsonUtil.toJson(rule);
  }
}
