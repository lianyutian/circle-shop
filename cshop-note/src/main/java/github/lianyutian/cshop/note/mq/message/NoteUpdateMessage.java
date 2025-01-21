package github.lianyutian.cshop.note.mq.message;

import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 笔记更新消息
 *
 * @author lianyutian
 * @since 2025-01-14 16:16:40
 * @version 1.0
 */
@Getter
@Setter
@Builder
public class NoteUpdateMessage implements Serializable {
  /** 笔记 ID */
  private Long noteId;

  /** 博主 ID */
  private Long userId;
}
