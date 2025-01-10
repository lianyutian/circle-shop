package github.lianyutian.cshop.user.config;

import com.google.code.kaptcha.Constants;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Kaptcha配置类
 *
 * @author lianyutian
 * @since 2024-12-17 11:15:02
 * @version 1.0
 */
@Configuration
public class KaptchaConfig {

  @Bean
  @Qualifier("captchaProducer")
  public DefaultKaptcha producer() {
    Properties properties = new Properties();

    // 设置Kaptcha生成的图片宽度(像素)
    properties.setProperty(Constants.KAPTCHA_IMAGE_WIDTH, "200");
    // 设置Kaptcha生成的图片高度(像素)
    properties.setProperty(Constants.KAPTCHA_IMAGE_HEIGHT, "50");
    // 设置Kaptcha文本字体大小
    properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_FONT_SIZE, "40");
    // 设置Kaptcha文本字体颜色
    properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_FONT_COLOR, "black");
    // 设置Kaptcha文本字符长度
    properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_CHAR_LENGTH, "5");
    // 设置Kaptcha文本字符集为英文字母（大小写）和数字
    properties.setProperty(
        Constants.KAPTCHA_TEXTPRODUCER_CHAR_STRING,
        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890");
    // 设置Kaptcha文本字体
    properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_FONT_NAMES, "Arial");
    // NoNoise 类表示不添加任何噪声，即生成的验证码图片将不会有任何干扰线或点。
    // 通过设置 Constants.KAPTCHA_NOISE_IMPL 属性，可以控制验证码图片的样式和复杂度
    properties.setProperty(Constants.KAPTCHA_NOISE_IMPL, "com.google.code.kaptcha.impl.NoNoise");

    Config config = new Config(properties);
    DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
    defaultKaptcha.setConfig(config);
    return defaultKaptcha;
  }
}
