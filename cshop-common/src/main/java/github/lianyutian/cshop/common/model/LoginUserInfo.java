package github.lianyutian.cshop.common.model;

import lombok.Builder;
import lombok.Data;

/**
 * 登录用户信息
 *
 * @author lianyutian
 * @since 2024-12-24 09:57:05
 * @version 1.0
 */
@Data
@Builder
public class LoginUserInfo {
    private long id;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 用户名称
     */
    private String name;

    /**
     * 用户头像
     */
    private String avatar;
}
