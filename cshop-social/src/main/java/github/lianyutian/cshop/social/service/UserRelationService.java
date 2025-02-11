package github.lianyutian.cshop.social.service;

import github.lianyutian.cshop.common.model.ApiResult;
import github.lianyutian.cshop.social.model.vo.UserAttentionListVO;
import github.lianyutian.cshop.social.model.vo.UserFollowerListVO;
import java.util.Set;

/**
 * UserRelationService
 *
 * @author lianyutian
 * @since 2025-01-14 09:49:05
 * @version 1.0
 */
public interface UserRelationService {
  /**
   * 关注/取关
   *
   * @param attentionUserId 关注用户id
   * @return ApiResult
   */
  ApiResult<Void> doAttention(Long attentionUserId);

  /**
   * 取关
   *
   * @param attentionUserId 关注用户id
   * @return ApiResult
   */
  ApiResult<Void> unAttention(Long attentionUserId);

  /**
   * 获取关注列表
   *
   * @param userId 用户id
   * @param start 开始位置
   * @return 关注列表
   */
  Set<UserAttentionListVO> getAttentionList(Long userId, Integer start);

  /**
   * 获取粉丝列表
   *
   * @param userId 用户id
   * @param start 开始位置
   * @return 粉丝列表
   */
  Set<UserFollowerListVO> getFollowerList(Long userId, Integer start);
}
