package github.lianyutian.cshop.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.gson.reflect.TypeToken;
import github.lianyutian.cshop.common.redis.RedisCache;
import github.lianyutian.cshop.common.utils.BeanUtil;
import github.lianyutian.cshop.common.utils.JsonUtil;
import github.lianyutian.cshop.product.constant.ProductCacheKeyConstant;
import github.lianyutian.cshop.product.mapper.ProductCategoryMapper;
import github.lianyutian.cshop.product.model.po.ProductCategory;
import github.lianyutian.cshop.product.model.vo.ProductCategoryLevel2VO;
import github.lianyutian.cshop.product.model.vo.ProductCategoryLevelVO;
import github.lianyutian.cshop.product.model.vo.ProductCategoryVO;
import github.lianyutian.cshop.product.service.ProductCategoryService;
import io.jsonwebtoken.lang.Collections;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author lianyutian
 * @since 2025-02-18 14:27:08
 * @version 1.0
 */
@Slf4j
@AllArgsConstructor
@Service
public class ProductCategoryServiceImpl implements ProductCategoryService {
  /** 顶级分类 */
  private static final int CATEGORY_PARENT_ID_TOP = 0;

  /** 分类状态 启用 */
  private static final int CATEGORY_ENABLE = 1;

  /** 分类状态 未删除 */
  private static final int CATEGORY_DELETE = 0;

  private final RedisCache redisCache;
  private final ProductCategoryMapper categoryMapper;

  @Override
  public List<ProductCategoryVO> getCategoryListLevelTop() {
    List<ProductCategoryVO> categoryListLevelTopFromCache = getCategoryListLevelTopFromCache();
    if (!Collections.isEmpty(categoryListLevelTopFromCache)) {
      return categoryListLevelTopFromCache;
    }
    return getCategoryListLevelTopFromDB();
  }

  @Override
  public List<ProductCategoryLevelVO> getCategoryLevelList(Long categoryId) {
    List<ProductCategoryLevelVO> categoryLevelListFromCache = getCategoryLevelListFromCache();
    if (!Collections.isEmpty(categoryLevelListFromCache)) {
      return categoryLevelListFromCache;
    }
    return getCategoryLevelListFromDB(categoryId);
  }

  private List<ProductCategoryLevelVO> getCategoryLevelListFromDB(Long categoryId) {
    // 获取多级分类列表
    List<ProductCategoryLevel2VO> levelVOList = categoryMapper.listByCategoryId(categoryId);

    if (Collections.isEmpty(levelVOList)) {
      return null;
    }

    // 组合返回数据
    // 使用 Map 来存储每个二级分类 ID 对应的 ProductCategoryLevelVO 对象
    Map<Integer, ProductCategoryLevelVO> levelVOMap = new HashMap<>();

    // 遍历所有的 ProductCategoryLevel2VO 对象
    for (ProductCategoryLevel2VO vo : levelVOList) {
      // 获取二级分类的 ID
      Integer level2Id = vo.getId2();

      // 检查 Map 中是否已经存在该二级分类的 ID
      ProductCategoryLevelVO levelVO = levelVOMap.get(level2Id);
      if (levelVO == null) {
        // 如果不存在，则创建一个新的 ProductCategoryLevelVO 对象
        levelVO = new ProductCategoryLevelVO();
        // 设置一级分类的 ID、name、alias、level
        levelVO.setId(vo.getId2());
        levelVO.setName(vo.getName2());
        levelVO.setAlias(vo.getAlias2());
        levelVO.setLevel(vo.getLevel2());

        // 创建一个新的列表来存储二级分类
        List<ProductCategoryVO> categoryList = new ArrayList<>();
        levelVO.setCategoryList(categoryList);

        // 将新的 ProductCategoryLevelVO 对象添加到 Map 中
        levelVOMap.put(level2Id, levelVO);
      }

      // 检查是否存在三级分类
      if (vo.getId3() != null) {
        // 创建三级分类的 VO 对象
        ProductCategoryVO level3 = new ProductCategoryVO();
        level3.setId(vo.getId3());
        level3.setName(vo.getName3());
        level3.setAlias(vo.getAlias3());
        level3.setLevel(vo.getLevel3());

        // 将三级分类添加到对应的二级分类的列表中
        levelVO.getCategoryList().add(level3);
      }
    }

    // 写入缓存，无过期时间
    String categoryKey = ProductCacheKeyConstant.CATEGORY_LEVEL_LIST;
    redisCache.set(categoryKey, levelVOMap.values(), 0, TimeUnit.MILLISECONDS);

    // 从 Map 中获取所有的 ProductCategoryLevelVO 对象并返回
    return new ArrayList<>(levelVOMap.values());
  }

  private List<ProductCategoryLevelVO> getCategoryLevelListFromCache() {
    String categoryKey = ProductCacheKeyConstant.CATEGORY_LEVEL_LIST;
    // 从缓存中获取数据
    String categoryValue = redisCache.get(categoryKey);

    log.info("分类模块-从缓存获取二三级分类列表信息，categoryKey:{}，categoryValue:{}", categoryKey, categoryValue);

    // 当读到空数据时返回空，防止缓存穿透
    if (StringUtils.isBlank(categoryValue)) {
      return null;
    }
    Type listType = new TypeToken<List<ProductCategoryLevelVO>>() {}.getType();
    List<ProductCategoryLevelVO> categoryVOList = JsonUtil.fromJson(categoryValue, listType);
    log.info("分类模块-解析二三级分类缓存信息，categoryKey:{}, categoryValue:{}", categoryKey, categoryValue);
    return categoryVOList;
  }

  /**
   * 从缓存中读取一级分类列表
   *
   * @return List<ProductCategoryVO>
   */
  private List<ProductCategoryVO> getCategoryListLevelTopFromCache() {
    String categoryKey = ProductCacheKeyConstant.CATEGORY_TOP_LEVEL_LIST;
    // 从缓存中获取数据
    String categoryValue = redisCache.get(categoryKey);

    log.info("分类模块-从缓存获取一级分类列表信息，categoryKey:{}，categoryValue:{}", categoryKey, categoryValue);

    // 当读到空数据时返回空，防止缓存穿透
    if (StringUtils.isBlank(categoryValue)) {
      return null;
    }
    Type listType = new TypeToken<List<ProductCategoryVO>>() {}.getType();
    List<ProductCategoryVO> categoryVOList = JsonUtil.fromJson(categoryValue, listType);
    log.info("分类模块-解析一级分类缓存信息，categoryKey:{}, categoryValue:{}", categoryKey, categoryValue);
    return categoryVOList;
  }

  /**
   * 从数据库中读取一级分类列表
   *
   * @return List<ProductCategoryVO>
   */
  private List<ProductCategoryVO> getCategoryListLevelTopFromDB() {
    List<ProductCategory> categoryTopList =
        categoryMapper.selectList(
            new LambdaQueryWrapper<ProductCategory>()
                .eq(ProductCategory::getParentId, CATEGORY_PARENT_ID_TOP)
                .eq(ProductCategory::getIsEnable, CATEGORY_ENABLE)
                .eq(ProductCategory::getIsDeleted, CATEGORY_DELETE));

    if (Collections.isEmpty(categoryTopList)) {
      return null;
    }

    // 品牌缓存 key
    String categoryKey = ProductCacheKeyConstant.CATEGORY_TOP_LEVEL_LIST;

    // 对象流式转换处理分类列表结果信息
    List<ProductCategoryVO> categoryVOList =
        categoryTopList.stream()
            .map(categoryTop -> BeanUtil.copy(categoryTop, ProductCategoryVO.class))
            .collect(Collectors.toList());

    // 写入缓存，无过期时间
    redisCache.set(categoryKey, JsonUtil.toJson(categoryVOList), 0);
    return categoryVOList;
  }
}
