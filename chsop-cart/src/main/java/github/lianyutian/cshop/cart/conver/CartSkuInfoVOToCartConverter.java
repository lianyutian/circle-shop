package github.lianyutian.cshop.cart.conver;

import github.lianyutian.cshop.cart.model.po.Cart;
import github.lianyutian.cshop.cart.model.vo.CartSkuInfoVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

/**
 * @author lianyutian
 * @since 2025-02-13 13:56:46
 * @version 1.0
 */
@Mapper(componentModel = "spring")
public interface CartSkuInfoVOToCartConverter {
  /**
   * 对象转换
   *
   * @param cartSkuInfoVO cartSkuInfoVO
   * @return Cart
   */
  @Mappings({
    @Mapping(target = "skuPrice", source = "price"),
    @Mapping(target = "skuCount", source = "buyCount"),
    @Mapping(target = "createUser", source = "userId")
  })
  Cart toPO(CartSkuInfoVO cartSkuInfoVO);

  @Mappings({
    @Mapping(target = "price", source = "skuPrice"),
    @Mapping(target = "buyCount", source = "skuCount"),
    @Mapping(target = "userId", source = "createUser")
  })
  CartSkuInfoVO toVO(Cart cart);
}
