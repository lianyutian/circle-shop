package github.lianyutian.cshop.social.controller;

import github.lianyutian.cshop.common.model.ApiResult;
import github.lianyutian.cshop.social.model.vo.UserAttentionListVO;
import github.lianyutian.cshop.social.model.vo.UserFollowerListVO;
import github.lianyutian.cshop.social.service.UserRelationService;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

  /**
   * 获取用户关注列表
   *
   * @return ApiResult
   */
  @PostMapping("attention/list")
  public ApiResult<List<UserAttentionListVO>> getAttentionList(
      @RequestParam("userId") Long userId) {
    List<UserAttentionListVO> attentionList = userRelationService.getAttentionList(userId);
    return ApiResult.success(attentionList);
  }

  /**
   * 获取用户粉丝列表
   *
   * @return 用户粉丝列表
   */
  @PostMapping("follower/list")
  public ApiResult<List<UserFollowerListVO>> getFollowerList(@RequestParam("userId") Long userId) {
    List<UserFollowerListVO> attentionList = userRelationService.getFollowerList(userId);
    return ApiResult.success(attentionList);
  }
}
