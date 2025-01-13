package github.lianyutian.cshop.common.redis;

import github.lianyutian.cshop.common.utils.CommonUtil;
import github.lianyutian.cshop.common.utils.JsonUtil;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

/**
 * Redis缓存工具类
 *
 * @author lianyutian
 * @since 2024-12-27 11:30:30
 * @version 1.0
 */
@Component
@ConditionalOnBean(RedisConfig.class)
@Slf4j
public class RedisCache {

  private final RedisTemplate<String, String> redisTemplate;

  /** 缓存空数据 */
  public static final String EMPTY_CACHE = "{}";

  /** 分布式锁加锁时间 200 毫秒 */
  public static final long UPDATE_LOCK_TIMEOUT = 200;

  /** 一小时有效期 */
  public static final Integer ONE_HOUR_SECONDS = 60 * 60 * 1000;

  /** 两天有效期 */
  public static final Integer TWO_DAYS_SECONDS = 2 * 24 * 60 * 60 * 1000;

  /** 一天有效期 */
  public static final Integer ONE_DAY_SECONDS = 24 * 60 * 60 * 1000;

  public RedisCache(RedisTemplate<String, String> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  /**
   * 生成缓存过期时间：2天加上随机几小时，防止缓存雪崩，穿透数据库
   *
   * @return 返回过期时间
   */
  public static Integer generateCacheExpire() {
    return TWO_DAYS_SECONDS + CommonUtil.genRandomInt(0, 10) * 60 * 60 * 1000;
  }

  /**
   * 生成缓存穿透过期时间，单位 秒
   *
   * @return 随机 30 - 100 秒
   */
  public static Integer generateCachePenetrationExpire() {
    return CommonUtil.genRandomInt(30, 100) * 1000;
  }

  /**
   * 写入缓存
   *
   * @param key key
   * @param value value
   * @param timeOut 设置过期时间
   * @param timeUnit 时间类型
   */
  public void set(String key, String value, long timeOut, TimeUnit timeUnit) {
    ValueOperations<String, String> op = redisTemplate.opsForValue();
    try {
      if (timeOut > 0) {
        // 设置缓存时间
        op.set(key, value, timeOut, timeUnit);
      } else if (timeOut == 0) {
        // 永不过期
        op.set(key, value);
      } else {
        throw new IllegalArgumentException("Expiration time must be a non-negative integer");
      }
    } catch (Exception e) {
      log.error("写入缓存失败, key: {}, value: [REDACTED]", key, e);
      throw new RedisCacheException("写入缓存失败", e);
    }
  }

  /**
   * 写入缓存 value 为 Object 类型
   *
   * @param key key
   * @param value value
   * @param timeOut 过期时间
   * @param timeUnit 时间类型
   */
  public void set(String key, Object value, long timeOut, TimeUnit timeUnit) {
    try {
      // 先将 Object 转成 Json 字符串再进行存储
      this.set(key, JsonUtil.toJson(value), timeOut, timeUnit);
    } catch (Exception e) {
      log.error("写入缓存失败, key: {}, value: [REDACTED]", key, e);
      throw new RedisCacheException("写入缓存失败", e);
    }
  }

  /**
   * 读取缓存
   *
   * @param key key
   * @return 缓存数据
   */
  public String get(String key) {
    try {
      ValueOperations<String, String> op = redisTemplate.opsForValue();
      return op.get(key);
    } catch (Exception e) {
      log.error("读取缓存失败, key: {}", key, e);
      throw new RedisCacheException("读取缓存失败", e);
    }
  }

  /**
   * 删除缓存
   *
   * @param key key
   * @return 删除成功返回 true，否则返回 false
   */
  public boolean delete(String key) {
    try {
      return Boolean.TRUE.equals(redisTemplate.delete(key));
    } catch (Exception e) {
      log.error("删除缓存失败, key: {}", key, e);
      throw new RedisCacheException("删除缓存失败", e);
    }
  }

  /**
   * 设置缓存过期时间
   *
   * @param key key
   * @param timeOut 过期时间
   * @param timeUnit 时间类型
   */
  public void expire(String key, long timeOut, TimeUnit timeUnit) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("Key cannot be null or empty");
    }
    try {
      redisTemplate.expire(key, timeOut, timeUnit);
    } catch (Exception e) {
      log.error("Failed to set expiration for key: {}", key, e);
      throw new RedisCacheException("Failed to set expiration for key: " + key, e);
    }
  }

  /**
   * 获取缓存过期时间
   *
   * @param key key
   * @param timeUnit 时间类型
   * @return 过期时间
   */
  public Long getExpire(String key, TimeUnit timeUnit) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("Key cannot be null or empty");
    }
    try {
      return redisTemplate.getExpire(key, timeUnit);
    } catch (Exception e) {
      log.error("Failed to get expiration for key: {}", key, e);
      throw new RedisCacheException("Failed to get expiration for key: " + key, e);
    }
  }

  /**
   * 获取整数值
   *
   * @param key key
   * @return 缓存数据
   */
  public Long getLong(String key) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("Key cannot be null or empty");
    }
    try {
      ValueOperations<String, String> op = redisTemplate.opsForValue();
      String value = op.get(key);
      return value != null ? Long.parseLong(value) : null;
    } catch (NumberFormatException e) {
      log.error("Failed to parse integer value for key: {}", key, e);
      throw new RedisCacheException("Failed to parse integer value for key: " + key, e);
    } catch (Exception e) {
      log.error("Failed to get integer value for key: {}", key, e);
      throw new RedisCacheException("Failed to get integer value for key: " + key, e);
    }
  }

  /**
   * 增加缓存值
   *
   * @param key key
   * @param delta 增加的值
   */
  public void increment(String key, int delta) {
    try {
      ValueOperations<String, String> op = redisTemplate.opsForValue();
      op.increment(key, delta);
    } catch (Exception e) {
      log.error("Failed to increment value for key: {}", key, e);
      throw new RedisCacheException("Failed to increment value for key: " + key, e);
    }
  }
}

class RedisCacheException extends RuntimeException {
  public RedisCacheException(String message, Throwable cause) {
    super(message, cause);
  }
}
