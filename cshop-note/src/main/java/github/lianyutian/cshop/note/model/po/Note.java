package github.lianyutian.cshop.note.model.po;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import github.lianyutian.cshop.note.enums.NoteStatusEnum;
import github.lianyutian.cshop.note.enums.VisibleRangeEnum;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户笔记表
 *
 * @author lianyutian
 */
@TableName(value = "note")
@Data
public class Note implements Serializable {
  @TableId(type = IdType.AUTO)
  private Long id;

  /** 用户id */
  private Long userId;

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
  @EnumValue private VisibleRangeEnum visibleRange;

  /** 1 审核中, 2 未通过，3 已发布 */
  @EnumValue private NoteStatusEnum status;

  /** 发布时间 */
  private Date publishTime;

  /** 创建时间 */
  private Date createTime;

  /** 更新时间 */
  private Date updateTime;

  @Serial
  @TableField(exist = false)
  private static final long serialVersionUID = 1L;
}
