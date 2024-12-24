package github.lianyutian.cshop.user.model.vo;

import lombok.Data;

/**
 * 用户登录VO
 *
 * @author lianyutian
 * @since 2024-12-24 08:40:33
 * @version 1.0
 */
@Data
public class UserLoginVO {
    /**
     * 手机号
     */
    private String phone;

    /**
     * 密码
     */
    private String password;
}
