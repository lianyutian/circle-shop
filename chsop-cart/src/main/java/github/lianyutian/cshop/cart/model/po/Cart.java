package github.lianyutian.cshop.cart.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 购物车表 @TableName cart
 *
 * @author lianyutian
 */
@TableName(value = "cart")
@Data
public class Cart {
  /** */
  @TableId(type = IdType.AUTO)
  private Long id;

  /** 用户id */
  private Long userId;

  /** 商品 id */
  private Long skuId;

  /** 商品加购数量 */
  private Integer skuCount;

  /** 商品加购金额 */
  private Integer skuPrice;

  /** 商品优惠价 */
  private Integer skuSalePrice;

  /** 购物车状态 0 正常 1 删除 */
  private Integer status;

  /** 创建时间 */
  private Date createTime;

  /** 创建用户id */
  private Long createUser;

  /** 更新时间 */
  private Date updateTime;

  /** 更新用户id */
  private Long updateUser;
}
