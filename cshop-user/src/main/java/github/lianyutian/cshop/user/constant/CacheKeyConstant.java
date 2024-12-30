package github.lianyutian.cshop.user.constant;

/**
 * 缓存常量
 *
 * @author lianyutian
 * @since 2024-12-24 13:46:37
 * @version 1.0
 */
public class CacheKeyConstant {
    /**
     * 图形验证码缓存 key 前缀
     */
    public static final String CAPTCHA_IMG_KEY_PREFIX = "cshop-user:img-captcha:";

    /**
     * 注册验证码缓存 key 前缀
     */
    public static final String CAPTCHA_REGISTER_KEY_PREFIX = "cshop-user:register:";

    /**
     * 用户信息更新锁前缀
     */
    public static final String USER_UPDATE_LOCK_KEY_PREFIX = "cshop-user:update:lock:";

    /**
     * 用户信息缓存前缀
     */
    public static final String USER_INFO_KEY_PREFIX = "cshop-user:info:";
}
