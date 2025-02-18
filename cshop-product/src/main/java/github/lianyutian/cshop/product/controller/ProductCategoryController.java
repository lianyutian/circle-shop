package github.lianyutian.cshop.product.controller;

import github.lianyutian.cshop.common.model.ApiResult;
import github.lianyutian.cshop.product.model.vo.ProductCategoryLevelVO;
import github.lianyutian.cshop.product.model.vo.ProductCategoryVO;
import github.lianyutian.cshop.product.service.ProductCategoryService;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品分类
 *
 * @author lianyutian
 * @since 2025-02-18 14:23:17
 * @version 1.0
 */
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/product/category/v1/")
public class ProductCategoryController {

  private final ProductCategoryService productCategoryService;

  /**
   * 一级商品分类列表
   *
   * @return ApiResult
   */
  @GetMapping("list_lv1")
  public ApiResult<List<ProductCategoryVO>> getCategoryListLevelTop() {
    List<ProductCategoryVO> categoryVO = productCategoryService.getCategoryListLevelTop();
    return ApiResult.success(categoryVO);
  }

  /**
   * 二三级商品分类列表
   *
   * @return ApiResult
   */
  @GetMapping("/list_lv2/{category_id}")
  public ApiResult<List<ProductCategoryLevelVO>> getCategoryListLevel(
      @PathVariable("category_id") Long categoryId) {
    log.info("查询二三级商品分类列表信息，category_id:{}", categoryId);
    List<ProductCategoryLevelVO> categoryList =
        productCategoryService.getCategoryLevelList(categoryId);
    return ApiResult.success(categoryList);
  }
}
