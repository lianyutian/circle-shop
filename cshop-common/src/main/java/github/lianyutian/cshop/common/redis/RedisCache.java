package github.lianyutian.cshop.common.redis;

import github.lianyutian.cshop.common.utils.CommonUtil;
import github.lianyutian.cshop.common.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

/**
 * Redis缓存工具类
 *
 * @author lianyutian
 * @since 2024-12-27 11:30:30
 * @version 1.0
 */
@Configuration
@Slf4j
public class RedisCache {

    private final StringRedisTemplate redisTemplate;

    /**
     * 缓存空数据
     */
    public static final String EMPTY_CACHE = "{}";

    /**
     * 分布式锁加锁时间 200 毫秒
     */
    public static final long USER_UPDATE_LOCK_TIMEOUT = 200;

    /**
     * 两天有效期
     */
    public static final Integer TWO_DAYS_SECONDS = 2 * 24 * 60 * 60;

    /**
     * 一天有效期
     */
    public static final Integer ONE_DAYS_SECONDS = 24 * 60 * 60;

    /**
     * 1 小时
     */
    public static final Integer ONE_HOURS_SECONDS = 60 * 60;

    /**
     * 1 小时毫秒
     */
    public static final Integer ONE_HOURS_MILLS = 1000 * 60 * 60;

    public RedisCache(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 生成缓存过期时间：2天加上随机几小时，防止缓存雪崩，穿透数据库
     *
     * @return 返回过期时间
     */
    public static Integer generateCacheExpire() {
        return TWO_DAYS_SECONDS + CommonUtil.genRandomInt(0, 10) * 60 * 60;
    }

    /**
     * 写入缓存
     *
     * @param key key
     * @param value value
     * @param seconds 设置过期时间
     */
    public void set(String key, String value, int seconds) {
        ValueOperations<String, String> op = redisTemplate.opsForValue();
        try {
            if (seconds > 0) {
                // 设置缓存时间
                op.set(key, value, seconds, TimeUnit.SECONDS);
            } else {
                // 永不过期
                op.set(key, value);
            }
        } catch (Exception e) {
            log.error("写入缓存失败, key: {}, value: {}", key, value, e);
            throw new RuntimeException("写入缓存失败", e);
        }
    }

    /**
     * 写入缓存 value 为 Object 类型
     *
     * @param key key
     * @param value value
     * @param seconds 过期时间
     */
    public void set(String key, Object value, int seconds) {
        try {
            // 先将 Object 转成 Json 字符串再进行存储
            this.set(key, JsonUtil.toJson(value), seconds);
        } catch (Exception e) {
            log.error("写入缓存失败, key: {}, value: {}", key, value, e);
            throw new RuntimeException("写入缓存失败", e);
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
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("读取缓存失败, key: {}", key, e);
            throw new RuntimeException("读取缓存失败", e);
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
            throw new RuntimeException("删除缓存失败", e);
        }
    }
}
