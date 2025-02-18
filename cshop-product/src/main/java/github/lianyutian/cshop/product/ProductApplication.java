package github.lianyutian.cshop.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * ${description}
 *
 * @author lianyutian
 * @since 2025-02-18 14:10:09
 * @version 1.0
 */
@SpringBootApplication
@ComponentScan(basePackages = {"github.lianyutian.cshop.product", "github.lianyutian.cshop.common"})
public class ProductApplication {
  public static void main(String[] args) {
    SpringApplication.run(ProductApplication.class, args);
  }
}
