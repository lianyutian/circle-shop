package github.lianyutian.cshop.common.redis;

import java.util.concurrent.TimeUnit;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * Redis分布式锁
 *
 * @author lianyutian
 * @since 2024-12-27 11:30:30
 * @version 1.0
 */
@Component
@ConditionalOnBean(RedissonClient.class)
public class RedisLock {

  /** 分布式锁加锁时间 200 毫秒 */
  public static final long UPDATE_LOCK_TIMEOUT = 200;

  private final RedissonClient redissonClient;

  public RedisLock(RedissonClient redissonClient) {
    this.redissonClient = redissonClient;
  }

  /**
   * redisson 互斥锁，等待 seconds 秒后自动失效
   *
   * @param key key
   * @param seconds 秒
   * @return 加锁结果
   */
  public boolean lock(String key, int seconds) {
    // 先获取分布式锁
    RLock rLock = redissonClient.getLock(key);
    // 如果已经加锁了返回 false
    if (rLock.isLocked()) {
      return false;
    }
    // 加锁，如果在指定时间内没释放锁，它会自动在底层把锁释放掉
    rLock.lock(seconds, TimeUnit.SECONDS);
    // 加锁成功返回 true
    return true;
  }

  /**
   * redisson 互斥锁，自动续期
   *
   * @param key key
   * @return 加锁结果
   */
  public boolean lock(String key) {
    // 先获取分布式锁
    RLock rLock = redissonClient.getLock(key);
    // 如果已经加锁了返回 false
    if (rLock.isLocked()) {
      return false;
    }
    // 加锁
    rLock.lock();
    // 加锁成功返回 true
    return true;
  }

  /**
   * 尝试加锁
   *
   * @param key key
   * @param timeout 超时时间
   * @return 加锁结果
   */
  public boolean tryLock(String key, long timeout) throws InterruptedException {
    // 先获取分布式锁
    RLock rLock = redissonClient.getLock(key);
    // 尝试加锁
    return rLock.tryLock(timeout, TimeUnit.MILLISECONDS);
  }

  /**
   * 阻塞加锁
   *
   * @param key key
   * @return 加锁结果
   */
  public boolean blockedLock(String key) {
    // 先获取分布式锁
    RLock rLock = redissonClient.getLock(key);
    // 阻塞加锁, 拿不到锁会一直等待
    rLock.lock();
    return true;
  }

  /**
   * 手动释放锁
   *
   * @param key key
   */
  public void unlock(String key) {
    // 先获取分布式锁
    RLock rLock = redissonClient.getLock(key);
    // 如果已锁定，则释放锁
    if (rLock.isLocked()) {
      rLock.unlock();
    }
  }
}
