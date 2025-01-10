package github.lianyutian.cshop.user.model.vo;

import lombok.Data;

/**
 * 用户展示信息VO 展示给他人看的信息
 *
 * @author lianyutian
 * @since 2024-12-27 17:11:22
 * @version 1.0
 */
@Data
public class UserShowVO {
  /** 用户id */
  private Long id;

  /** 用户名 */
  private String name;

  /** 用户头像 */
  private String avatar;
}
