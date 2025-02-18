package github.lianyutian.cshop.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import github.lianyutian.cshop.product.model.po.ProductCategory;
import github.lianyutian.cshop.product.model.vo.ProductCategoryLevel2VO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author lm
 * @description 针对表【product_category(商品分类)】的数据库操作Mapper
 * @createDate 2025-02-18 14:47:16 @Entity generator.model.ProductCategory
 */
@Mapper
public interface ProductCategoryMapper extends BaseMapper<ProductCategory> {
  /**
   * 根据一级分类id获取二三级分类列表
   *
   * @param categoryId categoryId
   * @return List<ProductCategoryLevel2VO>
   */
  List<ProductCategoryLevel2VO> listByCategoryId(Long categoryId);
}
