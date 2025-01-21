package github.lianyutian.cshop.social.service;

import github.lianyutian.cshop.common.model.ApiResult;

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
}
