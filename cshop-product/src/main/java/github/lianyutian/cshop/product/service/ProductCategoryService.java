package github.lianyutian.cshop.product.service;

import github.lianyutian.cshop.product.model.vo.ProductCategoryLevelVO;
import github.lianyutian.cshop.product.model.vo.ProductCategoryVO;
import java.util.List;

/**
 * @author lianyutian
 * @since 2025-02-18 14:26:29
 * @version 1.0
 */
public interface ProductCategoryService {
  /**
   * 获取一级分类
   *
   * @return List<ProductCategoryVO>
   */
  List<ProductCategoryVO> getCategoryListLevelTop();

  /**
   * 获取二级分类
   *
   * @param categoryId 分类id
   * @return List<ProductCategoryLevelVO>
   */
  List<ProductCategoryLevelVO> getCategoryLevelList(Long categoryId);
}
