package github.lianyutian.cshop.common.utils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.lang.reflect.Type;
import lombok.extern.slf4j.Slf4j;

/**
 * Json工具类
 *
 * @author lianyutian
 * @since 2024-12-27 14:25:42
 * @version 1.0
 */
@Slf4j
public class JsonUtil {

  private static final Gson GSON = new Gson();

  /**
   * 将对象转换为JSON字符串
   *
   * @param object 需要转换的对象
   * @return JSON字符串
   * @throws IllegalArgumentException 如果输入对象为null
   */
  public static String toJson(Object object) {
    if (object == null) {
      throw new IllegalArgumentException("Input object cannot be null");
    }
    return GSON.toJson(object);
  }

  /**
   * 将JSON字符串转换为指定类型的对象
   *
   * @param json JSON字符串
   * @param clazz 目标类型
   * @param <T> 泛型类型
   * @return 转换后的对象
   * @throws IllegalArgumentException 如果输入JSON字符串为null或空
   * @throws JsonSyntaxException 如果JSON字符串格式不正确
   */
  public static <T> T fromJson(String json, Class<T> clazz) {
    if (json == null || json.trim().isEmpty()) {
      throw new IllegalArgumentException("Input JSON string cannot be null or empty");
    }
    try {
      return GSON.fromJson(json, clazz);
    } catch (JsonSyntaxException e) {
      log.error("Failed to parse JSON string: {}", json, e);
      throw e;
    }
  }

  /**
   * 将JSON字符串转换为指定类型的对象
   *
   * @param json JSON字符串
   * @param type 目标类型
   * @param <T> 泛型类型
   * @return 转换后的对象
   * @throws IllegalArgumentException 如果输入JSON字符串为null或空
   * @throws JsonSyntaxException 如果JSON字符串格式不正确
   */
  public static <T> T fromJson(String json, Type type) {
    if (json == null || json.trim().isEmpty()) {
      throw new IllegalArgumentException("Input JSON string cannot be null or empty");
    }
    try {
      return GSON.fromJson(json, type);
    } catch (JsonSyntaxException e) {
      log.error("Failed to parse JSON string: {}", json, e);
      throw e;
    }
  }
}
