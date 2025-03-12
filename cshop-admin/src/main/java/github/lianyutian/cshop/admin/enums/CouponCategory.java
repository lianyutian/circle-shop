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
public enum CouponCategory {
  /** 0 商家券 */
  MERCHANT(0, "商家券", "仅限商家券使用"),
  /** 1 平台券 */
  PLATFORM(1, "平台券", "平台商品可用");

  // Getters
  private final int code;
  private final String displayName;
  private final String description;

  CouponCategory(int code, String displayName, String description) {
    this.code = code;
    this.displayName = displayName;
    this.description = description;
  }

  // 生产环境关键增强点：
  // 1. 添加缓存避免重复创建数组
  private static final Map<Integer, CouponCategory> CODE_MAP =
      Arrays.stream(values())
          .collect(Collectors.toMap(CouponCategory::getCode, Function.identity()));

  // 2. 安全转换方法（避免NPE）
  public static Optional<CouponCategory> fromCode(Integer code) {
    return Optional.ofNullable(code).flatMap(c -> Optional.ofNullable(CODE_MAP.get(c)));
  }

  // 3. 安全转换方法
  public static CouponCategory fromCodeNum(int code) {
    return Arrays.stream(CouponCategory.values())
        .filter(target -> target.getCode() == code)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("无效的优惠券分类编码: " + code));
  }

  // 3. 校验合法性（用于参数校验）
  public static boolean isValid(Integer code) {
    return fromCode(code).isPresent();
  }
}
