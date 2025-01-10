package github.lianyutian.cshop.note.model.param;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import lombok.Data;

/**
 * 添加笔记入参
 *
 * @author lianyutian
 * @since 2025-01-02 17:16:18
 * @version 1.0
 */
@Data
public class NoteAddParam {
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
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private Date publishTime;
}
