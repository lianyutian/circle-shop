package github.lianyutian.cshop.cart.model.vo;

import lombok.Builder;

/**
 * 购物车结算价格信息
 *
 * @author lianyutian
 * @since 2025-02-17 13:58:57
 * @version 1.0
 */
@Builder
public class BillingInfoVO {

  /** 合计金额 */
  private Integer totalPrice;

  /** 已优惠金额 */
  private Integer salePrice;
}
