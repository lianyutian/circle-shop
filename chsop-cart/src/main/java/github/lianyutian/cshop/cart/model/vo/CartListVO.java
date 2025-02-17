package github.lianyutian.cshop.cart.model.vo;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * 购物车列表
 *
 * @author lianyutian
 * @since 2025-02-17 13:57:44
 * @version 1.0
 */
@Data
@Builder
public class CartListVO {
  /** 未失效的购物车商品列表 */
  private List<CartSkuInfoVO> cartSkuList;

  /** 失效的购物车商品列表 */
  private List<CartSkuInfoVO> disabledCartSkuList;

  /** 结算价格信息 */
  private BillingInfoVO billing;
}
