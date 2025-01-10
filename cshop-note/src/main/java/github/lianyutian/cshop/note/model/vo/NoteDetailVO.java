package github.lianyutian.cshop.note.model.vo;

import java.util.Date;
import lombok.Data;

/**
 * 笔记VO
 *
 * @author lianyutian
 * @since 2025-01-03 11:06:30
 * @version 1.0
 */
@Data
public class NoteDetailVO {
  /** 笔记id */
  private long id;

  /** 笔记图片链接 */
  private String imgUrls;

  /** 笔记标题 */
  private String title;

  /** 正文内容 */
  private String content;

  /** 笔记视频链接 */
  private String videoUrl;

  /** 自主声明 */
  private String declaration;

  /** 1 公开可见，2 仅自己可见，3 仅互关好友可见，4 部分人可见 5 部分人不可见 */
  private Integer visibleRange;

  /** 发布时间 */
  private Date publishTime;
}
