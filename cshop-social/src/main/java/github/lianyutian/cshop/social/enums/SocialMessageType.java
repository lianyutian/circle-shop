package github.lianyutian.cshop.social.enums;

import lombok.Getter;

/**
 * 社交 MQ 消息类型枚举
 *
 * @author lianyutian
 * @since 2025-01-21 13:47:31
 * @version 1.0
 */
@Getter
public enum SocialMessageType {
  USER_ATTENTION("USER_ATTENTION", "用户关注消息"),
  USER_UN_ATTENTION("USER_UN_ATTENTION", "用户取关消息"),
  USER_FOLLOWER("USER_FOLLOWER", "粉丝关注消息"),
  USER_UN_FOLLOWER("USER_UN_FOLLOWER", "粉丝取关消息");

  private final String type;
  private final String message;

  SocialMessageType(String type, String message) {
    this.type = type;
    this.message = message;
  }
}
