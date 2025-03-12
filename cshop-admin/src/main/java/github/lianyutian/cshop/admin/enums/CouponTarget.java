package github.lianyutian.cshop.admin.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;

/**
 * 优惠对象
 *
 * @author lianyutian
 * @since 2025/3/10
 * @version 1.0
 */
@Getter
public enum CouponTarget {
  /** 0 商品专属券 */
  PRODUCT(0, "商品专属券", "仅限指定商品使用"),
  /** 1 全店通用券 */
  SHOP(1, "全店通用券", "全店商品可用"),
  /** 2 无门槛券 */
  NO_THRESHOLD(2, "无门槛券", "无使用限制");

  // Getters
  private final int code;
  private final String displayName;
  private final String description;

  CouponTarget(int code, String displayName, String description) {
    this.code = code;
    this.displayName = displayName;
    this.description = description;
  }

  // 生产环境关键增强点：
  // 1. 添加缓存避免重复创建数组
  private static final Map<Integer, CouponTarget> CODE_MAP =
      Arrays.stream(values()).collect(Collectors.toMap(CouponTarget::getCode, Function.identity()));

  // 2. 安全转换方法（避免NPE）
  public static Optional<CouponTarget> fromCode(Integer code) {
    return Optional.ofNullable(code).flatMap(c -> Optional.ofNullable(CODE_MAP.get(c)));
  }

  // 3. 安全转换方法
  public static CouponTarget fromCodeNum(int code) {
    return Arrays.stream(CouponTarget.values())
        .filter(target -> target.getCode() == code)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("无效的优惠对象编码: " + code));
  }

  // 4. 校验合法性（用于参数校验）
  public static boolean isValid(Integer code) {
    return fromCode(code).isPresent();
  }
}
