package github.lianyutian.cshop.admin.model.vo.res;

import java.math.BigInteger;
import java.util.Date;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author lianyutian
 * @since 2025/3/6
 * @version 1.0
 */
@Data
@Accessors(chain = true)
public class CouponTemplateResVO {
  private Long id;

  /** 优惠券名称 */
  private String couponName;

  /** 店铺编号 */
  private BigInteger shopNumber;

  /** 优惠券分类 0:商家券 1:平台券 */
  private Boolean couponCategory;

  /** 优惠对象 0:商品专属 1:全店通用 2: 无门槛 */
  private Integer couponTarget;

  /** 优惠商品id */
  private String productId;

  /** 优惠券类型 0:立减券 1:满减券 2:折扣券 */
  private Integer couponType;

  /** 优惠券领取方式 0:手动领取 1:新人券 2:赠送券 3:会员券 */
  private Integer receiveType;

  /** 优惠券有效期开始时间 */
  private Date couponStartTime;

  /** 优惠券有效期结束时间 */
  private Date couponEndTime;

  /** 优惠券发行数量 */
  private Integer couponCount;

  /** 优惠券已经领取的数量 */
  private Integer couponReceivedCount;

  /** 优惠券领取规则 */
  private String couponReceiveRule;

  /** 优惠券消耗规则 */
  private String couponConsumeRule;

  /** 优惠券状态 0:生效中 1:已结束 */
  private Boolean couponStatus;

  /** 优惠券审核状态 0:待审核 1:已通过 2:已驳回 */
  private Integer couponAuditStatus;

  /** 删除标识 0:未删除 1:已删除 */
  private Boolean couponDel;

  /** 创建时间 */
  private Date createTime;

  /** 修改时间 */
  private Date updateTime;
}
