package github.lianyutian.cshop.common.utils;

import github.lianyutian.cshop.common.enums.BizCodeEnums;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 接口统一响应封装
 *
 * @author lianyutian
 * @since 2024-12-13 13:40:47
 * @version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResult<T> {
    /**
     * 接口响应状态码 0 表示成功 其他表示失败
     */
    private Integer code;
    /**
     * 数据
     */
    private T data;
    /**
     * 描述
     */
    private String msg;

    /**
     * 成功响应
     * @return @see ApiResult
     */
    public static ApiResult<String> success() {
        return new ApiResult<>(0, null, null);
    }

    /**
     * 成功响应，传入数据
     * @param data 接口数据
     * @return @see ApiResult
     */
    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(0, data, null);
    }

    /**
     * 失败响应
     * @param code 状态码
     * @return @see ApiResult
     */
    public static ApiResult<String> error(Integer code, String msg) {
        return new ApiResult<>(code, null, msg);
    }

    /**
     * 自定义状态码和错误信息
     * @param code 状态码
     * @param msg 错误信息
     * @return @see ApiResult
     */
    public static ApiResult<String> buildResult(int code, String msg) {
        return new ApiResult<>(code, null, msg);
    }

    /**
     * 通过枚举返回
     * @param bizCodeEnums 业务枚举状态码
     * @return @see ApiResult
     */
    public static ApiResult<String> buildResult(BizCodeEnums bizCodeEnums) {
        return ApiResult.buildResult(bizCodeEnums.getCode(), bizCodeEnums.getMessage());
    }
}
