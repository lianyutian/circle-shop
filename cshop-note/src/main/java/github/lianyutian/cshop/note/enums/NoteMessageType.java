package github.lianyutian.cshop.note.enums;

import lombok.Getter;

/**
 * 笔记 MQ 消息类型枚举
 *
 * @author lianyutian
 * @since 2025-01-15 14:26:26
 * @version 1.0
 */
@Getter
public enum NoteMessageType {
  /** 笔记新增消息 */
  ADD_NOTE("ADD", "新增笔记消息"),
  /** 笔记更新消息 */
  UPDATE_NOTE("UPDATE", "更新笔记消息");

  private final String type;
  private final String message;

  NoteMessageType(String type, String message) {
    this.type = type;
    this.message = message;
  }
}
