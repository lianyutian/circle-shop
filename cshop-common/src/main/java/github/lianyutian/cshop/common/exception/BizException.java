package github.lianyutian.cshop.common.exception;

import github.lianyutian.cshop.common.enums.BizCodeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 业务异常
 *
 * @author lianyutian
 * @since 2024-12-13 13:54:35
 * @version 1.0
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class BizException extends RuntimeException {
  /** 默认错误码 */
  private static final int DEFAULT_ERROR_CODE = -1;

  /** 异常code码 */
  private int code;

  /** 异常消息 */
  private String message;

  /**
   * 根据 code、message 返回异常信息
   *
   * @param code 错误码
   * @param message 异常消息
   */
  public BizException(int code, String message) {
    super(message);
    this.code = code;
    this.message = message;
  }

  /** 根据 BizCode 返回异常信息 */
  public BizException(BizCodeEnum bizCodeEnum) {
    super(bizCodeEnum.getMessage());
    this.code = bizCodeEnum.getCode();
    this.message = bizCodeEnum.getMessage();
  }

  /**
   * 根据 errMsg 返回异常信息
   *
   * @param errorMsg 错误信息
   */
  public BizException(String errorMsg) {
    super(errorMsg);
    this.code = DEFAULT_ERROR_CODE;
    this.message = errorMsg;
  }
}
