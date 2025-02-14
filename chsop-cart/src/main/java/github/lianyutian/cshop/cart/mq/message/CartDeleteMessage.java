package github.lianyutian.cshop.cart.mq.message;

import java.util.List;
import lombok.Data;

/**
 * @author lianyutian
 * @since 2025-02-13 16:49:19
 * @version 1.0
 */
@Data
public class CartDeleteMessage {
  private Long userId;
  private List<Long> skuIdList;
}
