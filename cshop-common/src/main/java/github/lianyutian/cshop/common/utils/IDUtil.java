package github.lianyutian.cshop.common.utils;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 分布式id生成
 *
 * @author lianyutian
 * @since 2025-01-23 10:38:58
 * @version 1.0
 */
@Slf4j
public class IDUtil {

  private static final Snowflake SNOWFLAKE;

  // 从配置文件或环境变量中读取工作机器ID和数据中心ID
  private static final long WORKER_ID =
      Long.parseLong(System.getenv("WORKER_ID") != null ? System.getenv("WORKER_ID") : "1");
  private static final long DATACENTER_ID =
      Long.parseLong(System.getenv("DATACENTER_ID") != null ? System.getenv("DATACENTER_ID") : "1");

  static {
    try {
      SNOWFLAKE = IdUtil.getSnowflake(WORKER_ID, DATACENTER_ID);
    } catch (Exception e) {
      log.error("初始化 Snowflake 失败", e);
      throw new RuntimeException("初始化 Snowflake 失败", e);
    }
  }

  /**
   * 生成分布式id
   *
   * @return id
   */
  public static Long getId() {
    try {
      return SNOWFLAKE.nextId();
    } catch (Exception e) {
      log.error("生成ID失败", e);
      throw new RuntimeException("生成ID失败", e);
    }
  }
}
