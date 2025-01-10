package github.lianyutian.cshop.user.model.vo;

import lombok.Data;

/**
 * 用户地址VO 界面展示
 *
 * @author lianyutian
 * @since 2024-12-26 09:59:44
 * @version 1.0
 */
@Data
public class UserAddressVO {
  private Long id;

  /** 用户id */
  private Long userId;

  /** 是否默认收货地址：0否 1是 */
  private Integer defaultStatus;

  /** 收发货人姓名 */
  private String receiveName;

  /** 收货人电话 */
  private String phone;

  /** 省/直辖市 */
  private String province;

  /** 市 */
  private String city;

  /** 区 */
  private String region;

  /** 详细地址 */
  private String detailAddress;
}
