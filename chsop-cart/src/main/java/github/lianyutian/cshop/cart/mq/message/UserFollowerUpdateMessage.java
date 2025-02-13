package github.lianyutian.cshop.cart.mq.message;

import lombok.Builder;
import lombok.Data;

/**
 * 用户粉丝更新消息
 *
 * @author lianyutian
 * @since 2025-01-22 09:24:06
 * @version 1.0
 */
@Data
@Builder
public class UserFollowerUpdateMessage {
  /** 用户id */
  private Long userId;

  /** 粉丝id */
  private Long followerId;

  /** 是否取关 */
  private Integer del;
}
