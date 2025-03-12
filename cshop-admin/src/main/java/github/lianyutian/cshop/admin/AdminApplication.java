package github.lianyutian.cshop.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 后台管理应用启动类
 *
 * @author lianyutian
 * @since 2025/3/5
 * @version 1.0
 */
@SpringBootApplication
@ComponentScan(basePackages = {"github.lianyutian.cshop.admin", "github.lianyutian.cshop.common"})
public class AdminApplication {
  public static void main(String[] args) {
    SpringApplication.run(AdminApplication.class, args);
  }
}
