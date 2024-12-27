package github.lianyutian.cshop.user.model.param;

import lombok.Data;

/**
 * 用户信息修改入参
 *
 * @author lianyutian
 * @since 2024-12-27 13:23:04
 * @version 1.0
 */
@Data
public class UserEditParam {
    /**
     * 手机号
     */
    private String phone;

    /**
     * 用户名
     */
    private String name;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 性别
     */
    private int sex;
}
