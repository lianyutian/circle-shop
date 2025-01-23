package github.lianyutian.cshop.social.model.vo;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * 用户关注列表
 *
 * @author lianyutian
 * @since 2025-01-23 11:20:12
 * @version 1.0
 */
@Data
public class UserAttentionListVO {
  /** 关注博主的用户id */
  @SerializedName("id")
  private Long userId;

  /** 关注博主的用户头像 */
  @SerializedName("avatar")
  private String userAvatar;

  /** 关注博主的用户昵称 */
  @SerializedName("name")
  private String userName;
}
