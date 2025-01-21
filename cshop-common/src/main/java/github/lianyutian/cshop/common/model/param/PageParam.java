package github.lianyutian.cshop.common.model.param;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * 分页查询入参
 *
 * @author lianyutian
 * @since 2025-01-03 14:53:29
 * @version 1.0
 */
@Data
public class PageParam {
  public static final Integer DEFAULT_PAGE_SIZE = 20;
  public static final Integer DEFAULT_PAGE_NUM = 1;

  /** 页码 */
  @Min(value = 1, message = "页码不能小于1")
  private final Integer pageNo = DEFAULT_PAGE_NUM;

  /** 每页大小 */
  @Min(value = 1, message = "每页查询数量不能小于1")
  private final Integer pageSize = DEFAULT_PAGE_SIZE;

  /** 是否升序 */
  private Boolean isAsc = true;

  /** 排序字段 */
  private String sortBy;

  public int from() {
    return (pageNo - 1) * pageSize;
  }

  public <T> Page<T> toMpPage(OrderItem... orderItems) {
    Page<T> page = new Page<>(pageNo, pageSize);
    // 是否手动指定排序方式
    if (orderItems != null && orderItems.length > 0) {
      for (OrderItem orderItem : orderItems) {
        page.addOrder(orderItem);
      }
      return page;
    }
    // 前端是否有排序字段
    if (StringUtils.isNotEmpty(sortBy)) {
      OrderItem orderItem = new OrderItem();
      orderItem.setAsc(isAsc);
      orderItem.setColumn(sortBy);
      page.addOrder(orderItem);
    }
    return page;
  }

  public <T> Page<T> toMpPage(String defaultSortBy, boolean isAsc) {
    if (StringUtils.isBlank(sortBy)) {
      sortBy = defaultSortBy;
      this.isAsc = isAsc;
    }
    Page<T> page = new Page<>(pageNo, pageSize);
    OrderItem orderItem = new OrderItem();
    orderItem.setAsc(this.isAsc);
    orderItem.setColumn(sortBy);
    page.addOrder(orderItem);
    return page;
  }

  public <T> Page<T> toMpPageDefaultSortByCreateTimeDesc() {
    return toMpPage("create_time", false);
  }

  public <T> Page<T> toMpPageSortByUpdateTimeDesc() {
    return toMpPage("update_time", false);
  }
}
