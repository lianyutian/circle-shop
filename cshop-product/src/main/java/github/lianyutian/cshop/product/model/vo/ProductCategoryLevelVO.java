package github.lianyutian.cshop.product.model.vo;

import java.util.List;
import lombok.Data;

/**
 * 二三级分类
 *
 * @author lianyutian
 * @since 2025-02-18 15:09:44
 * @version 1.0
 */
@Data
public class ProductCategoryLevelVO {
  private Integer id;

  /** 分类名称 */
  private String name;

  /** 分类别名 */
  private String alias;

  /** 分类层级 */
  private Integer level;

  /** 二三级分类 */
  private List<ProductCategoryVO> categoryList;
}
