package github.lianyutian.cshop.social.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 用户关注表
 *
 * @author lianyutian
 * @since 2025-01-14 09:49:05
 * @version 1.0
 */
@TableName(value = "user_attention")
@Data
public class UserAttention {
  /** 主键id */
  @TableId(type = IdType.AUTO)
  private Integer id;

  /** 当前博主用户id */
  private Integer userId;

  /** 关注博主用户id */
  private Integer attentionId;

  /** 创建设计，会按这个字段排序 */
  private Date createTime;

  /** 是否删除，0 正常 1 删除 */
  private Integer del;
}
