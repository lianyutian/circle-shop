package github.lianyutian.cshop.cart.model.vo;

import github.lianyutian.cshop.cart.model.entity.SkuInfoEntity;
import java.util.Date;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * 购物车 SKU VO
 *
 * @author lianyutian
 * @since 2025-02-12 15:53:38
 * @version 1.0
 */
@Data
@Builder
public class CartSkuInfoVO {
  /** 用户ID */
  private Long userId;

  /** 商品编码 */
  private Long skuId;

  /** 商品名 */
  private String title;

  /** 商品图片 url TODO 后续增加商品服务后再进行修改 */
  private List<SkuInfoEntity.ImageInfo> image;

  /** 选中状态 */
  private Integer checkStatus;

  /** 商品价格 */
  private Integer price;

  /** 商品加购数量 */
  private Integer buyCount;

  /** 商品更新时间 */
  private Date updateTime;
}
