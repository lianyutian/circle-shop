package github.lianyutian.cshop.social.constant;

/**
 * 社交服务MQ常量类
 *
 * @author lianyutian
 * @since 2025-01-21 14:05:15
 * @version 1.0
 */
public class SocialMQConstant {
  /** 关系服务 producer group */
  public static final String RELATION_DEFAULT_PRODUCER_GROUP = "relation_default_producer_group";

  /** 粉丝关注/取关服务 consumer group */
  public static final String FOLLOWER_DEFAULT_CONSUMER_GROUP = "follower_default_consumer_group";

  /** 博主关注/取关服务 consumer group */
  public static final String ATTENTION_DEFAULT_CONSUMER_GROUP = "attention_default_consumer_group";

  /** 笔记点赞、收藏、评论计数 consumer group */
  public static final String NOTE_COUNTER_DEFAULT_CONSUMER_GROUP =
      "note_counter_default_consumer_group";

  /** 粉丝关注/取关 topic */
  public static final String FOLLOWER_TOPIC = "follower_topic";

  /** 博主关注/取关 topic */
  public static final String ATTENTION_TOPIC = "attention_topic";

  /** 笔记点赞、收藏、评论计数 topic */
  public static final String NOTE_COUNTER_TOPIC = "note_counter_topic";
}
