package github.lianyutian.cshop.common.mq;

import lombok.Data;
import org.apache.rocketmq.acl.common.AclClientRPCHook;
import org.apache.rocketmq.acl.common.SessionCredentials;
import org.apache.rocketmq.remoting.RPCHook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * rocketmq配置类
 *
 * @author lianyutian
 * @since 2025-01-15 10:26:51
 * @version 1.0
 */
@Data
@Configuration
@ConditionalOnProperty(prefix = "rocketmq", value = "nameServer")
public class RocketMQConfig {
  @Value("${rocketmq.nameServer}")
  private String nameServer;

  @Value("${rocketmq.accessKey}")
  private String accessKey;

  @Value("${rocketmq.secretKey}")
  private String secretKey;

  /**
   * 鉴权 hook
   *
   * @return RPCHook
   */
  @Bean
  public RPCHook rpcHook() {
    return new AclClientRPCHook(new SessionCredentials(accessKey, secretKey));
  }
}
