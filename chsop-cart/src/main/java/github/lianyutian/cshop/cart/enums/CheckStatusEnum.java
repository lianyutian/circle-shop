package github.lianyutian.cshop.cart.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * 商品选中状态枚举
 *
 * @author lianyutian
 * @since 2025-02-12 16:04:23
 * @version 1.0
 */
public enum CheckStatusEnum {
  /** 选中 */
  CHECKED(1, "选中"),

  /** 未选中 */
  NO_CHECKED(0, "未选中");

  private final Integer code;

  private final String status;

  CheckStatusEnum(Integer code, String value) {
    this.code = code;
    this.status = value;
  }

  public Integer getCode() {
    return code;
  }

  public String getStatus() {
    return status;
  }

  public static Map<Integer, String> toMap() {
    Map<Integer, String> map = new HashMap<>(16);
    for (CheckStatusEnum element : CheckStatusEnum.values()) {
      map.put(element.getCode(), element.getStatus());
    }
    return map;
  }

  public static CheckStatusEnum getByCode(Integer code) {
    if (code == null) {
      return null;
    }
    for (CheckStatusEnum element : CheckStatusEnum.values()) {
      if (code.equals(element.getCode())) {
        return element;
      }
    }
    return null;
  }
}
