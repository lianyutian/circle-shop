package github.lianyutian.cshop.user.service.captcha.impl;

import github.lianyutian.cshop.common.utils.CommonUtil;
import github.lianyutian.cshop.user.service.captcha.CaptchaService;
import github.lianyutian.cshop.user.service.sms.SmsSender;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现类
 *
 * @author lianyutian
 * @since 2024-12-17 17:35:10
 * @version 1.0
 */
@Service
@Slf4j
@AllArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {
    /**
     * 短信验证码过期时间
     */
    private static final long CAPTCHA_EXPIRE_TIME = 60 * 1000 * 5;

    private final StringRedisTemplate redisTemplate;

    private final SmsSender smsSender;

    @Override
    public boolean sendCode(String cacheKey, String to) {
        // 获取随机验证码
        String code = CommonUtil.getRandomCode(6);
        // 拼接验证码  格式：验证码_时间戳
        String newCode = code + "_" + CommonUtil.getCurrentTimestamp();
        log.info("验证码模块-写入 Redis 验证码：{}", newCode);
        redisTemplate.opsForValue().set(cacheKey, newCode, CAPTCHA_EXPIRE_TIME, TimeUnit.MILLISECONDS);

        // 发送短信
        // boolean sendRes = smsSender.send(to, new String[]{code});
        boolean sendRes = true;

        if (!sendRes) {
            // 发送失败删除缓存
            redisTemplate.opsForValue().getAndDelete(cacheKey);
        }
        return sendRes;
    }
}
