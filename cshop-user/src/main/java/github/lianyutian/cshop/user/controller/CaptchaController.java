package github.lianyutian.cshop.user.controller;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import github.lianyutian.cshop.common.enums.BizCodeEnum;
import github.lianyutian.cshop.common.utils.ApiResult;
import github.lianyutian.cshop.common.utils.CheckUtil;
import github.lianyutian.cshop.common.utils.CommonUtil;
import github.lianyutian.cshop.user.constant.UserCacheKeyConstant;
import github.lianyutian.cshop.user.service.captcha.CaptchaService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * CaptchaController
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

  /** 图形验证码过期时间 */
  private static final long CAPTCHA_EXPIRE_TIME = 60 * 1000 * 5;

  /** 验证码生成器 */
  private final DefaultKaptcha captchaProducer;

  private final StringRedisTemplate redisTemplate;

  private final CaptchaService captchaService;

  /**
   * 获取图形验证码
   *
   * @param request request
   * @param response response
   */
  @PostMapping("getImgCaptcha")
  public void getImgCaptcha(HttpServletRequest request, HttpServletResponse response) {
    // 1、先生成图形验证码文本
    String captchaText = captchaProducer.createText();
    log.info("验证码模块-获取图形验证码文本:{}", captchaText);

    // 2、存储到 redis 中，并设置过期时间
    redisTemplate
        .opsForValue()
        .set(getImgCaptchaKey(request), captchaText, CAPTCHA_EXPIRE_TIME, TimeUnit.MILLISECONDS);

    // 3、根据验证码文本生成图形验证码图片
    BufferedImage bufferedImage = captchaProducer.createImage(captchaText);

    try (ServletOutputStream outputStream = response.getOutputStream(); ) {
      ImageIO.write(bufferedImage, "jpg", outputStream);
      outputStream.flush();
    } catch (IOException e) {
      log.error("验证码模块-获取图形验证码异常:{}", e.getMessage(), e);
    }
  }

  /**
   * 发送注册验证码
   *
   * @param to 接收方
   */
  @PostMapping("sendRegisterCode")
  public ApiResult<Void> sendRegisterCode(@RequestParam(value = "to") String to) {
    if (!CheckUtil.isPhone(to)) {
      return ApiResult.result(BizCodeEnum.USER_PHONE_ERROR);
    }

    // 先从缓存中获取验证码 key - code:USER_REGISTER:电话或邮箱
    String cacheKey = UserCacheKeyConstant.CAPTCHA_REGISTER_KEY_PREFIX + to;
    String registerCode = redisTemplate.opsForValue().get(cacheKey);

    // 如果不为空，则判断是否60秒内重复发送
    if (StringUtils.isNotBlank(registerCode)) {
      // 从缓存中取出验证码发送时间戳
      long cacheCheckTtl = Long.parseLong(registerCode.split("_")[1]);
      // 当前时间 - 验证码发送时间戳，如果小于 60 秒，则不给重复发送
      if (CommonUtil.getCurrentTimestamp() - cacheCheckTtl < 1000 * 60) {
        log.info(
            "验证码模块-请不要重复发送验证码，时间间隔：{}", (CommonUtil.getCurrentTimestamp() - cacheCheckTtl) / 1000);
        return ApiResult.result(BizCodeEnum.USER_CODE_FAST_LIMITED);
      }
    }
    // 发送注册验证码
    boolean res = captchaService.sendCode(cacheKey, to);
    return res ? ApiResult.success() : ApiResult.result(BizCodeEnum.USER_CODE_SEND_ERROR);
  }

  private String getImgCaptchaKey(HttpServletRequest request) {
    // 获取客户端用户的 IP 地址
    String ip = CommonUtil.getRemoteIpAddr(request);
    // 获取请求头中的 User-Agent 属性值
    String userAgent = request.getHeader("User-Agent");

    // 根据 ip + userAgent 生成对应的 key
    return UserCacheKeyConstant.CAPTCHA_IMG_KEY_PREFIX + CommonUtil.MD5(ip + userAgent);
  }
}
