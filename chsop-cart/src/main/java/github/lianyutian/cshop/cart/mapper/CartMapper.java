package github.lianyutian.cshop.cart.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import github.lianyutian.cshop.cart.model.po.Cart;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author lianyutian
 * @description 针对表【cart(购物车表)】的数据库操作Mapper
 * @createDate 2025-02-13 10:12:13 @Entity github.lianyutian.cshop.cart.model.po.Cart
 */
@Mapper
public interface CartMapper extends BaseMapper<Cart> {}
