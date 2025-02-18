package github.lianyutian.cshop.product.model.vo;

import lombok.Data;

/**
 * 二级分类
 *
 * @author lianyutian
 * @since 2025-02-18 15:15:11
 * @version 1.0
 */
@Data
public class ProductCategoryLevel2VO {
  private Integer id;

  /** 分类名称 */
  private String name;

  /** 分类别名 */
  private String alias;

  /** 分类层级 */
  private Integer level;

  /** 父类id */
  private Integer parentId;

  /** 二级分类id */
  private Integer id2;

  /** 二级分类名称 */
  private String name2;

  /** 二级分类别名 */
  private String alias2;

  /** 二级分类层级 */
  private Integer level2;

  /** 二级父类id */
  private Integer parentId2;

  /** 三级分类id */
  private Integer id3;

  /** 三级分类名称 */
  private String name3;

  /** 三级分类别名 */
  private String alias3;

  /** 三级分类层级 */
  private Integer level3;

  /** 三级父类id */
  private Integer parentId3;
}
