package github.lianyutian.cshop.cart.controller;

import github.lianyutian.cshop.cart.model.param.CartDeleteParam;
import github.lianyutian.cshop.cart.model.param.CartUpdateParam;
import github.lianyutian.cshop.cart.service.CartService;
import github.lianyutian.cshop.common.model.ApiResult;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * CartController
 *
 * @author lianyutian
 * @since 2025-02-12 15:44:26
 * @version 1.0
 */
@RestController
@RequestMapping("/api/cart/v1")
@AllArgsConstructor
@Slf4j
public class CartController {

  private final CartService cartService;

  /**
   * 更新购物车(增加或减少)
   *
   * @param cartUpdateParam cartUpdateParam
   * @return ApiResult
   */
  @PostMapping("updateCart")
  public ApiResult<Void> updateCart(@RequestBody CartUpdateParam cartUpdateParam) {
    log.info(
        "更新购物车, skuId: {}, buyCount: {}",
        cartUpdateParam.getSkuId(),
        cartUpdateParam.getBuyCount());

    cartService.updateCart(cartUpdateParam);
    return ApiResult.success();
  }

  /**
   * 删除购物车
   *
   * @param cartDeleteParam cartDeleteParam
   * @return ApiResult
   */
  @PostMapping("deleteCart")
  public ApiResult<Void> deleteCart(@RequestBody CartDeleteParam cartDeleteParam) {
    log.info("删除购物车, skuId: {}", cartDeleteParam.getSkuIdList());
    cartService.deleteCart(cartDeleteParam);
    return ApiResult.success();
  }
}
