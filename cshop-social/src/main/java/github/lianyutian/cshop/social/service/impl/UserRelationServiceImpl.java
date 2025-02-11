package github.lianyutian.cshop.social.service.impl;

import github.lianyutian.cshop.common.enums.BizCodeEnum;
import github.lianyutian.cshop.common.interceptor.LoginInterceptor;
import github.lianyutian.cshop.common.model.ApiResult;
import github.lianyutian.cshop.common.model.LoginUserInfo;
import github.lianyutian.cshop.common.redis.RedisCache;
import github.lianyutian.cshop.common.redis.RedisLock;
import github.lianyutian.cshop.common.utils.JsonUtil;
import github.lianyutian.cshop.social.constant.SocialCacheKeyConstant;
import github.lianyutian.cshop.social.constant.SocialMQConstant;
import github.lianyutian.cshop.social.enums.SocialMessageType;
import github.lianyutian.cshop.social.mapper.UserAttentionMapper;
import github.lianyutian.cshop.social.mapper.UserFollowerMapper;
import github.lianyutian.cshop.social.model.vo.UserAttentionListVO;
import github.lianyutian.cshop.social.model.vo.UserFollowerListVO;
import github.lianyutian.cshop.social.mq.message.UserAttentionUpdateMessage;
import github.lianyutian.cshop.social.mq.message.UserFollowerUpdateMessage;
import github.lianyutian.cshop.social.mq.producer.SocialDefaultProducer;
import github.lianyutian.cshop.social.service.UserRelationService;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
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
  private static final int PAGE_SIZE = 10;

  private final RedisCache redisCache;
  private final RedisLock redisLock;

  private final SocialDefaultProducer socialDefaultProducer;
  private final UserAttentionMapper userAttentionMapper;
  private final UserFollowerMapper userFollowerMapper;
  private final StringRedisTemplate stringRedisTemplate;

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
    Boolean isExist = redisCache.isMemberOfZSet(attentionKey, String.valueOf(attentionUserId));

    if (isExist) {
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
    redisCache.addToZSet(attentionKey, String.valueOf(attentionUserId));
    publishAttentionEvent(loginUserInfo.getId(), attentionUserId);

    // 添加用户为被关注用户的粉丝
    String followerKey = SocialCacheKeyConstant.USER_FOLLOWER_PREFIX + attentionUserId;
    log.info(
        "博主id: {} 被用户id: {} 关注, 用户id: {} 被添加到博主粉丝列表中",
        attentionUserId,
        loginUserInfo.getId(),
        loginUserInfo.getId());
    redisCache.addToZSet(followerKey, String.valueOf(loginUserInfo.getId()));
    publishFollowerEvent(loginUserInfo.getId(), attentionUserId);
    return ApiResult.result(BizCodeEnum.USER_ATTENTION_SUCCESS);
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
    Boolean isExist = redisCache.isMemberOfZSet(key, String.valueOf(attentionUserId));
    if (!isExist) {
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
    redisCache.removeValueFromZSet(key, String.valueOf(attentionUserId));
    publishUnAttentionEvent(loginUserInfo.getId(), attentionUserId);

    // 发布粉丝取关事件
    String followerKey = SocialCacheKeyConstant.USER_FOLLOWER_PREFIX + attentionUserId;
    log.info(
        "博主id: {} 被用户id: {} 取关, 用户id: {} 从博主粉丝列表中移除",
        attentionUserId,
        loginUserInfo.getId(),
        loginUserInfo.getId());
    redisCache.removeValueFromZSet(followerKey, String.valueOf(loginUserInfo.getId()));
    publishUnFollowerEvent(loginUserInfo.getId(), attentionUserId);
    return ApiResult.result(BizCodeEnum.USER_UN_ATTENTION_SUCCESS);
  }

  @Override
  public Set<UserAttentionListVO> getAttentionList(Long userId, Integer start) {
    // 从缓存获取关注列表
    Set<UserAttentionListVO> attentionListFromCache =
        getAttentionListFromCache(userId, start, start + PAGE_SIZE);
    if (!CollectionUtils.isEmpty(attentionListFromCache)) {
      return attentionListFromCache;
    }
    // 从数据库获取关注列表
    return getAttentionListFromDB(userId, start, PAGE_SIZE);
  }

  @Override
  public Set<UserFollowerListVO> getFollowerList(Long userId, Integer start) {
    // 从缓存获取粉丝列表
    Set<UserFollowerListVO> followerListFromCache =
        getFollowerListFromCache(userId, start, start + PAGE_SIZE);
    if (!CollectionUtils.isEmpty(followerListFromCache)) {
      return followerListFromCache;
    }
    // 从数据库获取粉丝列表
    return getFollowerListFromDB(userId, start, PAGE_SIZE);
  }

  @Override
  public Boolean isFollower(Long attentionUserId) {
    /*
    直接使用 redis 判断是否关注。
    允许 redis 中数据和 DB 数据有差异
    1. 一般情况下，用户关注后会往 redis 里写入关注数据
    2. 这里不考虑极端场景，如 redis 中数据丢失
     */
    LoginUserInfo loginUserInfo = LoginInterceptor.USER_THREAD_LOCAL.get();
    String key = SocialCacheKeyConstant.USER_FOLLOWER_PREFIX + attentionUserId;
    Boolean isExist = redisCache.isMemberOfZSet(key, String.valueOf(loginUserInfo.getId()));
    if (isExist) {
      log.info("用户id: {} 是博主id: {} 的粉丝", loginUserInfo.getId(), attentionUserId);
      return true;
    }
    return false;
  }

  private Set<UserFollowerListVO> getFollowerListFromDB(long id, int offset, int limit) {
    log.info("从数据库获取用户id: {} 粉丝列表", id);
    List<Long> attentionIdList = userFollowerMapper.selectFollowerIdListByUserId(id, offset, limit);

    if (CollectionUtils.isEmpty(attentionIdList)) {
      return null;
    }
    return getUserFollowerListVOS(
        attentionIdList.stream().map(String::valueOf).collect(Collectors.toSet()));
  }

  private Set<UserFollowerListVO> getFollowerListFromCache(long id, int start, int end) {
    log.info("从缓存获取用户id: {} 粉丝列表", id);
    String key = SocialCacheKeyConstant.USER_FOLLOWER_PREFIX + id;
    Set<String> followerUserIdList = redisCache.rangeZSet(key, start, end);
    if (CollectionUtils.isEmpty(followerUserIdList)) {
      return null;
    }

    return getUserFollowerListVOS(followerUserIdList);
  }

  private Set<UserFollowerListVO> getUserFollowerListVOS(Set<String> followerUserIdList) {
    List<String> keys =
        followerUserIdList.stream()
            .map(attentionUserId -> SocialCacheKeyConstant.USER_SHOW_KEY_PREFIX + attentionUserId)
            .toList();

    List<Object> objects = redisCache.batchGet(keys);

    if (CollectionUtils.isEmpty(objects)) {
      // TODO 从用户服务获取
      return null;
    }

    return objects.stream()
        .map(
            object -> {
              if (object != null) {
                return JsonUtil.fromJson(object.toString(), UserFollowerListVO.class);
              }
              return null;
            })
        .collect(Collectors.toSet());
  }

  private Set<UserAttentionListVO> getAttentionListFromDB(long id, int offset, int limit) {
    log.info("从数据库获取用户id: {} 关注列表", id);
    List<Long> attentionIdList =
        userAttentionMapper.selectAttentionIdListByUserId(id, offset, limit);

    if (CollectionUtils.isEmpty(attentionIdList)) {
      return null;
    }
    return getUserAttentionListVOS(
        attentionIdList.stream().map(String::valueOf).collect(Collectors.toSet()));
  }

  private Set<UserAttentionListVO> getAttentionListFromCache(long id, Integer start, Integer end) {
    log.info("从缓存获取用户id: {} 关注列表", id);
    String key = SocialCacheKeyConstant.USER_ATTENTION_PREFIX + id;
    Set<String> attentionUserIdList = redisCache.rangeZSet(key, start, end);
    if (CollectionUtils.isEmpty(attentionUserIdList)) {
      return null;
    }

    return getUserAttentionListVOS(attentionUserIdList);
  }

  private Set<UserAttentionListVO> getUserAttentionListVOS(Set<String> attentionUserIdList) {
    List<String> keys =
        attentionUserIdList.stream()
            .map(attentionUserId -> SocialCacheKeyConstant.USER_SHOW_KEY_PREFIX + attentionUserId)
            .toList();

    List<Object> objects = redisCache.batchGet(keys);

    if (objects.contains(null)) {
      // TODO 从用户服务获取
      return null;
    }

    return objects.stream()
        .map(
            object -> {
              if (object != null) {
                return JsonUtil.fromJson(object.toString(), UserAttentionListVO.class);
              }
              return null;
            })
        .collect(Collectors.toSet());
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
