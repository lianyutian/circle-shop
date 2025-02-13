package github.lianyutian.cshop.cart.controller;

import github.lianyutian.cshop.cart.model.param.CartAddParam;
import github.lianyutian.cshop.cart.service.CartService;
import github.lianyutian.cshop.common.model.ApiResult;
import lombok.AllArgsConstructor;
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
public class CartController {

  private final CartService cartService;

  /**
   * 添加购物车
   *
   * @param cartAddParam 购物车入参
   * @return ApiResult
   */
  @PostMapping("addCart")
  public ApiResult<Void> addCart(@RequestBody CartAddParam cartAddParam) {
    cartService.addCart(cartAddParam);
    return ApiResult.success();
  }
}
