package github.lianyutian.cshop.admin.service.impl;

import github.lianyutian.cshop.admin.handler.CouponTemplateContext;
import github.lianyutian.cshop.admin.handler.CouponTemplateHandler;
import github.lianyutian.cshop.admin.model.vo.req.CouponTemplateReqVO;
import github.lianyutian.cshop.admin.model.vo.res.CouponTemplateResVO;
import github.lianyutian.cshop.admin.service.CouponTemplateService;
import github.lianyutian.cshop.common.exception.CompositeException;
import github.lianyutian.cshop.common.interceptor.LoginInterceptor;
import github.lianyutian.cshop.common.model.LoginUserInfo;
import github.lianyutian.cshop.common.utils.BeanUtil;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author lianyutian
 * @since 2025/3/5
 * @version 1.0
 */
@Service
@Slf4j
@AllArgsConstructor
public class CouponTemplateServiceImpl implements CouponTemplateService {

  private List<CouponTemplateHandler> handlers;

  @Override
  @Transactional
  public boolean create(CouponTemplateReqVO couponTemplateReqVO) {
    /** 获取责任链上下文 */
    CouponTemplateContext context = new CouponTemplateContext();
    context.setCouponTemplateReqVO(couponTemplateReqVO);
    LoginUserInfo loginUserInfo = LoginInterceptor.USER_THREAD_LOCAL.get();
    context.setUserId(loginUserInfo.getId());
    try {
      initTemplate(context);

      // 添加空检查
      if (handlers == null || handlers.isEmpty()) {
        log.error("责任链创建优惠券模板出现异常，处理器未配置");
        throw new IllegalStateException("责任链处理器未配置");
      }

      // 按Order顺序执行责任链相关处理器件
      handlers.stream()
          .sorted(AnnotationAwareOrderComparator.INSTANCE)
          .forEach(
              handler -> {
                if (!context.hasErrors()) {
                  handler.handle(context);
                }
              });

      if (context.hasErrors()) {
        log.error("创建优惠券模板出现异常: {}", context.getErrors());
        throw new CompositeException(context.getErrors());
      }

      return true;
    } catch (Exception e) {
      log.error("创建优惠券模板出现异常: {}", e.getMessage(), e);
      return false;
    }
  }

  /** 初始化优惠券模版 */
  private void initTemplate(CouponTemplateContext context) {
    CouponTemplateReqVO template = context.getCouponTemplateReqVO();
    template.setCouponStatus(false);
    template.setCouponDel(false);
    template.setCouponAuditStatus(1);
    CouponTemplateResVO couponTemplateResVO = BeanUtil.copy(template, CouponTemplateResVO.class);
    log.info("初始化优惠券模板, reqVO: {}, resVO: {}", template, couponTemplateResVO);
    context.setCouponTemplateResVO(couponTemplateResVO);
  }
}
