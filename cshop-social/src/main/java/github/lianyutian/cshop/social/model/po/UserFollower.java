package github.lianyutian.cshop.social.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 粉丝关系表
 *
 * @author lianyutian
 * @since 2025-01-14 09:49:05
 * @version 1.0
 */
@TableName(value = "user_follower")
@Data
public class UserFollower {
  /** 主键id */
  @TableId(type = IdType.AUTO)
  private Long id;

  /** 博主id */
  private Long userId;

  /** 粉丝id */
  private Long followerId;

  /** 创建时间，按时间排序 */
  private Date createTime;

  /** 是否删除 0 正常 1删除 */
  private Integer del;
}
