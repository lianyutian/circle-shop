package github.lianyutian.cshop.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import github.lianyutian.cshop.common.enums.BizCodeEnums;
import github.lianyutian.cshop.common.exception.BizException;
import github.lianyutian.cshop.common.interceptor.LoginInterceptor;
import github.lianyutian.cshop.common.model.LoginUserInfo;
import github.lianyutian.cshop.common.redis.RedisCache;
import github.lianyutian.cshop.common.redis.RedisLock;
import github.lianyutian.cshop.common.utils.ApiResult;
import github.lianyutian.cshop.common.utils.JWTUtil;
import github.lianyutian.cshop.common.utils.JsonUtil;
import github.lianyutian.cshop.user.constant.CacheKeyConstant;
import github.lianyutian.cshop.user.mapper.UserMapper;
import github.lianyutian.cshop.user.model.param.UserEditParam;
import github.lianyutian.cshop.user.model.po.User;
import github.lianyutian.cshop.user.model.vo.UserDetailVO;
import github.lianyutian.cshop.user.model.param.UserLoginParam;
import github.lianyutian.cshop.user.model.param.UserRegisterParam;
import github.lianyutian.cshop.user.model.vo.UserShowDetailVO;
import github.lianyutian.cshop.user.service.UserService;
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

    private final RedisCache redisCache;

    private final RedisLock redisLock;

    @Override
    @Transactional
    public ApiResult<Void> register(UserRegisterParam userRegisterVO) {
        // 1. 校验注册验证码是否正确
        boolean checked = checkCode(userRegisterVO.getPhone(), userRegisterVO.getCode());
        if (!checked) {
            return ApiResult.result(BizCodeEnums.USER_CODE_PHONE_ERROR);
        }
        // 1.2 通过手机号唯一索引实现唯一
        User user = new User();
        BeanUtils.copyProperties(userRegisterVO, user);
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
    public ApiResult<Map<String, Object>> login(UserLoginParam userLoginVO) {
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

    @Override
    public UserDetailVO getUserDetail() {
        // 通过登录拦截器中 ThreadLocal 进行了用户信息传递
        LoginUserInfo loginUserInfo = LoginInterceptor.USER_THREAD_LOCAL.get();

        UserDetailVO userDetailVO = getUserInfoFromCache(loginUserInfo.getId());

        if (userDetailVO != null) {
            return userDetailVO;
        }

        // 不存在才去查库
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getId, loginUserInfo.getId()));
        userDetailVO = new UserDetailVO();
        BeanUtils.copyProperties(user, userDetailVO);

        redisCache.set(CacheKeyConstant.USER_INFO_KEY_PREFIX + loginUserInfo.getId(),
                user,
                RedisCache.generateCacheExpire());
        log.info("用户模块-缓存用户信息：{}", JsonUtil.toJson(user));

        return userDetailVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserInfo(UserEditParam userEditParam) {
        LoginUserInfo loginUserInfo = LoginInterceptor.USER_THREAD_LOCAL.get();
        userEditParam.setId(loginUserInfo.getId());
        boolean locked = false;
        try {
            // 这里加分布式锁是为了保证高并发下有其他用户查询该用户信息时保证数据库和缓存的一致性
            locked = redisLock.lock(CacheKeyConstant.USER_UPDATE_LOCK_KEY_PREFIX + loginUserInfo.getId());

            if (!locked) {
                log.warn("用户模块-修改用户信息：用户 {} 获取锁失败", loginUserInfo.getId());
                throw new BizException(BizCodeEnums.USER_UPDATE_LOCK_FAIL);
            }
            // 先删掉缓存，防止出现数据库更新成功但是更新缓存失败，获取用户信息时一直都是缓存的数据
            redisCache.delete(CacheKeyConstant.USER_INFO_KEY_PREFIX + loginUserInfo.getId());

            User user = new User();
            BeanUtils.copyProperties(userEditParam, user);
            int rows = userMapper.update(user,
                    new LambdaUpdateWrapper<User>()
                            .eq(User::getId, loginUserInfo.getId())
            );
            log.info("用户模块-修改用户信息：rows={}，data={}", rows, userEditParam);
            User newUser = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getId, loginUserInfo.getId()));

            // 同一个用户同时调该接口去修改自身的信息这种场景几乎不会出现，不考虑并发问题
            // 这里存在数据库更新后，缓存更新失败的场景（这里不去强制保证数据库更新完成后一定更新成功缓存）
            redisCache.set(CacheKeyConstant.USER_INFO_KEY_PREFIX + loginUserInfo.getId(),
                    newUser,
                    RedisCache.generateCacheExpire()
            );
        } finally {
            if (locked) {
                redisLock.unlock(CacheKeyConstant.USER_UPDATE_LOCK_KEY_PREFIX + loginUserInfo.getId());
            }
        }
    }

    /**
     * 用户展示信息
     * <p>
     * 1.假如用户Z发布了一篇内容突然爆火，同一时刻有上万用户来查看用户Z的个人信息，此时就会存在同一时刻会有超大并发流量进入该接口
     * <p>
     * 2.如果有恶意用户大批量使用不存在的用户id来查询，就会造成缓存穿透，大量请求击穿的 DB 层。
     * <p>
     * 3.如果用户Z在这个时刻修改了自己的信息，怎么保证其他用户查询到的是最新的信息，怎么保证缓存和数据库的数据一致性
     * <p>
     *  - 例如用户A来查询用户Z的信息，同时用户Z修改自身信息
     *  场景：此时缓存中用户Z信息刚好过期，那么此时会去查数据库，这个时候用户Z还没将修改后的信息保存到数据库中所以用户A拿到的是旧的数据
     *        再假设此时执行用户A查询信息的线程没抢到CPU资源先阻塞住了，这个时候用户Z已经修改完自身信息并保存。这个时候会去数据中刷新
     *        自身数据并同步刷新到缓存中。再执行完成这一步后，用户A的线程才继续执行，此时用户A拿到的还是旧的数据，这个时候也会去刷新缓存
     *        那么此时缓存中的数据和数据库的数据就是不一致的。
     *  解决方案：更新用户接口也加 同 一把分布式锁，保证读写串行。用户更新自身信息是占比很少的写操作，这里考虑的是极端场景
     *
     *
     * @param userId 用户id
     * @return 用户展示信息
     */
    @Override
    public UserShowDetailVO getUserShowDetail(Long userId) {
        UserDetailVO userDetailVO = getUserInfoFromCache(userId);
        UserShowDetailVO userShowDetailVO = new UserShowDetailVO();
        if (userDetailVO != null) {
            BeanUtils.copyProperties(userDetailVO, userShowDetailVO);
            return userShowDetailVO;
        }

        // 2、缓存为空再从数据库中获取
        userDetailVO = getUserInfoFromDB(userId);
        if (userDetailVO != null) {
            BeanUtils.copyProperties(userDetailVO, userShowDetailVO);
            return userShowDetailVO;
        }

        return null;
    }

    private UserDetailVO getUserInfoFromCache(long userId) {
        String key = CacheKeyConstant.USER_INFO_KEY_PREFIX + userId;
        String userDetail = redisCache.get(key);
        log.info("用户模块-从缓存中获取用户展示信息：{}", userDetail);
        if (StringUtils.isNotBlank(userDetail)) {
            // 缓存延期
            redisCache.expire(key, RedisCache.generateCacheExpire());
            return JsonUtil.fromJson(userDetail, UserDetailVO.class);
        }
        return null;
    }

    private UserDetailVO getUserInfoFromDB(Long userId) {
        String userUpdateLockKey = CacheKeyConstant.USER_UPDATE_LOCK_KEY_PREFIX + userId;
        // 设置分布式锁
        boolean tryLocked;
        try {
            tryLocked = redisLock.tryLock(userUpdateLockKey, RedisCache.USER_UPDATE_LOCK_TIMEOUT);
        } catch (InterruptedException e) {
            // 加锁失败
            // 这里再去尝试获取下缓存-双重检查
            UserDetailVO userDetailVO = getUserInfoFromCache(userId);
            if (userDetailVO != null) {
                return userDetailVO;
            }
            log.error("【getUserInfoFromDB】尝试加锁异常，异常信息：{}", e.getMessage(), e);
            throw new BizException(BizCodeEnums.USER_INFO_LOCK_FAIL);
        }

        if (!tryLocked) {
            // 加锁超时
            // 这里再去尝试获取下缓存-双重检查
            UserDetailVO userDetailVO = getUserInfoFromCache(userId);
            if (userDetailVO != null) {
                return userDetailVO;
            }
            log.warn("【getUserInfoFromDB】用户缓存为空，查询用户信息获取锁失败 {}", userId);
            return null;
        }

        try {
            // 这里可能获取锁时已经有线程更新了缓存信息-双重检查
            UserDetailVO userDetailVO = getUserInfoFromCache(userId);
            if (userDetailVO != null) {
                return userDetailVO;
            }
            User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getId, userId));
            if (user == null) {
                // 如果为空先在缓存中设置一个空值，防止缓存穿透（解决2）
                redisCache.set(CacheKeyConstant.USER_INFO_KEY_PREFIX + userId,
                        RedisCache.EMPTY_CACHE,
                        RedisCache.generateCachePenetrationExpire());
                return null;
            }
            userDetailVO = new UserDetailVO();
            BeanUtils.copyProperties(user, userDetailVO);
            log.info("【getUserInfoFromDB】用户缓存为空，从数据库获取用户信息 {}", JsonUtil.toJson(user));
            redisCache.set(CacheKeyConstant.USER_INFO_KEY_PREFIX + userId,
                    user,
                    RedisCache.generateCacheExpire());
            return userDetailVO;
        } finally {
            redisLock.unlock(userUpdateLockKey);
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
