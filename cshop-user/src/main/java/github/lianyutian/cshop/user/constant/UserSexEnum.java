package github.lianyutian.cshop.user.constant;

import lombok.Getter;

/**
 * 用户性别枚举
 *
 * @author lianyutian
 * @since 2024-12-27 13:24:05
 * @version 1.0
 */
@Getter
public enum UserSexEnum {
    FEMALE(0, "女"),
    MALE(1, "男");

    private final int code;
    private final String message;

    UserSexEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
