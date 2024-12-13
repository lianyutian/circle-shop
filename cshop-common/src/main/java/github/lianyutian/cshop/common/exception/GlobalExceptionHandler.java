package github.lianyutian.cshop.common.exception;

import github.lianyutian.cshop.common.utils.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 全局异常处理
 *
 * @author lianyutian
 * @since 2024-12-13 14:04:58
 * @version 1.0
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ApiResult<String> handle(Exception e) {
        // 判断是否是业务异常还是非业务异常
        if (e instanceof BizException bizException) {
            log.error("[这里是业务异常信息]{}，具体内容如下: ", e.getMessage(), e);
            return ApiResult.error(bizException.getCode(), bizException.getMessage());
        } else {
            log.info("[这里是系统异常信息，具体内容如下: ]{}", e.getMessage(), e);
            return ApiResult.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "这里是系统异常：" + e);
        }
    }
}
