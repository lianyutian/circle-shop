package github.lianyutian.cshop.note.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 可见范围枚举
 *
 * @author lianyutian
 * @since 2025-01-03 14:06:21
 * @version 1.0
 */
@Getter
public enum VisibleRangeEnum {
  /** 公开可见 */
  PUBLIC_VISIBLE(1, "公开可见"),

  /** 仅自己可见 */
  PRIVATE_VISIBLE(2, "仅自己可见"),

  /** 仅互关好友可见 */
  FRIEND_VISIBLE(3, "仅互关好友可见"),

  /** 部分人可见 */
  PART_VISIBLE(4, "部分人可见"),

  /** 部分人不可见 */
  PART_INVISIBLE(5, "部分人不可见");

  @EnumValue private final int code;
  private final String message;

  VisibleRangeEnum(int code, String message) {
    this.code = code;
    this.message = message;
  }
}
