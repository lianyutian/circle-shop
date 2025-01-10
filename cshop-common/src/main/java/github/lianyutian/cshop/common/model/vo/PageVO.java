package github.lianyutian.cshop.common.model.vo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 分页返回结果
 *
 * @author lianyutian
 * @since 2025-01-03 14:19:25
 * @version 1.0
 */
@Data
@AllArgsConstructor
public class PageVO<T> {
  /** 总条数 */
  protected Long total;

  /** 总页码数 */
  protected Long pages;

  /** 当前页数据 */
  protected List<T> list;

  public static <T> PageVO<T> empty(Page<?> page) {
    return new PageVO<>(
        Objects.requireNonNull(page, "Page cannot be null").getTotal(),
        page.getPages(),
        Collections.emptyList());
  }

  public static <T> PageVO<T> of(Page<?> page, List<T> list) {
    if (page == null) {
      throw new IllegalArgumentException("Page cannot be null");
    }
    if (list == null) {
      throw new IllegalArgumentException("List cannot be null");
    }
    return new PageVO<>(page.getTotal(), page.getPages(), Collections.unmodifiableList(list));
  }

  @JsonIgnore
  public boolean isEmpty() {
    return list.isEmpty();
  }
}
