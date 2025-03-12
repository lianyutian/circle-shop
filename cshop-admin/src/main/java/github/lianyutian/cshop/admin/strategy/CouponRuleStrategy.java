package github.lianyutian.cshop.admin.strategy;

import github.lianyutian.cshop.admin.model.vo.req.CouponTemplateReqVO;

/**
 * @author lianyutian
 * @since 2025/3/11
 * @version 1.0
 */
public interface CouponRuleStrategy {
  /**
   * 生成规则
   *
   * @param couponTemplateReqVO
   * @return
   */
  String generateRule(CouponTemplateReqVO couponTemplateReqVO);
}
