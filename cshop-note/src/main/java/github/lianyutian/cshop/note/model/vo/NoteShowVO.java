package github.lianyutian.cshop.note.model.vo;

import java.util.Date;
import lombok.Data;

/**
 * 笔记展示VO
 *
 * @author lianyutian
 * @since 2025-01-03 11:06:30
 * @version 1.0
 */
@Data
public class NoteShowVO {
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

  /** 发布时间 */
  private Date publishTime;
}
