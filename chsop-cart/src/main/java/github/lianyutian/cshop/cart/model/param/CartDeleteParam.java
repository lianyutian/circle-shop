package github.lianyutian.cshop.cart.model.param;

import java.util.List;
import lombok.Data;

/**
 * @author lianyutian
 * @since 2025-02-13 15:11:23
 * @version 1.0
 */
@Data
public class CartDeleteParam {
  private List<Long> skuIdList;
}
