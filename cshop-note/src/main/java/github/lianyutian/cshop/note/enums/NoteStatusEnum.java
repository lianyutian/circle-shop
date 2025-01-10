package github.lianyutian.cshop.note.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 笔记状态枚举
 *
 * @author lianyutian
 * @since 2025-01-03 13:58:28
 * @version 1.0
 */
@Getter
public enum NoteStatusEnum {
  /** 审核中状态 */
  AUDITING(1, "审核中"),

  /** 未通过状态 */
  REJECTED(2, "未通过"),

  /** 已发布状态 */
  PUBLISHED(3, "已发布");

  @EnumValue private final int code;
  private final String message;

  NoteStatusEnum(int code, String message) {
    this.code = code;
    this.message = message;
  }
}
