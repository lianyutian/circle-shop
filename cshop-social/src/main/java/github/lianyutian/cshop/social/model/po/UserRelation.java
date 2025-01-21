package github.lianyutian.cshop.social.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户关系表
 *
 * @author lianyutian
 * @since 2025-01-14 09:49:05
 * @version 1.0
 */
@TableName(value = "user_relation")
@Data
public class UserRelation {
  /** 主键id */
  @TableId(type = IdType.AUTO)
  private Integer id;

  /** 用户id */
  private Integer userId;

  /** 关注数 */
  private Integer attentionCount;

  /** 粉丝数 */
  private Integer followerCount;
}
