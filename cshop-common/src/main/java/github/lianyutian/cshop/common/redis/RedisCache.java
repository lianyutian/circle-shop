package github.lianyutian.cshop.common.redis;

import github.lianyutian.cshop.common.utils.CommonUtil;
import github.lianyutian.cshop.common.utils.JsonUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
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

  public static final String EMPTY_ARRAY_CACHE = "[]";

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
   */
  public void set(String key, String value) {
    ValueOperations<String, String> op = redisTemplate.opsForValue();
    try {
      // 永不过期
      op.set(key, value);
    } catch (Exception e) {
      log.error("写入缓存失败, key: {}, value: [REDACTED]", key, e);
      throw new RedisCacheException("写入缓存失败", e);
    }
  }

  /**
   * 写入缓存
   *
   * @param key key
   * @param value value
   * @param timeOut 设置过期时间
   */
  public void set(String key, String value, long timeOut) {
    ValueOperations<String, String> op = redisTemplate.opsForValue();
    try {
      if (timeOut > 0) {
        // 设置缓存时间
        op.set(key, value, timeOut, TimeUnit.MILLISECONDS);
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

  /**
   * 获取 key 缓存所有列表
   *
   * @param key key
   * @return 缓存列表
   */
  public List<String> listAll(String key) {
    try {
      ListOperations<String, String> listOperations = redisTemplate.opsForList();
      return listOperations.range(key, 0, -1);
    } catch (Exception e) {
      log.error("Failed to list all values for key: {}", key, e);
      throw new RedisCacheException("Failed to list all values for key: " + key, e);
    }
  }

  /**
   * 添加到列表
   *
   * @param key key
   * @param value value
   */
  public void pushToList(String key, String value) {
    try {
      ListOperations<String, String> listOperations = redisTemplate.opsForList();
      listOperations.rightPush(key, value);
    } catch (Exception e) {
      log.error("Failed to add value to list for key: {}", key, e);
      throw new RedisCacheException("Failed to add value to list for key: " + key, e);
    }
  }

  /**
   * 从列表中移除特定值
   *
   * <p>此方法旨在从Redis中指定键对应的列表中移除所有匹配给定值的元素
   *
   * <p>它使用RedisTemplate的ListOperations来执行移除操作，并处理可能发生的异常
   *
   * @param key Redis中列表的键
   * @param value 要从列表中移除的值
   */
  public void removeValueFromList(String key, String value) {
    try {
      // 获取RedisTemplate的ListOperations操作对象
      ListOperations<String, String> listOperations = redisTemplate.opsForList();
      // 使用remove方法从列表中移除匹配的值，0表示移除所有匹配项
      listOperations.remove(key, 0, value);
    } catch (Exception e) {
      // 记录错误日志，包括键、值和异常信息
      log.error("Failed to remove value {} from list for key: {}", value, key, e);
      // 抛出自定义异常，包装原始异常
      throw new RedisCacheException("Failed to remove value from list for key: " + key, e);
    }
  }

  /**
   * 执行 lua 脚本
   *
   * @param script lua 脚本
   * @param keys keys
   * @param args args
   */
  public void execute(DefaultRedisScript<Long> script, List<String> keys, Object... args) {
    try {
      redisTemplate.execute(script, keys, args);
    } catch (Exception e) {
      log.error("Failed to execute script for keys: {}", keys, e);
      throw new RedisCacheException("Failed to execute script for keys: " + keys, e);
    }
  }

  /**
   * 批量获取 Redis 键对应的值
   *
   * @param keys 要查询的键列表
   * @return 查询结果列表，按键顺序返回值；不存在的键返回 null
   */
  public List<Object> batchGet(List<String> keys) {
    try {
      // 使用 Pipeline 批量执行 GET 操作
      List<Object> results = new ArrayList<>(keys.size());

      redisTemplate
          .executePipelined(
              (RedisCallback<Object>)
                  connection -> {
                    for (String key : keys) {
                      byte[] rawKey = key.getBytes();
                      connection.stringCommands().get(rawKey);
                    }
                    return null; // 必须返回 null
                  })
          .forEach(
              result -> {
                if (result == null || result instanceof Throwable) {
                  results.add(null);
                } else {
                  results.add(result);
                }
              });
      return results;
    } catch (Exception e) {
      log.error(
          "Failed to batch get values for keys: {}. Error type: {}, Message: {}",
          keys,
          e.getClass().getName(),
          e.getMessage(),
          e);
      throw new RedisCacheException("Failed to batch get values for keys: " + keys, e);
    }
  }

  /**
   * 判断成员是否在集合中
   *
   * @param key key
   * @param value value
   * @return Boolean
   */
  public Boolean isMemberOfZSet(String key, String value) {
    try {
      ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
      return zSetOperations.rank(key, value) != null;
    } catch (Exception e) {
      log.error("Failed to check if member is in set for key: {}", key, e);
      throw new RedisCacheException("Failed to check if member is in set for key: " + key, e);
    }
  }

  /**
   * 添加到集合
   *
   * @param key key
   * @param value value
   */
  public void zAdd(String key, String value) {
    try {
      ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
      double score = System.currentTimeMillis();
      zSetOperations.add(key, value, score);
    } catch (Exception e) {
      log.error("Failed to add value to set for key: {}", key, e);
      throw new RedisCacheException("Failed to add value to set for key: " + key, e);
    }
  }

  /**
   * 添加到集合
   *
   * @param key key
   * @param value value
   */
  public void zAdd(String key, String value, double score) {
    try {
      ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
      zSetOperations.add(key, value, score);
    } catch (Exception e) {
      log.error("Failed to add value to set for key: {}", key, e);
      throw new RedisCacheException("Failed to add value to set for key: " + key, e);
    }
  }

  /**
   * 从集合中移除
   *
   * @param key key
   * @param value value
   */
  public void removeValueFromZSet(String key, String value) {
    try {
      ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
      zSetOperations.remove(key, value);
    } catch (Exception e) {
      log.error("Failed to remove value from set for key: {}", key, e);
      throw new RedisCacheException("Failed to remove value from set for key: " + key, e);
    }
  }

  /**
   * 获取 Set 集合
   *
   * @param key key
   * @return 集合
   */
  public Set<String> listZSet(String key) {
    try {
      ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
      return zSetOperations.range(key, 0, -1);
    } catch (Exception e) {
      log.error("Failed to get set for key: {}", key, e);
      throw new RedisCacheException("Failed to get set for key: " + key, e);
    }
  }

  /**
   * 获取 Set 集合
   *
   * @param key key
   * @param start start
   * @param end end
   * @return 集合
   */
  public Set<String> rangeZSet(String key, long start, long end) {
    try {
      ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
      return zSetOperations.range(key, start, end);
    } catch (Exception e) {
      log.error("Failed to get set for key: {}", key, e);
      throw new RedisCacheException("Failed to get set for key: " + key, e);
    }
  }

  /**
   * 获取集合大小
   *
   * @param key key
   * @return 集合大小
   */
  public Long getZSetSize(String key) {
    try {
      ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
      return zSetOperations.zCard(key);
    } catch (Exception e) {
      log.error("Failed to get set for key: {}", key, e);
      throw new RedisCacheException("Failed to get set for key: " + key, e);
    }
  }

  /**
   * 删除 zset 集合中的元素
   *
   * @param key key
   * @param member member
   */
  public void zRemove(String key, String member) {
    try {
      ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
      zSetOperations.remove(key, member);
    } catch (Exception e) {
      log.error("Failed to zRemove for key: {}, member: {}", key, member, e);
      throw new RedisCacheException("Failed to zRemove for key: " + key + "member: " + member, e);
    }
  }

  /**
   * 获取 Hash 集合大小
   *
   * @param key key
   * @return key对应的Hash集合大小
   */
  public Long getHashSize(String key) {
    try {
      HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
      return hashOperations.size(key);
    } catch (Exception e) {
      log.error("Failed to get set for key: {}", key, e);
      throw new RedisCacheException("Failed to get set for key: " + key, e);
    }
  }

  /**
   * 判断 Hash 集合中是否存在该字段
   *
   * @param key key
   * @param hashKey hashKey
   * @return Boolean
   */
  public Boolean isFiledExistOfHash(String key, String hashKey) {
    try {
      HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
      return hashOperations.hasKey(key, hashKey);
    } catch (Exception e) {
      log.error("Failed to get set for key: {}, hashKey: {}", key, hashKey, e);
      throw new RedisCacheException("Failed to get set for key: " + key + "hashKey: " + hashKey, e);
    }
  }

  /**
   * Hash 集合中获取值
   *
   * @param key key
   * @param hashKey hashKey
   * @return String
   */
  public String hGet(String key, String hashKey) {
    try {
      HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
      return hashOperations.get(key, hashKey);
    } catch (Exception e) {
      log.error("Failed to get set for key: {}, hashKey: {}", key, hashKey, e);
      throw new RedisCacheException("Failed to get set for key: " + key + "hashKey: " + hashKey, e);
    }
  }

  /**
   * Hash 集合中获取所有值
   *
   * @param key key
   * @return values
   */
  public List<String> hGetAll(String key) {
    try {
      HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
      return hashOperations.values(key);
    } catch (Exception e) {
      log.error("Failed to hGetAll for key: {}", key, e);
      throw new RedisCacheException("Failed to hGetAll for key: " + key, e);
    }
  }

  /**
   * Hash 集合中添加值
   *
   * @param key key
   * @param hashKey hashKey
   * @param value value
   */
  public void hPut(String key, String hashKey, String value) {
    try {
      HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
      hashOperations.put(key, hashKey, value);
    } catch (Exception e) {
      log.error("Failed to get set for key: {}, hashKey: {}", key, hashKey, e);
      throw new RedisCacheException("Failed to get set for key: " + key + "hashKey: " + hashKey, e);
    }
  }

  /**
   * 以 map 集合的形式添加 hash 键值对
   *
   * @param key key
   * @param map map
   */
  public void hPutAll(String key, Map<String, String> map) {
    try {
      HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
      hashOperations.putAll(key, map);
    } catch (Exception e) {
      log.error("Failed to hPutAll key: {}", key, e);
      throw new RedisCacheException("Failed to hPutAll for key: " + key, e);
    }
  }

  /**
   * Hash 集合中删除值
   *
   * @param key key
   * @param hashKey hashKey
   */
  public void hDel(String key, String hashKey) {
    try {
      HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
      hashOperations.delete(key, hashKey);
    } catch (Exception e) {
      log.error("Failed to get set for key: {}, hashKey: {}", key, hashKey, e);
      throw new RedisCacheException("Failed to get set for key: " + key + "hashKey: " + hashKey, e);
    }
  }

  /**
   * 删除 key
   *
   * @param key key
   */
  public void del(String key) {
    try {
      redisTemplate.delete(key);
    } catch (Exception e) {
      log.error("Failed to delete for key: {}", key, e);
      throw new RedisCacheException("Failed to delete for key: " + key, e);
    }
  }
}

class RedisCacheException extends RuntimeException {
  public RedisCacheException(String message, Throwable cause) {
    super(message, cause);
  }
}
