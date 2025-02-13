package github.lianyutian.cshop.cart.model.entity;

import java.util.Date;
import java.util.List;
import lombok.Data;

/**
 * @author lianyutian
 * @since 2025-02-12 15:59:36
 * @version 1.0
 */
@Data
public class SkuInfoEntity {
  /** 商品编码 */
  private Long skuId;

  /** 商品名称 */
  private String skuName;

  /** 价格（单位为分） */
  private Integer price;

  /** 会员价（单位为分） */
  private Integer vipPrice;

  /** 商品主图链接 */
  private String mainUrl;

  /** 商品轮播图 [{"sort":1, "img": "url"}] */
  private List<ImageInfo> skuImage;

  /** 商品详情图 [{"sort":1, "img": "url"}] */
  private List<ImageInfo> detailImage;

  /** 商品状态 1:上架 2:下架 */
  private Integer skuStatus;

  @Data
  public static class ImageInfo {

    /** 排序字段，从小到大 */
    private Integer sort;

    /** 商品图片 url */
    private String img;
  }

  private Date updateTime;
}
