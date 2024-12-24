package github.lianyutian.cshop.common.utils;

import github.lianyutian.cshop.common.model.LoginUserInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * jwt工具类
 *
 * @author lianyutian
 * @since 2024-12-24 10:01:54
 * @version 1.0
 */
@Slf4j
public class JWTUtil {
    /**
     *  AccessToken 过期时间，正常过期时间 7 天
     */
    private static final long EXPIRE = 1000L * 60 * 60 * 24 * 7;

    /**
     *  RefreshToken 过期时间，正常过期时间 30 天
     */
    public static final long REFRESH_EXPIRE = 1000L * 60 * 60 * 24 * 30;

    /**
     * 加密的密钥，从环境变量或配置文件中读取
     */
    private static final String SECRET = "circle-shop";

    /**
     * token 前缀
     */
    private static final String TOKEN_PREFIX = "circle-shop.";

    /**
     * SUBJECT
     */
    private static final String SUBJECT = "circle-shop";

    public static final String KEY_PREFIX = "circle-shop:refresh-token:";

    /**
     * 根据用户信息，生成 JWT 令牌
     *
     * @return JWT 令牌
     */
    public static Map<String, Object> createJwt(LoginUserInfo loginUserInfo) {
        if (loginUserInfo == null) {
            throw new NullPointerException("登录对象为空");
        }

        // 过期时间
        Date expireTime = new Date(System.currentTimeMillis() + EXPIRE);

        // 生成 JWT TOKEN
        String token = Jwts.builder().subject(SUBJECT)
                // payload
                .claim("id", loginUserInfo.getId())
                .claim("name", loginUserInfo.getName())
                .claim("avatar", loginUserInfo.getAvatar()).issuedAt(new Date()).expiration(expireTime)
                // 加密算法
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes()), Jwts.SIG.HS256)
                .compact();
        // 加前缀区分业务
        token = TOKEN_PREFIX + token;

        Map<String, Object> result = new HashMap<>();
        result.put("AccessToken", token);
        result.put("AccessTokenExpireTime", expireTime);
        result.put("RefreshToken", CommonUtil.generateUUID());

        log.info("公共服务-生成 token，过期时间：{}", expireTime);
        return result;
    }

    /**
     * 校验 token 是否正确
     *
     * @param token token
     * @return Claims
     */
    public static Claims parserToken(String token) {
        if (token == null || !token.startsWith(TOKEN_PREFIX)) {
            log.error("公共服务-解析 token 失败：无效的 token 格式");
            return null;
        }

        try {
            // 去除前缀后解析
            String cleanToken = token.replace(TOKEN_PREFIX, "");
            // jws解析
            Jws<Claims> claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes()))
                    .build()
                    .parseSignedClaims(cleanToken);

            log.info("公共服务-解析 token 成功");
            return claims.getPayload();
        } catch (Exception e) {
            log.error("公共服务-解析 token 失败：{}", e.getMessage(), e);
            return null;
        }
    }
}
