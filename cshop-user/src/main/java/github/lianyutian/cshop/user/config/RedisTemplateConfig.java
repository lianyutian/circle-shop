package github.lianyutian.cshop.user.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * redis客户端配置
 *
 * @author lianyutian
 * @since 2024-12-17 14:36:40
 * @version 1.0
 */
@Configuration
public class RedisTemplateConfig {

    /**
     * 配置 RedisTemplate 用于操作 Redis 数据库
     *
     * @param redisConnectionFactory Redis 连接工厂，用于创建 Redis 连接
     * @return 配置好的 RedisTemplate 实例
     */
    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {

        // 实例化 RedisTemplate
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        // 设置连接
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 配置 Redis 序列化规则
        // 使用 Jackson 作为 JSON 处理库。该序列化器可以用于将 Java 对象存储到 Redis 中时，自动转换为 JSON 字符串
        ObjectMapper objectMapper = new ObjectMapper();
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);

        // 设置 ObjectMapper 的可见性，以便它可以序列化和反序列化所有访问级别的属性
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        // 设置 key-value 序列化规则
        // 使用 StringRedisSerializer 序列化 key，以确保 key 是字符串形式存储
        // 使用 Jackson2JsonRedisSerializer 序列化 value，将 Java 对象转换为 JSON 字符串存储
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);

        // 设置 hash-value 序列化规则
        // 同样地，使用 StringRedisSerializer 序列化 hash 的 key，使用 Jackson2JsonRedisSerializer 序列化 hash 的 value
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);

        return redisTemplate;
    }
}
