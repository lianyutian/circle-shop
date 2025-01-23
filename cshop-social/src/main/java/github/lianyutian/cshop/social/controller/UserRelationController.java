package github.lianyutian.cshop.social.controller;

import github.lianyutian.cshop.common.model.ApiResult;
import github.lianyutian.cshop.social.service.UserRelationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * UserRelationController
 *
 * @author lianyutian
 * @since 2025-01-17 08:59:09
 * @version 1.0
 */
@RestController
@RequestMapping("/api/social/v1")
@Slf4j
@AllArgsConstructor
public class UserRelationController {

  private final UserRelationService userRelationService;

  /**
   * 用户关注
   *
   * @param attentionUserId 关注博主id
   * @return ApiResult
   */
  @GetMapping("attention/{attentionUserId}")
  public ApiResult<Void> doAttention(@PathVariable("attentionUserId") Long attentionUserId) {
    log.info("博主关注，attentionUserId:{}", attentionUserId);
    return userRelationService.doAttention(attentionUserId);
  }

  /**
   * 用户取关
   *
   * @param attentionUserId 取关博主id
   * @return ApiResult
   */
  @GetMapping("unAttention/{attentionUserId}")
  public ApiResult<Void> unAttention(@PathVariable("attentionUserId") Long attentionUserId) {
    log.info("博主取关，attentionUserId:{}", attentionUserId);
    return userRelationService.unAttention(attentionUserId);
  }
}
