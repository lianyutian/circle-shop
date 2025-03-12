package github.lianyutian.cshop.admin.model.vo.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigInteger;
import java.util.Date;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

/**
 * 优惠券模板请求参数
 *
 * @author lianyutian
 * @since 2025/3/5
 * @version 1.0
 */
@Data
@Accessors(chain = true)
public class CouponTemplateReqVO {

  private Long id;

  /** 优惠券名称 */
  @NotBlank(message = "请填写优惠券名称")
  @Length(max = 256, message = "优惠券名称长度不能超过256个字符")
  private String couponName;

  /** 优惠券分类 0:商家券 1:平台券 */
  @Range(min = 0, max = 1, message = "请选择优惠分类")
  private Integer couponCategory;

  /** 优惠对象 0:商品专属 1:全店通用 2:无门槛 */
  @Range(min = 0, max = 2, message = "请选择优惠对象")
  private Integer couponTarget;

  /** 优惠商品id */
  private String productId;

  /** 店铺编号 */
  private BigInteger shopNumber;

  /** 优惠券类型 0:立减券 1:满减券 2:折扣券 */
  @Range(min = 0, max = 2, message = "请选择优惠券类型")
  private Integer couponType;

  /** 优惠券领取方式 0:手动领取 1:新人券 2:赠送券 3:会员券 */
  @Range(min = 0, max = 3, message = "请选择优惠券领取方式")
  private Integer receiveType;

  /** 领取规则 */
  private Integer couponPerUserLimit;

  /** 满减券规则使用条件 满 x 元可用 */
  private Integer couponUseLimit;

  /** 立减券规则使用条件 满 x 元可用 */
  private Integer couponReductionAmountLimit;

  /** 折扣券规则使用条件 折扣率 */
  private Integer couponDiscountRateLimit;

  /** 最大优惠券金额 */
  private Integer couponMaxAmountLimit;

  /** 自领取优惠券后有效时间，单位小时 */
  private Integer couponTimeLimitPeriod;

  /** 优惠券有效期开始时间 */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private Date couponStartTime;

  /** 优惠券有效期结束时间 */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private Date couponEndTime;

  /** 优惠券发行数量 */
  @NotNull(message = "请填写优惠券发行数量")
  private Integer couponCount;

  /** 优惠券状态 */
  @NotNull(message = "请设置优惠券状态")
  private Boolean couponStatus;

  /** 优惠券审核状态 */
  private Integer couponAuditStatus;

  /** 删除标识 */
  private Boolean couponDel;
}
