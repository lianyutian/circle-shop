package github.lianyutian.cshop.user.model.param;

import lombok.Data;

/**
 * 用户登录参数
 *
 * @author lianyutian
 * @since 2024-12-24 08:40:33
 * @version 1.0
 */
@Data
public class UserLoginParam {
  /** 手机号 */
  private String phone;

  /** 密码 */
  private String password;
}
