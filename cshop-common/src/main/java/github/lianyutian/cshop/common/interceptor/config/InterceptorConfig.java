package github.lianyutian.cshop.common.interceptor.config;

import github.lianyutian.cshop.common.interceptor.LoginInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 登录拦截配置
 *
 * @author lianyutian
 * @since 2024-12-25 16:17:31
 * @version 1.0
 */
@Configuration
@Slf4j
public class InterceptorConfig implements WebMvcConfigurer {

  public LoginInterceptor loginInterceptor() {
    return new LoginInterceptor();
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry
        // 添加拦截器
        .addInterceptor(loginInterceptor())
        // 不需要拦截的路径
        .excludePathPatterns(
            "/api/user/*/getImgCaptcha",
            "/api/user/*/sendRegisterCode",
            "/api/user/*/register",
            "/api/user/*/login",
            "/api/user/*/uploadUserAvatar",
            "/api/user/*/detailShow/*");
  }
}
