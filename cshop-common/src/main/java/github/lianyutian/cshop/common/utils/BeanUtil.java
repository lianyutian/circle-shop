package github.lianyutian.cshop.common.utils;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

/**
 * Bean拷贝工具类
 *
 * @author lianyutian
 * @since 2025-01-03 15:09:31
 * @version 1.0
 */
@Slf4j
public class BeanUtil {

  private static final ConstructorCache CONSTRUCTOR_CACHE = new ConstructorCache();

  public static <T> T copy(Object source, Class<T> targetClass) {
    if (source == null) {
      return null;
    }
    T target;
    try {
      Constructor<T> constructor = CONSTRUCTOR_CACHE.getConstructor(targetClass);
      target = constructor.newInstance();
      BeanUtils.copyProperties(source, target);
    } catch (ReflectiveOperationException e) {
      log.error(
          "Failed to copy properties from {} to {}",
          source.getClass().getName(),
          targetClass.getName(),
          e);
      throw new BeanCopyException("Failed to copy properties", e);
    }
    return target;
  }

  public static <T> List<T> copyList(List<?> sourceList, Class<T> targetClass) {
    if (sourceList == null || sourceList.isEmpty()) {
      return null;
    }
    List<T> targetList = new ArrayList<>(sourceList.size());
    for (Object source : sourceList) {
      T target = copy(source, targetClass);
      if (target != null) {
        targetList.add(target);
      }
    }
    return targetList;
  }

  private static class ConstructorCache {

    private final Map<Class<?>, Constructor<?>> cache = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> Constructor<T> getConstructor(Class<T> clazz) throws ReflectiveOperationException {
      Constructor<T> constructor = (Constructor<T>) cache.get(clazz);
      if (constructor == null) {
        synchronized (cache) {
          constructor = (Constructor<T>) cache.get(clazz);
          if (constructor == null) {
            try {
              constructor = clazz.getDeclaredConstructor();
              constructor.setAccessible(true);
              cache.put(clazz, constructor);
            } catch (NoSuchMethodException e) {
              throw new ReflectiveOperationException(e);
            }
          }
        }
      }
      return constructor;
    }
  }

  public static class BeanCopyException extends RuntimeException {
    public BeanCopyException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
