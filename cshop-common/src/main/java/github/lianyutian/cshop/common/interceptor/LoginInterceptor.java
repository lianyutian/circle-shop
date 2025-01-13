package github.lianyutian.cshop.common.interceptor;

import github.lianyutian.cshop.common.enums.BizCodeEnum;
import github.lianyutian.cshop.common.model.ApiResult;
import github.lianyutian.cshop.common.model.LoginUserInfo;
import github.lianyutian.cshop.common.utils.CommonUtil;
import github.lianyutian.cshop.common.utils.JWTUtil;
import github.lianyutian.cshop.common.utils.JsonUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截
 *
 * @author lianyutian
 * @since 2024-12-25 15:47:02
 * @version 1.0
 */
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

  public static final ThreadLocal<LoginUserInfo> USER_THREAD_LOCAL = new ThreadLocal<>();

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    String token = request.getHeader("token");
    if (StringUtils.isBlank(token)) {
      token = request.getParameter("token");
    }

    Claims claims = JWTUtil.parserToken(token);
    if (claims == null) {
      // 解密失败，登录过期，提示账号未登录
      CommonUtil.sendResponse(response, ApiResult.result(BizCodeEnum.USER_ACCOUNT_UNLOGIN));
      return false;
    }

    long id = Long.parseLong(claims.get("id").toString());
    String avatar = (String) claims.get("avatar");
    String name = (String) claims.get("name");

    LoginUserInfo loginUserInfo = LoginUserInfo.builder().id(id).avatar(avatar).name(name).build();
    USER_THREAD_LOCAL.set(loginUserInfo);
    log.info("登录拦截器--用户信息：{}", JsonUtil.toJson(loginUserInfo));
    return true;
  }
}
