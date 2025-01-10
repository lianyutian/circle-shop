package github.lianyutian.cshop.user.model.vo;

import lombok.Data;

/**
 * 用户信息VO
 *
 * @author lianyutian
 * @since 2024-12-25 16:22:40
 * @version 1.0
 */
@Data
public class UserDetailVO {
  private Long id;

  /** 用户名 */
  private String name;

  /** 用户头像 */
  private String avatar;

  /** 0 女，1 男 */
  private Integer sex;

  /** 用户手机号 */
  private String phone;
}
