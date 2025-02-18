package github.lianyutian.cshop.product.model.vo;

import lombok.Data;

/**
 * 商品分类VO
 *
 * @author lianyutian
 * @since 2025-02-18 14:24:45
 * @version 1.0
 */
@Data
public class ProductCategoryVO {
  private Integer id;

  /** 分类名称 */
  private String name;

  /** 分类别名 */
  private String alias;

  /** 分类层级 */
  private Integer level;
}
