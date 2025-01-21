package github.lianyutian.cshop.social;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 社交服务启动类
 *
 * @author lianyutian
 * @since 2025-01-14 09:49:05
 * @version 1.0
 */
@SpringBootApplication
@ComponentScan(basePackages = {"github.lianyutian.cshop.social", "github.lianyutian.cshop.common"})
public class SocialApplication {
  public static void main(String[] args) {
    SpringApplication.run(SocialApplication.class, args);
  }
}
