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
   * 博主关注/取关
   *
   * @param attentionUserId 关注用户id
   * @return
   */
  @GetMapping("attention/{attentionUserId}")
  public ApiResult<Void> doAttention(@PathVariable("attentionUserId") Long attentionUserId) {
    log.info("博主关注，attentionUserId:{}", attentionUserId);
    return userRelationService.doAttention(attentionUserId);
  }
}
