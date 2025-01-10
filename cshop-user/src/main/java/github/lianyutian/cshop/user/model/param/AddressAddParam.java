package github.lianyutian.cshop.user.model.param;

import lombok.Data;

/**
 * 添加用户收货地址VO
 *
 * @author lianyutian
 * @since 2024-12-26 10:18:32
 * @version 1.0
 */
@Data
public class AddressAddParam {
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
