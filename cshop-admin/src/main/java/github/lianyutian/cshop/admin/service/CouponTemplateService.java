package github.lianyutian.cshop.admin.service;

import github.lianyutian.cshop.admin.model.vo.req.CouponTemplateReqVO;

/**
 * @author lianyutian
 * @since 2025/3/5
 * @version 1.0
 */
public interface CouponTemplateService {
  /**
   * 创建优惠券模板
   *
   * @param couponTemplateReqVO 模板请求参数
   * @return 创建结果
   */
  boolean create(CouponTemplateReqVO couponTemplateReqVO);
}
