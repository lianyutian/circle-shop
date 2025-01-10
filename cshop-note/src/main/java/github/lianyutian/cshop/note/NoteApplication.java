package github.lianyutian.cshop.note;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 笔记服务启动类
 *
 * @author lianyutian
 * @since 2025-01-02 14:55:33
 * @version 1.0
 */
@SpringBootApplication
@ComponentScan(basePackages = {"github.lianyutian.cshop.note", "github.lianyutian.cshop.common"})
public class NoteApplication {
  public static void main(String[] args) {
    SpringApplication.run(NoteApplication.class, args);
  }
}
