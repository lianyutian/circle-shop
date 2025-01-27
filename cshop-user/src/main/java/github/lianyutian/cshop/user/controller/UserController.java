package github.lianyutian.cshop.user.controller;

import github.lianyutian.cshop.common.enums.BizCodeEnum;
import github.lianyutian.cshop.common.model.ApiResult;
import github.lianyutian.cshop.user.model.param.UserEditParam;
import github.lianyutian.cshop.user.model.param.UserLoginParam;
import github.lianyutian.cshop.user.model.param.UserRegisterParam;
import github.lianyutian.cshop.user.model.vo.UserDetailVO;
import github.lianyutian.cshop.user.model.vo.UserShowVO;
import github.lianyutian.cshop.user.service.UserService;
import github.lianyutian.cshop.user.service.oss.OssService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
   *
   * @param file 头像文件
   * @return 上传结果
   */
  @PostMapping("uploadUserAvatar")
  public ApiResult<String> uploadUserAvatar(@RequestPart("file") MultipartFile file) {
    // 验证文件是否为空
    if (file == null || file.isEmpty()) {
      log.warn("用户微服务-上传模块-文件为空");
      return ApiResult.result(BizCodeEnum.USER_AVATAR_FILE_UPLOAD_ERROR);
    }
    // 验证文件大小
    if (file.getSize() > maxFileSize) {
      log.warn("用户微服务-上传模块-文件超出最大限制: {}", file.getSize());
      return ApiResult.result(BizCodeEnum.USER_AVATAR_FILE_UPLOAD_ERROR);
    }
    // 验证文件类型
    String contentType = file.getContentType();
    if (!isFileTypeAllowed(contentType)) {
      log.warn("用户微服务-上传模块-不允许的文件类型: {}", contentType);
      return ApiResult.result(BizCodeEnum.USER_AVATAR_FILE_UPLOAD_ERROR);
    }
    String result = ossService.uploadUserAvatar(file);
    return result != null
        ? ApiResult.success(result)
        : ApiResult.result(BizCodeEnum.USER_AVATAR_FILE_UPLOAD_ERROR);
  }

  /**
   * 用户注册
   *
   * @param userRegisterVO 注册信息
   * @return 注册结果
   */
  @PostMapping("register")
  public ApiResult<Void> register(@RequestBody UserRegisterParam userRegisterParam) {
    return userService.register(userRegisterParam);
  }

  /**
   * 用户登录
   *
   * @param userLoginParam 用户登录入参
   * @return 登录结果
   */
  @PostMapping("login")
  public ApiResult<Map<String, Object>> login(@RequestBody UserLoginParam userLoginParam) {
    return userService.login(userLoginParam);
  }

  /**
   * 刷新 token
   *
   * @param refreshToken refreshToken
   * @param accessToken accessToken
   * @return 新的 token
   */
  @PostMapping("refreshToken")
  public ApiResult<Map<String, Object>> refreshToken(
      @RequestParam(value = "refreshToken") String refreshToken,
      @RequestParam(value = "accessToken") String accessToken) {

    return userService.refreshToken(refreshToken, accessToken);
  }

  /**
   * 用户账号信息查询
   *
   * @return 用户信息
   */
  @GetMapping("detail")
  public ApiResult<UserDetailVO> detail() {
    UserDetailVO userDetailVO = userService.getUserDetail();
    return ApiResult.success(userDetailVO);
  }

  /**
   * 用户信息修改
   *
   * @param userEditParam 用户信息
   * @return 修改结果
   */
  @PostMapping("edit")
  public ApiResult<Void> edit(@RequestBody UserEditParam userEditParam) {
    userService.updateUser(userEditParam);
    return ApiResult.success();
  }

  /**
   * 获取用户展示信息
   *
   * @param userId 用户ID
   * @return 用户展示信息
   */
  @GetMapping("detail/show/{userId}")
  public ApiResult<UserShowVO> getUserShow(@PathVariable("userId") Long userId) {
    UserShowVO userShowVO = userService.getUserShow(userId);
    return ApiResult.success(userShowVO);
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
