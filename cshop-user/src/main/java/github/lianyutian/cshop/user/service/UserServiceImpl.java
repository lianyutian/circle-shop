package github.lianyutian.cshop.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import github.lianyutian.cshop.common.enums.BizCodeEnums;
import github.lianyutian.cshop.common.model.LoginUserInfo;
import github.lianyutian.cshop.common.utils.ApiResult;
import github.lianyutian.cshop.user.mapper.UserMapper;
import github.lianyutian.cshop.user.model.po.User;
import github.lianyutian.cshop.user.model.vo.UserLoginVO;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static github.lianyutian.cshop.common.utils.JWTUtil.KEY_PREFIX;
import static github.lianyutian.cshop.common.utils.JWTUtil.REFRESH_EXPIRE;
import static github.lianyutian.cshop.common.utils.JWTUtil.createJwt;
import static github.lianyutian.cshop.common.utils.JWTUtil.parserToken;

/**
 * 用户服务实现类
 *
 * @author lianyutian
 * @since 2024-12-24 08:44:49
 * @version 1.0
 */
@Service
@Slf4j
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    private final StringRedisTemplate redisTemplate;

    @Override
    public ApiResult<Map<String, Object>> login(UserLoginVO userLoginVO) {
        // 1. 根据手机号查询是否注册过
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone, userLoginVO.getPhone());
        List<User> userList = userMapper.selectList(queryWrapper);
        if (userList != null && userList.size() == 1) {
            // 1.1 该手机号已经注册了
            User user = userList.get(0);
            String pwd = Md5Crypt.md5Crypt(userLoginVO.getPassword().getBytes(), user.getSecret());
            // 1.2 如果请求的密码加密后跟数据库的匹配
            if (pwd.equals(user.getPwd())) {
                // 登录成功，生成 jwt
                Map<String, Object> jwt = createNewJwt(user);

                return ApiResult.success(jwt);
            } else {
                return ApiResult.result(BizCodeEnums.USER_ACCOUNT_PWD_ERROR);
            }
        } else {
            // 未注册
            return ApiResult.result(BizCodeEnums.USER_ACCOUNT_UNREGISTER);
        }
    }

    @Override
    public ApiResult<Map<String, Object>> refreshToken(String refreshToken, String accessToken) {
        String refreshTokenVal = redisTemplate.opsForValue().get(KEY_PREFIX + refreshToken);
        // refreshToken 过期
        if (StringUtils.isBlank(refreshTokenVal)) {
            return ApiResult.result(BizCodeEnums.USER_REFRESH_TOKEN_EMPTY);
        }
        // 2、如果存在，解密 accessToken
        Claims claims = parserToken(accessToken);
        if (claims == null) {
            // 无法解密提示未登录
            return ApiResult.result(BizCodeEnums.USER_ACCOUNT_UNLOGIN);
        }
        // 3、如果可以解密 accessToken， 则重新生成 accessToken 等信息返回
        long userId = Long.parseLong(claims.get("id").toString());
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getId, userId);
        List<User> userList = userMapper.selectList(queryWrapper);
        if (userList != null && userList.size() == 1) {
            User user = userList.get(0);
            Map<String, Object> jwt = createNewJwt(user);
            // 删除旧的 refreshToken
            redisTemplate.opsForValue().getAndDelete(KEY_PREFIX + refreshToken);
            return ApiResult.success(jwt);
        } else {
            // 无法解密提示未登录
            return ApiResult.result(BizCodeEnums.USER_ACCOUNT_UNLOGIN);
        }
    }

    private Map<String, Object> createNewJwt(User user) {
        // 登录成功，生成 Token
        LoginUserInfo loginUserInfo = LoginUserInfo.builder().build();
        // 拷贝
        BeanUtils.copyProperties(user, loginUserInfo);
        // 生成 JWT Token，过期时间
        Map<String,Object> jwt = createJwt(loginUserInfo);
        // 4、设置 RefreshToken 到 Redis 中，过期时间为 30 天
        String newRefreshToken = (String) jwt.get("RefreshToken");
        String key = KEY_PREFIX + newRefreshToken;
        redisTemplate.opsForValue().set(key, "1", REFRESH_EXPIRE, TimeUnit.MILLISECONDS);
        return jwt;
    }
}
