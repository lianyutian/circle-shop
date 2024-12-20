package github.lianyutian.cshop.user.service.captcha;

/**
 * 验证码服务接口
 *
 * @author lianyutian
 * @since 2024-12-17 17:17:03
 * @version 1.0
 */
public interface CaptchaService {

    /**
     * 发送验证码
     *
     * @param cacheKey 缓存key
     * @param to 接收者
     * @return 发送结果
     */
    boolean sendCode(String cacheKey, String to);
}
