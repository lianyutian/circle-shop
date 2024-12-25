package github.lianyutian.cshop.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import github.lianyutian.cshop.common.enums.BizCodeEnums;
import github.lianyutian.cshop.common.model.LoginUserInfo;
import github.lianyutian.cshop.common.utils.ApiResult;
import github.lianyutian.cshop.common.utils.JWTUtil;
import github.lianyutian.cshop.user.constant.CacheKeyConstant;
import github.lianyutian.cshop.user.mapper.UserMapper;
import github.lianyutian.cshop.user.model.po.User;
import github.lianyutian.cshop.user.model.vo.UserLoginVO;
import github.lianyutian.cshop.user.model.vo.UserRegisterVO;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static github.lianyutian.cshop.common.utils.JWTUtil.KEY_PREFIX;
import static github.lianyutian.cshop.common.utils.JWTUtil.REFRESH_EXPIRE;

/**
 * 用户服务实现类
 *
 * @author lianyutian
 * @since 2024-12-24 08:44:49
 * @version 1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    private final StringRedisTemplate redisTemplate;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public ApiResult<Void> register(UserRegisterVO userRegisterVO) {
        // 1. 校验注册验证码是否正确
        boolean checked = checkCode(userRegisterVO.getPhone(), userRegisterVO.getCode());
        if (!checked) {
            return ApiResult.result(BizCodeEnums.USER_CODE_PHONE_ERROR);
        }
        // 1.2 通过手机号唯一索引实现唯一
        User user = new User();
        BeanUtils.copyProperties(userRegisterVO, user);
        user.setCreateTime(new Date());
        // 密码加密
        String secretPwd = passwordEncoder.encode(userRegisterVO.getPassword());
        user.setPwd(secretPwd);
        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException e) {
            log.warn("用户微服务-注册模块-用户已存在 {}", userRegisterVO.getPhone());
            return ApiResult.result(BizCodeEnums.USER_ACCOUNT_EXIST);
        }
        return ApiResult.success();
    }

    @Override
    public ApiResult<Map<String, Object>> login(UserLoginVO userLoginVO) {
        // 1. 根据手机号查询是否存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone, userLoginVO.getPhone());
        List<User> userList = userMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(userList)) {
            // 未注册
            return ApiResult.result(BizCodeEnums.USER_ACCOUNT_PWD_ERROR);
        }

        // 1.1 该手机号已经注册了
        User user = userList.get(0);
        if (passwordEncoder.matches(userLoginVO.getPassword(), user.getPwd())) {
            // 登录成功，生成 jwt
            Map<String, Object> jwt = createNewJwt(user);
            return ApiResult.success(jwt);
        } else {
            return ApiResult.result(BizCodeEnums.USER_ACCOUNT_PWD_ERROR);
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
        Claims claims = JWTUtil.parserToken(accessToken);
        if (claims == null) {
            // 无法解密提示未登录
            return ApiResult.result(BizCodeEnums.USER_ACCOUNT_UNLOGIN);
        }
        // 3、如果可以解密 accessToken， 则重新生成 accessToken 等信息返回
        long userId = Long.parseLong(claims.get("id").toString());
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getId, userId);
        User user = userMapper.selectOne(queryWrapper);
        if (user != null) {
            Map<String, Object> jwt = createNewJwt(user);
            // 删除旧的 refreshToken
            redisTemplate.opsForValue().getAndDelete(KEY_PREFIX + refreshToken);
            return ApiResult.success(jwt);
        } else {
            // 无法解密提示未登录
            return ApiResult.result(BizCodeEnums.USER_ACCOUNT_UNLOGIN);
        }
    }

    private boolean checkCode(String phone, String code) {
        // 先从缓存中获取验证码 key - code:USER_REGISTER:电话或邮箱
        String cacheKey = CacheKeyConstant.CAPTCHA_REGISTER_KEY_PREFIX + phone;
        String codeVal = redisTemplate.opsForValue().get(cacheKey);
        if (StringUtils.isBlank(codeVal)) {
            return false;
        }
        String[] parts = codeVal.split("_");
        String registerCode = parts[0];
        if (registerCode.equals(code)) {
            // 删除验证码，确保验证码不可以重复使用
            redisTemplate.opsForValue().getAndDelete(cacheKey);
            return true;
        }
        return false;
    }

    private Map<String, Object> createNewJwt(User user) {
        // 登录成功，生成 Token
        LoginUserInfo loginUserInfo = LoginUserInfo.builder().build();
        BeanUtils.copyProperties(user, loginUserInfo);
        // 生成 JWT Token，过期时间
        Map<String, Object> jwt = JWTUtil.createJwt(loginUserInfo);
        // 4、设置 RefreshToken 到 Redis 中，过期时间为 30 天
        String newRefreshToken = (String) jwt.get("RefreshToken");
        String key = KEY_PREFIX + newRefreshToken;
        redisTemplate.opsForValue().set(key, "1", REFRESH_EXPIRE, TimeUnit.MILLISECONDS);
        return jwt;
    }
}
