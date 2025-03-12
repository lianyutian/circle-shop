package github.lianyutian.cshop.admin.controller;

import github.lianyutian.cshop.admin.model.vo.req.CouponTemplateReqVO;
import github.lianyutian.cshop.admin.service.CouponTemplateService;
import github.lianyutian.cshop.common.model.ApiResult;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 优惠券管理控制器
 *
 * @author lianyutian
 * @since 2025/3/5
 * @version 1.0
 */
@RestController
@RequestMapping("api/admin/marketing/coupon")
@Slf4j
@AllArgsConstructor
public class CouponTemplateController {

  private final CouponTemplateService couponTemplateService;

  public ApiResult<Void> create(@RequestBody @Validated CouponTemplateReqVO couponTemplateReqVO) {
    couponTemplateService.create(couponTemplateReqVO);
    return null;
  }
}
