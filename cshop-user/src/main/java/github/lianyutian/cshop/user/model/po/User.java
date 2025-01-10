package github.lianyutian.cshop.user.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * User实体
 *
 * @author lianyutian
 * @since 2024-12-24 08:41:57
 * @version 1.0
 */
@TableName(value = "user")
@Data
public class User implements Serializable {
  /** */
  @TableId(type = IdType.AUTO)
  private Long id;

  /** 用户昵称 */
  private String name;

  /** 用户密码 */
  private String pwd;

  /** 用户头像 */
  private String avatar;

  /** 0 女，1 男 */
  private Integer sex;

  /** 用户手机号 */
  private String phone;

  /** 创建时间 */
  private Date createTime;

  /** 更新时间 */
  private Date updateTime;

  @Serial
  @TableField(exist = false)
  private static final long serialVersionUID = 1L;
}
