package github.lianyutian.cshop.admin.handler;

/**
 * 优惠券模板责任链基础处理器
 *
 * @author lianyutian
 * @since 2025/3/5
 * @version 1.0
 */
public interface CouponTemplateHandler {
  /**
   * 处理器具体处理逻辑
   *
   * @param context context
   * @return
   */
  void handle(CouponTemplateContext context);

  /**
   * 设置下一个处理器
   *
   * @param handler
   */
  void setNextHandler(CouponTemplateHandler handler);
}
