package github.lianyutian.cshop.social.service.impl;

import github.lianyutian.cshop.common.enums.BizCodeEnum;
import github.lianyutian.cshop.common.interceptor.LoginInterceptor;
import github.lianyutian.cshop.common.model.ApiResult;
import github.lianyutian.cshop.common.model.LoginUserInfo;
import github.lianyutian.cshop.common.redis.RedisCache;
import github.lianyutian.cshop.common.utils.JsonUtil;
import github.lianyutian.cshop.social.constant.SocialCacheKeyConstant;
import github.lianyutian.cshop.social.constant.SocialMQConstant;
import github.lianyutian.cshop.social.enums.SocialMessageType;
import github.lianyutian.cshop.social.mapper.UserFollowerMapper;
import github.lianyutian.cshop.social.mq.message.UserAttentionMessage;
import github.lianyutian.cshop.social.mq.producer.SocialDefaultProducer;
import github.lianyutian.cshop.social.service.UserRelationService;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * UserRelationService实现类
 *
 * @author lianyutian
 * @since 2025-01-17 09:19:09
 * @version 1.0
 */
@Service
@Slf4j
@AllArgsConstructor
public class UserRelationServiceImpl implements UserRelationService {

  private final UserFollowerMapper userFollowerMapper;

  private final RedisCache redisCache;

  private final SocialDefaultProducer socialDefaultProducer;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public ApiResult<Void> doAttention(Long attentionUserId) {
    // 判断是否关注，关注了就取消关注，没有关注就关注
    LoginUserInfo loginUserInfo = LoginInterceptor.USER_THREAD_LOCAL.get();

    // 1、博主自己不能关注自己
    if (loginUserInfo.getId() == attentionUserId) {
      return ApiResult.result(BizCodeEnum.USER_ATTENTION_NOT_SELF);
    }

    // 判断下要关注的博主是否有关注过
    String key = SocialCacheKeyConstant.USER_ATTENTION_PREFIX + loginUserInfo.getId();
    // 已关注的用户列表
    List<String> attentionUserIdList = redisCache.listAll(key);

    if (!CollectionUtils.isEmpty(attentionUserIdList)
        && attentionUserIdList.contains(String.valueOf(attentionUserId))) {
      log.info("博主: {} 关注，其他被关注博主 id: {}, 已经存在列表中", loginUserInfo.getId(), attentionUserId);
      return ApiResult.result(BizCodeEnum.USER_ATTENTED);
    }

    // 没有关注，加入关注列表
    redisCache.pushToList(key, String.valueOf(attentionUserId));
    publishAttentionEvent(loginUserInfo.getId(), attentionUserId);
    return ApiResult.result(BizCodeEnum.USER_ATTENTION_SUCCESS);
  }

  private void publishAttentionEvent(long id, Long attentionUserId) {
    UserAttentionMessage userAttentionMessage =
        UserAttentionMessage.builder().userId(id).attentionId(attentionUserId).del(0).build();

    socialDefaultProducer.sendMessage(
        SocialMQConstant.ATTENTION_TOPIC,
        SocialMessageType.USER_ATTENTION.getType(),
        JsonUtil.toJson(userAttentionMessage),
        SocialMessageType.USER_ATTENTION);
  }
}
