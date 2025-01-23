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
import github.lianyutian.cshop.social.mq.message.UserAttentionUpdateMessage;
import github.lianyutian.cshop.social.mq.message.UserFollowerUpdateMessage;
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
    String attentionKey = SocialCacheKeyConstant.USER_ATTENTION_PREFIX + loginUserInfo.getId();
    // 已关注的用户列表
    List<String> attentionUserIdList = redisCache.listAll(attentionKey);

    if (!CollectionUtils.isEmpty(attentionUserIdList)
        && attentionUserIdList.contains(String.valueOf(attentionUserId))) {
      log.info(
          "用户id: {} 关注博主id: {}, 博主id: {} 已经存在用户关注列表中",
          loginUserInfo.getId(),
          attentionUserId,
          attentionUserId);
      return ApiResult.result(BizCodeEnum.USER_ATTENTED);
    }

    // 没有关注，加入关注列表
    log.info(
        "用户id: {} 关注博主id: {}, 博主id: {} 被添加到用户关注列表中",
        loginUserInfo.getId(),
        attentionUserId,
        attentionUserId);
    redisCache.pushToList(attentionKey, String.valueOf(attentionUserId));
    publishAttentionEvent(loginUserInfo.getId(), attentionUserId);

    // 添加用户为被关注用户的粉丝
    String followerKey = SocialCacheKeyConstant.USER_FOLLOWER_PREFIX + attentionUserId;
    log.info(
        "博主id: {} 被用户id: {} 关注, 用户id: {} 被添加到博主粉丝列表中",
        attentionUserId,
        loginUserInfo.getId(),
        loginUserInfo.getId());
    redisCache.pushToList(followerKey, String.valueOf(loginUserInfo.getId()));
    publishFollowerEvent(loginUserInfo.getId(), attentionUserId);
    return ApiResult.result(BizCodeEnum.USER_ATTENTION_SUCCESS);
  }

  private void publishFollowerEvent(Long followerId, Long attentionUserId) {
    UserFollowerUpdateMessage userFollowerUpdateMessage =
        UserFollowerUpdateMessage.builder()
            .userId(attentionUserId)
            .followerId(followerId)
            .del(0)
            .build();
    socialDefaultProducer.sendMessage(
        SocialMQConstant.FOLLOWER_TOPIC,
        SocialMessageType.USER_FOLLOWER.getType(),
        JsonUtil.toJson(userFollowerUpdateMessage),
        SocialMessageType.USER_FOLLOWER);
  }

  @Override
  public ApiResult<Void> unAttention(Long attentionUserId) {
    LoginUserInfo loginUserInfo = LoginInterceptor.USER_THREAD_LOCAL.get();
    // 不能取关自己
    if (loginUserInfo.getId() == attentionUserId) {
      return ApiResult.result(BizCodeEnum.USER_UN_ATTENTION_NOT_SELF);
    }
    // 判断是否关注，关注了就取消关注
    String key = SocialCacheKeyConstant.USER_ATTENTION_PREFIX + loginUserInfo.getId();
    List<String> attentionUserIdList = redisCache.listAll(key);
    if (CollectionUtils.isEmpty(attentionUserIdList)
        || !attentionUserIdList.contains(String.valueOf(attentionUserId))) {
      log.info(
          "用户id: {} 取关博主id: {}, 博主id: {} 不存在用户关注列表中",
          loginUserInfo.getId(),
          attentionUserId,
          attentionUserId);
      return ApiResult.result(BizCodeEnum.USER_UN_ATTENTED);
    }

    // 发布用户取关事件
    log.info(
        "用户id: {} 取关博主id: {}, 博主id: {} 从用户关注列表中移除",
        loginUserInfo.getId(),
        attentionUserId,
        attentionUserId);
    redisCache.removeValueFromList(key, String.valueOf(attentionUserId));
    publishUnAttentionEvent(loginUserInfo.getId(), attentionUserId);

    // 发布粉丝取关事件
    String followerKey = SocialCacheKeyConstant.USER_FOLLOWER_PREFIX + attentionUserId;
    log.info(
        "博主id: {} 被用户id: {} 取关, 用户id: {} 从博主粉丝列表中移除",
        attentionUserId,
        loginUserInfo.getId(),
        loginUserInfo.getId());
    redisCache.removeValueFromList(followerKey, String.valueOf(loginUserInfo.getId()));
    publishUnFollowerEvent(loginUserInfo.getId(), attentionUserId);
    return ApiResult.result(BizCodeEnum.USER_UN_ATTENTION_SUCCESS);
  }

  private void publishUnFollowerEvent(Long userId, Long attentionUserId) {
    UserFollowerUpdateMessage userFollowerUpdateMessage =
        UserFollowerUpdateMessage.builder()
            .userId(attentionUserId)
            .followerId(userId)
            .del(1)
            .build();
    socialDefaultProducer.sendMessage(
        SocialMQConstant.FOLLOWER_TOPIC,
        SocialMessageType.USER_UN_FOLLOWER.getType(),
        JsonUtil.toJson(userFollowerUpdateMessage),
        SocialMessageType.USER_UN_FOLLOWER);
  }

  private void publishUnAttentionEvent(Long userId, Long attentionUserId) {
    UserAttentionUpdateMessage userAttentionUpdateMessage =
        UserAttentionUpdateMessage.builder()
            .userId(userId)
            .attentionId(attentionUserId)
            .del(1)
            .build();
    socialDefaultProducer.sendMessage(
        SocialMQConstant.ATTENTION_TOPIC,
        SocialMessageType.USER_UN_ATTENTION.getType(),
        JsonUtil.toJson(userAttentionUpdateMessage),
        SocialMessageType.USER_UN_ATTENTION);
  }

  private void publishAttentionEvent(Long userId, Long attentionUserId) {
    UserAttentionUpdateMessage userAttentionUpdateMessage =
        UserAttentionUpdateMessage.builder()
            .userId(userId)
            .attentionId(attentionUserId)
            .del(0)
            .build();
    socialDefaultProducer.sendMessage(
        SocialMQConstant.ATTENTION_TOPIC,
        SocialMessageType.USER_ATTENTION.getType(),
        JsonUtil.toJson(userAttentionUpdateMessage),
        SocialMessageType.USER_ATTENTION);
  }
}
