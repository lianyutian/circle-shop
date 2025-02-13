package github.lianyutian.cshop.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 购物车服务启动类
 *
 * @author lianyutian
 * @since 2025-02-12 15:41:27
 * @version 1.0
 */
@SpringBootApplication
@ComponentScan(basePackages = {"github.lianyutian.cshop.cart", "github.lianyutian.cshop.common"})
public class CartApplication {
  public static void main(String[] args) {
    SpringApplication.run(CartApplication.class, args);
  }
}
