package github.lianyutian.cshop.product.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/** 商品分类 @TableName product_category */
@TableName(value = "product_category")
@Data
public class ProductCategory {
  /** 分类id */
  @TableId(type = IdType.AUTO)
  private Integer id;

  /** 父类id 上级分类的编号：0 表示一级分类 */
  private Integer parentId;

  /** 分类名称 */
  private String name;

  /** 分类别名 */
  private String alias;

  /** 是否启用：1 启用 2 禁用 */
  private Integer isEnable;

  /** 分类层级 */
  private Integer level;

  /** 分类状态：0 未删除 1 已删除 */
  private Integer isDeleted;

  /** 创建时间 */
  private Date createTime;

  /** 更新时间 */
  private Date updateTime;
}
