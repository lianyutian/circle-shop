package github.lianyutian.cshop.social.model.vo;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * 用户粉丝列表VO
 *
 * @author lianyutian
 * @since 2025-01-23 16:25:02
 * @version 1.0
 */
@Data
public class UserFollowerListVO {
  /** 粉丝 userId */
  @SerializedName("id")
  private Long userId;

  /** 粉丝的粉丝数 */
  @SerializedName("followerCount")
  private Integer userFollowerCnt;

  /** 粉丝的关注数 */
  @SerializedName("attentionCount")
  private Integer userAttentionCnt;

  /** 粉丝头像 */
  @SerializedName("avatar")
  private String userAvatar;

  /** 粉丝昵称 */
  @SerializedName("name")
  private String userName;
}
