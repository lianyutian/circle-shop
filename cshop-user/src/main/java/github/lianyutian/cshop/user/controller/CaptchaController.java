package github.lianyutian.cshop.user.controller;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import github.lianyutian.cshop.common.utils.CommonUtil;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 验证码前端控制器
 *
 * @author lianyutian
 * @since 2024-12-17 11:17:12
 * @version 1.0
 */
@RestController
@RequestMapping("/api/captcha/v1")
@AllArgsConstructor
@Slf4j
public class CaptchaController {

    /**
     * 验证码过期时间
     */
    private static final long CAPTCHA_EXPIRE_TIME = 60 * 1000 * 5;

    /**
     * 验证码缓存 key 前缀
     */
    private static final String CAPTCHA_KEY_PREFIX = "cshop-user:captcha:";

    /**
     * 验证码生成器
     */
    private final DefaultKaptcha captchaProducer;


    private final StringRedisTemplate redisTemplate;

    /**
     * 获取图形验证码
     *
     * @param request request
     * @param response response
     */
    @RequestMapping("getCaptcha")
    public void getImgCaptcha(HttpServletRequest request, HttpServletResponse response) {
        // 1、先生成图形验证码文本
        String captchaText = captchaProducer.createText();
        log.info("验证码模块-获取图形验证码文本:{}", captchaText);

        // 2、存储到 redis 中，并设置过期时间
        redisTemplate.opsForValue().set(
                getCaptchaKey(request),
                captchaText,
                CAPTCHA_EXPIRE_TIME,
                TimeUnit.MILLISECONDS
        );

        // 3、根据验证码文本生成图形验证码图片
        BufferedImage bufferedImage = captchaProducer.createImage(captchaText);

        try (ServletOutputStream outputStream = response.getOutputStream();) {
            ImageIO.write(bufferedImage, "jpg", outputStream);
            outputStream.flush();
        } catch (IOException e) {
            log.error("验证码模块-获取图形验证码异常:{}", e.getMessage(), e);
        }
    }

    private String getCaptchaKey(HttpServletRequest request) {
        // 获取客户端用户的 IP 地址
        String ip = CommonUtil.getRemoteIpAddr(request);
        // 获取请求头中的 User-Agent 属性值
        String userAgent = request.getHeader("User-Agent");

        // 根据 ip + userAgent 生成对应的 key
        String keyGen = CAPTCHA_KEY_PREFIX + CommonUtil.MD5(ip + userAgent);

        log.info("验证码模块-验证码 key:{}", keyGen);
        return keyGen;
    }
}
