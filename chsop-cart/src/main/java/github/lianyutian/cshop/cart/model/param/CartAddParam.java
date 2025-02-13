package github.lianyutian.cshop.cart.model.param;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 添加购物车入参
 *
 * @author lianyutian
 * @since 2025-02-12 15:48:19
 * @version 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartAddParam {

  /** 用户 id：加入到哪个用户的购物车里去 */
  private Long userId;

  /** 商品 skuId：代表了是一个商品，购买一个商品 */
  private Long skuId;

  /** 加购数量 */
  @Min(0)
  private Integer buyCount;
}
