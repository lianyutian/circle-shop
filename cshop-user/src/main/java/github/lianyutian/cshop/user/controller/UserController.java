package github.lianyutian.cshop.user.controller;

import github.lianyutian.cshop.common.enums.BizCodeEnums;
import github.lianyutian.cshop.common.utils.ApiResult;
import github.lianyutian.cshop.user.model.vo.UserLoginVO;
import github.lianyutian.cshop.user.model.vo.UserRegisterVO;
import github.lianyutian.cshop.user.service.UserService;
import github.lianyutian.cshop.user.service.oss.OssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * UserController
 *
 * @author lianyutian
 * @since 2024-12-13 14:19:30
 * @version 1.0
 */
@RestController
@RequestMapping("/api/user/v1")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    @Value("${oss.aliyun.maxFileSize:1048576}") // 新增文件大小限制配置
    private long maxFileSize;

    @Value("${oss.aliyun.allowedFileTypes:image/jpeg,image/png}") // 新增允许的文件类型配置
    private String[] allowedFileTypes;

    private final OssService ossService;

    private final UserService userService;

    /**
     * 上传用户头像
     * 默认文件大小 1M，超过报错
     *
     * @param file 头像文件
     * @return 上传结果
     */
    @PostMapping("uploadUserAvatar")
    public ApiResult<String> uploadUserAvatar(@RequestPart("file") MultipartFile file) {
        // 验证文件是否为空
        if (file == null || file.isEmpty()) {
            log.warn("用户微服务-上传模块-文件为空");
            return ApiResult.result(BizCodeEnums.USER_AVATAR_FILE_UPLOAD_ERROR);
        }
        // 验证文件大小
        if (file.getSize() > maxFileSize) {
            log.warn("用户微服务-上传模块-文件超出最大限制: {}", file.getSize());
            return ApiResult.result(BizCodeEnums.USER_AVATAR_FILE_UPLOAD_ERROR);
        }
        // 验证文件类型
        String contentType = file.getContentType();
        if (!isFileTypeAllowed(contentType)) {
            log.warn("用户微服务-上传模块-不允许的文件类型: {}", contentType);
            return ApiResult.result(BizCodeEnums.USER_AVATAR_FILE_UPLOAD_ERROR);
        }
        String result = ossService.uploadUserAvatar(file);
        return result != null ? ApiResult.success(result) : ApiResult.result(BizCodeEnums.USER_AVATAR_FILE_UPLOAD_ERROR);
    }

    /**
     * 用户注册
     * @param userRegisterVO 注册信息
     * @return 注册结果
     */
    @PostMapping("/register")
    public ApiResult<Void> register (@RequestBody UserRegisterVO userRegisterVO){
        return userService.register(userRegisterVO);
    }

    /**
     * 用户登录
     *
     * @param userLoginVO 用户登录VO
     * @return 登录结果
     */
    @PostMapping("login")
    public ApiResult<Map<String, Object>> login(@RequestBody UserLoginVO userLoginVO) {
        return userService.login(userLoginVO);
    }

    /**
     * 刷新 token
     *
     * @param refreshToken refreshToken
     * @param accessToken accessToken
     * @return 新的 token
     */
    @PostMapping("refreshToken")
    public ApiResult<Map<String, Object>> refreshToken(@RequestParam(value = "refreshToken") String refreshToken,
                                                       @RequestParam(value = "accessToken") String accessToken) {
        return userService.refreshToken(refreshToken, accessToken);
    }

    private boolean isFileTypeAllowed(String contentType) {
        for (String allowedType : allowedFileTypes) {
            if (allowedType.equalsIgnoreCase(contentType)) {
                return true;
            }
        }
        return false;
    }
}
