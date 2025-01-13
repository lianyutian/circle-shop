package github.lianyutian.cshop.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import github.lianyutian.cshop.common.enums.BizCodeEnum;
import github.lianyutian.cshop.common.exception.BizException;
import github.lianyutian.cshop.common.interceptor.LoginInterceptor;
import github.lianyutian.cshop.common.model.ApiResult;
import github.lianyutian.cshop.common.model.LoginUserInfo;
import github.lianyutian.cshop.common.redis.RedisCache;
import github.lianyutian.cshop.common.redis.RedisLock;
import github.lianyutian.cshop.common.utils.BeanUtil;
import github.lianyutian.cshop.common.utils.JWTUtil;
import github.lianyutian.cshop.common.utils.JsonUtil;
import github.lianyutian.cshop.user.constant.UserCacheKeyConstant;
import github.lianyutian.cshop.user.mapper.UserMapper;
import github.lianyutian.cshop.user.model.param.UserEditParam;
import github.lianyutian.cshop.user.model.param.UserLoginParam;
import github.lianyutian.cshop.user.model.param.UserRegisterParam;
import github.lianyutian.cshop.user.model.po.User;
import github.lianyutian.cshop.user.model.vo.UserDetailVO;
import github.lianyutian.cshop.user.model.vo.UserShowVO;
import github.lianyutian.cshop.user.service.UserService;
import io.jsonwebtoken.Claims;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

  private final RedisCache redisCache;

  private final PasswordEncoder passwordEncoder;

  private final RedisLock redisLock;

  @Override
  @Transactional
  public ApiResult<Void> register(UserRegisterParam userRegisterParam) {
    // 1. 校验注册验证码是否正确
    boolean checked = checkCode(userRegisterParam.getPhone(), userRegisterParam.getCode());
    if (!checked) {
      return ApiResult.result(BizCodeEnum.USER_CODE_PHONE_ERROR);
    }
    // 1.2 通过手机号唯一索引实现唯一
    User user = BeanUtil.copy(userRegisterParam, User.class);
    // 密码加密
    String secretPwd = passwordEncoder.encode(userRegisterParam.getPassword());
    user.setPwd(secretPwd);
    try {
      userMapper.insert(user);
    } catch (DuplicateKeyException e) {
      log.error("用户微服务-注册模块-用户已存在 {}", userRegisterParam.getPhone());
      return ApiResult.result(BizCodeEnum.USER_ACCOUNT_EXIST);
    }
    return ApiResult.success();
  }

  @Override
  public ApiResult<Map<String, Object>> login(UserLoginParam userLoginParam) {
    // 根据手机号查询是否存在
    LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(User::getPhone, userLoginParam.getPhone());
    User user = userMapper.selectOne(queryWrapper);
    if (user == null) {
      // 未注册
      return ApiResult.result(BizCodeEnum.USER_ACCOUNT_PWD_ERROR);
    }

    // 校验密码
    if (passwordEncoder.matches(userLoginParam.getPassword(), user.getPwd())) {
      // 登录成功，生成 jwt
      Map<String, Object> jwt = createNewJwt(user);
      return ApiResult.success(jwt);
    } else {
      return ApiResult.result(BizCodeEnum.USER_ACCOUNT_PWD_ERROR);
    }
  }

  @Override
  public ApiResult<Map<String, Object>> refreshToken(String refreshToken, String accessToken) {
    String refreshTokenVal = redisCache.get(JWTUtil.KEY_PREFIX + refreshToken);
    // refreshToken 过期
    if (StringUtils.isBlank(refreshTokenVal)) {
      return ApiResult.result(BizCodeEnum.USER_REFRESH_TOKEN_EMPTY);
    }
    // 2、如果 refreshToken 存在，解密 accessToken
    Claims claims = JWTUtil.parserToken(accessToken);
    if (claims == null) {
      // 无法解密提示未登录
      return ApiResult.result(BizCodeEnum.USER_ACCOUNT_UNLOGIN);
    }
    // 3、如果可以解密 accessToken， 则重新生成 accessToken 等信息返回
    long userId = Long.parseLong(claims.get("id").toString());
    LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(User::getId, userId);
    User user = userMapper.selectOne(queryWrapper);
    if (user != null) {
      Map<String, Object> jwt = createNewJwt(user);
      // 删除旧的 refreshToken
      redisCache.delete(JWTUtil.KEY_PREFIX + refreshToken);
      return ApiResult.success(jwt);
    } else {
      // 无法解密提示未登录
      return ApiResult.result(BizCodeEnum.USER_ACCOUNT_UNLOGIN);
    }
  }

  @Override
  public UserDetailVO getUserDetail() {
    // 通过登录拦截器中 ThreadLocal 进行了用户信息传递
    LoginUserInfo loginUserInfo = LoginInterceptor.USER_THREAD_LOCAL.get();

    UserDetailVO userDetailVO = getUserDetailFromCache(loginUserInfo.getId());

    if (userDetailVO != null) {
      return userDetailVO;
    }

    // 不存在才去查库
    User user =
        userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getId, loginUserInfo.getId()));
    userDetailVO = BeanUtil.copy(user, UserDetailVO.class);

    redisCache.set(
        UserCacheKeyConstant.USER_DETAIL_KEY_PREFIX + loginUserInfo.getId(),
        userDetailVO,
        RedisCache.generateCacheExpire(),
        TimeUnit.MILLISECONDS);
    log.info("用户模块-缓存用户信息：{}", JsonUtil.toJson(user));

    return userDetailVO;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void updateUser(UserEditParam userEditParam) {
    LoginUserInfo loginUserInfo = LoginInterceptor.USER_THREAD_LOCAL.get();
    userEditParam.setId(loginUserInfo.getId());
    boolean locked = false;
    try {
      // 这里加分布式锁是为了保证高并发下有其他用户查询该用户信息时保证数据库和缓存的一致性
      locked =
          redisLock.lock(UserCacheKeyConstant.USER_UPDATE_LOCK_KEY_PREFIX + loginUserInfo.getId());

      if (!locked) {
        log.error("用户模块-修改用户信息：用户 {} 获取锁失败", loginUserInfo.getId());
        throw new BizException(BizCodeEnum.USER_DETAIL_UPDATE_FAIL);
      }
      // 先删掉缓存，防止出现数据库更新成功但是更新缓存失败，获取用户信息时一直都是缓存的数据
      redisCache.delete(UserCacheKeyConstant.USER_SHOW_KEY_PREFIX + loginUserInfo.getId());
      redisCache.delete(UserCacheKeyConstant.USER_DETAIL_KEY_PREFIX + loginUserInfo.getId());

      User user = BeanUtil.copy(userEditParam, User.class);
      int rows =
          userMapper.update(
              user, new LambdaUpdateWrapper<User>().eq(User::getId, loginUserInfo.getId()));
      log.info("用户模块-修改用户信息：rows={}，data={}", rows, userEditParam);

      User newUser =
          userMapper.selectOne(
              new LambdaQueryWrapper<User>().eq(User::getId, loginUserInfo.getId()));
      UserShowVO userShowVO = BeanUtil.copy(newUser, UserShowVO.class);

      // 这里存在数据库更新后，缓存更新失败的场景（这里不去强制保证数据库更新完成后一定更新成功缓存）
      redisCache.set(
          UserCacheKeyConstant.USER_SHOW_KEY_PREFIX + loginUserInfo.getId(),
          userShowVO,
          RedisCache.generateCacheExpire(),
          TimeUnit.MILLISECONDS);

      UserDetailVO userDetailVO = BeanUtil.copy(newUser, UserDetailVO.class);
      redisCache.set(
          UserCacheKeyConstant.USER_DETAIL_KEY_PREFIX + loginUserInfo.getId(),
          userDetailVO,
          RedisCache.generateCacheExpire(),
          TimeUnit.MILLISECONDS);
    } finally {
      if (locked) {
        redisLock.unlock(UserCacheKeyConstant.USER_UPDATE_LOCK_KEY_PREFIX + loginUserInfo.getId());
      }
    }
  }

  /**
   * 用户展示信息
   *
   * <p>1.假如 用户Z 发布了一篇内容突然爆火，同一时刻有上万用户来查看 用户Z 的个人信息，此时就会存在同一时刻会有超大并发流量进入该接口
   *
   * <p>2.如果有恶意用户大批量使用不存在的 用户id 来查询，就会造成缓存穿透，大量请求击穿的 DB 层。
   *
   * <p>3.如果 用户Z 在这个时刻修改了自己的信息，怎么保证其他用户查询到的是最新的信息，怎么保证缓存和数据库的数据一致性
   *
   * <p>场景：例如 线程A 来查询 用户Z 的信息，同时 线程B 修改 用户Z 信息且此时缓存中 用户Z 信息刚好过期。
   *
   * <p>那么此时 线程A 会去查数据库，这个时候 线程B 还没将 用户Z 修改后的信息保存到数据库中。
   *
   * <p>所以 线程A 拿到的是旧的数据，再假设此时 线程A 没抢到 CPU 资源阻塞住了。
   *
   * <p>这个时候 线程B 已经修改完自身信息并保存入数据库，同时会将新数据刷新到缓存中。
   *
   * <p>再执行完成这一步后，线程A 才继续执行，此时 线程A 拿到的还是旧的数据，也会去刷新缓存。
   *
   * <p>那么此时缓存中的数据是 线程A 在 线程B 更新前的旧数据，数据库中是 线程B 更新的新数据。
   *
   * <p>解决方案：更新用户接口也加 同 一把分布式锁，保证读写串行。用户更新自身信息是占比很少的写操作，这里考虑的是极端场景
   *
   * @param userId 用户id
   * @return 用户展示信息
   */
  @Override
  public UserShowVO getUserShow(Long userId) {
    UserShowVO userShowVO = getUserShowFromCache(userId);

    if (userShowVO != null) {
      return userShowVO;
    }

    // 2、缓存为空再从数据库中获取
    return getUserShowFromDB(userId);
  }

  private UserDetailVO getUserDetailFromCache(long userId) {
    String key = UserCacheKeyConstant.USER_DETAIL_KEY_PREFIX + userId;
    String userDetail = redisCache.get(key);
    log.info("用户模块-从缓存中获取用户信息：{}", userDetail);
    if (StringUtils.isNotBlank(userDetail)) {
      // 缓存延期
      redisCache.expire(key, RedisCache.generateCacheExpire(), TimeUnit.MILLISECONDS);
      return JsonUtil.fromJson(userDetail, UserDetailVO.class);
    }
    return null;
  }

  private UserShowVO getUserShowFromCache(long userId) {
    String key = UserCacheKeyConstant.USER_SHOW_KEY_PREFIX + userId;
    String userShow = redisCache.get(key);
    log.info("用户模块-从缓存中获取用户展示信息：{}", userShow);
    if (StringUtils.isNotBlank(userShow)) {
      // 缓存延期
      redisCache.expire(key, RedisCache.generateCacheExpire(), TimeUnit.MILLISECONDS);
      return JsonUtil.fromJson(userShow, UserShowVO.class);
    }
    return null;
  }

  private UserShowVO getUserShowFromDB(Long userId) {
    String userUpdateLockKey = UserCacheKeyConstant.USER_UPDATE_LOCK_KEY_PREFIX + userId;
    // 设置分布式锁
    boolean tryLocked = false;
    try {
      // TODO 解释
      tryLocked = redisLock.tryLock(userUpdateLockKey, RedisCache.UPDATE_LOCK_TIMEOUT);

      if (!tryLocked) {
        // 加锁超时
        /* 尝试加锁时间有 RedisCache.UPDATE_LOCK_TIMEOUT 有可能其他线程已经获取数据并写入缓存了所以这里再尝试去读下缓存 */
        UserShowVO userShowFromCache = getUserShowFromCache(userId);
        if (userShowFromCache != null) {
          return userShowFromCache;
        }
        log.warn("【getUserShowFromDB】用户缓存为空，查询用户信息获取锁失败 {}", userId);
        return null;
      }

      User user = userMapper.selectById(userId);
      if (user == null) {
        // 如果为空先在缓存中设置一个空值，防止缓存穿透（解决2）
        redisCache.set(
            UserCacheKeyConstant.USER_SHOW_KEY_PREFIX + userId,
            RedisCache.EMPTY_CACHE,
            RedisCache.generateCachePenetrationExpire(),
            TimeUnit.MILLISECONDS);
        return null;
      }
      UserShowVO userShowVO = BeanUtil.copy(user, UserShowVO.class);
      log.info("【getUserShowFromDB】用户缓存为空，从数据库获取用户信息 {}", JsonUtil.toJson(userShowVO));
      redisCache.set(
          UserCacheKeyConstant.USER_SHOW_KEY_PREFIX + userId,
          userShowVO,
          RedisCache.generateCacheExpire(),
          TimeUnit.MILLISECONDS);
      return userShowVO;
    } catch (InterruptedException e) {
      // 加锁失败
      // 这里再去尝试获取下缓存-双重检查
      UserShowVO userShowFromCache = getUserShowFromCache(userId);
      if (userShowFromCache != null) {
        return userShowFromCache;
      }
      log.error("【getUserShowFromDB】尝试加锁异常，异常信息：{}", e.getMessage(), e);
      throw new BizException(BizCodeEnum.USER_SHOW_LOCK_FAIL);
    } finally {
      if (tryLocked) {
        redisLock.unlock(userUpdateLockKey);
      }
    }
  }

  private boolean checkCode(String phone, String code) {
    // 先从缓存中获取验证码 key - cshop-user:register-captcha:电话
    String cacheKey = UserCacheKeyConstant.CAPTCHA_REGISTER_KEY_PREFIX + phone;
    String codeVal = redisCache.get(cacheKey);
    if (StringUtils.isBlank(codeVal)) {
      return false;
    }
    String[] parts = codeVal.split("_");
    String registerCode = parts[0];
    if (registerCode.equals(code)) {
      // 删除验证码，确保验证码不可以重复使用
      redisCache.delete(cacheKey);
      return true;
    }
    return false;
  }

  private Map<String, Object> createNewJwt(User user) {
    // 登录成功，生成 Token
    LoginUserInfo loginUserInfo = BeanUtil.copy(user, LoginUserInfo.class);
    BeanUtils.copyProperties(user, loginUserInfo);
    // 生成 JWT Token，过期时间
    Map<String, Object> jwt = JWTUtil.createJwt(loginUserInfo);
    // 4、设置 RefreshToken 到 Redis 中，过期时间为 30 天
    String newRefreshToken = (String) jwt.get("RefreshToken");
    String key = JWTUtil.KEY_PREFIX + newRefreshToken;
    redisCache.set(key, "1", JWTUtil.REFRESH_EXPIRE, TimeUnit.MILLISECONDS);
    return jwt;
  }
}
