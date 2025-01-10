package github.lianyutian.cshop.user.model.param;

import lombok.Data;

/**
 * 用户注册参数
 *
 * @author lianyutian
 * @since 2024-12-24 13:30:19
 * @version 1.0
 */
@Data
public class UserRegisterParam {
  /** 用户名 */
  private String name;

  /** 密码 */
  private String password;

  /** 用户头像 */
  private String avatar;

  /** 性别 0 女，1 男 */
  private String sex;

  /** 手机号 */
  private String phone;

  /** 验证码 */
  private String code;
}
