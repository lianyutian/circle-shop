package github.lianyutian.cshop.admin.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;

/**
 * @author lianyutian
 * @since 2025/3/11
 * @version 1.0
 */
@Getter
public enum CouponType {
  /** 0 立减券 */
  INSTANT(0, "立减券", "立减券"),
  /** 1 满减券 */
  FULL_REDUCTION(1, "满减券", "满减券"),
  /** 2 折扣券 */
  DISCOUNT(2, "折扣券", "折扣券");

  // Getters
  private final int code;
  private final String displayName;
  private final String description;

  CouponType(int code, String displayName, String description) {
    this.code = code;
    this.displayName = displayName;
    this.description = description;
  }

  // 生产环境关键增强点：
  // 1. 添加缓存避免重复创建数组
  private static final Map<Integer, CouponType> CODE_MAP =
      Arrays.stream(values()).collect(Collectors.toMap(CouponType::getCode, Function.identity()));

  // 2. 安全转换方法（避免NPE）
  public static Optional<CouponType> fromCode(Integer code) {
    return Optional.ofNullable(code).flatMap(c -> Optional.ofNullable(CODE_MAP.get(c)));
  }

  // 3. 安全转换方法
  public static CouponType fromCodeNum(int code) {
    return Arrays.stream(CouponType.values())
        .filter(target -> target.getCode() == code)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("无效的优惠券类型编码: " + code));
  }

  // 4. 校验合法性（用于参数校验）
  public static boolean isValid(Integer code) {
    return fromCode(code).isPresent();
  }
}
