package github.lianyutian.cshop.admin.handler;

import github.lianyutian.cshop.admin.enums.CouponCategory;
import github.lianyutian.cshop.admin.model.vo.req.CouponTemplateReqVO;
import github.lianyutian.cshop.common.enums.BizCodeEnum;
import github.lianyutian.cshop.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.UUID;

/**
 * 店铺校验处理器
 *
 * @author lianyutian
 * @since 2025/3/11
 * @version 1.0
 */
@Component
@Order(300)
@Slf4j
public class ShopValidatorHandler implements CouponTemplateHandler {

  private CouponTemplateHandler next;

  @Override
  public void handle(CouponTemplateContext context) {
    CouponTemplateReqVO couponTemplateReqVO = context.getCouponTemplateReqVO();
    CouponCategory couponCategory =
        CouponCategory.fromCodeNum(couponTemplateReqVO.getCouponCategory());
    log.info("责任链创建优惠券模板--店铺校验处理器 request：{}, category：{}", couponTemplateReqVO, couponCategory);
    if (couponCategory != CouponCategory.MERCHANT
        && StringUtils.isBlank(couponTemplateReqVO.getProductId())) {
      log.info(
          "责任链创建优惠券模板--店铺校验处理器 category：{}, category：{}",
          couponCategory,
          couponTemplateReqVO.getProductId());
      throw new BizException(BizCodeEnum.ADMIN_COUPON_PRODUCT_ERROR);
    }
    // 这里先使用 uuid 来生成
    String uuid = UUID.randomUUID().toString().replace("-", "");
    // 取 UUID 的前 16 个字符（64 位）
    String shortUUID = uuid.substring(0, 16);
    BigInteger shopNumber = new BigInteger(shortUUID, 16);
    log.info("责任链创建优惠券模板--店铺校验处理器 shopNumber：{}", shopNumber);
    context.setShopNumber(shopNumber);

    if (next != null) {
      next.handle(context);
    }
  }

  @Override
  public void setNextHandler(CouponTemplateHandler handler) {
    this.next = handler;
  }
}
