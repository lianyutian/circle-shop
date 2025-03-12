package github.lianyutian.cshop.admin.handler;

import github.lianyutian.cshop.admin.model.vo.req.CouponTemplateReqVO;
import github.lianyutian.cshop.admin.model.vo.res.CouponTemplateResVO;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * @author lianyutian
 * @since 2025/3/5
 * @version 1.0
 */
@Data
public class CouponTemplateContext {
  // 请求输入
  private CouponTemplateReqVO couponTemplateReqVO;

  // 请求 HttpServletRequest
  private HttpServletRequest servletRequest;

  // 操作人信息
  private Long userId;

  // 中间数据
  private BigInteger shopNumber;
  private String productId;
  private String idempotentKey;

  // 输出
  private CouponTemplateResVO couponTemplateResVO;

  // 错误处理
  private List<String> errors = new ArrayList<>();

  public void addError(String error) {
    errors.add(error);
  }

  public boolean hasErrors() {
    return !errors.isEmpty();
  }
}
