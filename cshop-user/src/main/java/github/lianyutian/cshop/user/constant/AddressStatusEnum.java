package github.lianyutian.cshop.user.constant;

import lombok.Getter;

/**
 * 收货地址状态枚举
 *
 * @author lianyutian
 * @since 2024-12-26 17:34:38
 * @version 1.0
 */
@Getter
public enum AddressStatusEnum {
  DEFAULT_STATUS(0, "默认地址"),
  NOT_DEFAULT_STATUS(1, "非默认地址");

  private final Integer code;
  private final String message;

  AddressStatusEnum(Integer code, String message) {
    this.code = code;
    this.message = message;
  }
}
