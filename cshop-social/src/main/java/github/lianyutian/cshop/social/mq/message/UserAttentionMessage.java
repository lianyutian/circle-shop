package github.lianyutian.cshop.social.mq.message;

import java.io.Serializable;
import lombok.Builder;
import lombok.Data;

/**
 * 用户关注消息
 *
 * @author lianyutian
 * @since 2025-01-21 13:41:48
 * @version 1.0
 */
@Data
@Builder
public class UserAttentionMessage implements Serializable {
  private Long userId;
  private Long attentionId;
  private Integer del;
}
