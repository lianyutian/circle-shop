package github.lianyutian.cshop.user.service;

import github.lianyutian.cshop.common.model.ApiResult;
import github.lianyutian.cshop.user.model.param.UserEditParam;
import github.lianyutian.cshop.user.model.param.UserLoginParam;
import github.lianyutian.cshop.user.model.param.UserRegisterParam;
import github.lianyutian.cshop.user.model.vo.UserDetailVO;
import github.lianyutian.cshop.user.model.vo.UserShowVO;
import java.util.Map;

/**
 * 用户服务接口
 *
 * @author lianyutian
 * @since 2024-12-24 08:41:57
 * @version 1.0
 */
public interface UserService {
  /**
   * 用户注册
   *
   * @param userRegisterVO 用户注册VO
   * @return 注册结果
   */
  ApiResult<Void> register(UserRegisterParam userRegisterVO);

  /**
   * 用户登录
   *
   * @param userLoginVO 用户登录VO
   * @return 登录结果
   */
  ApiResult<Map<String, Object>> login(UserLoginParam userLoginVO);

  /**
   * 刷新token
   *
   * @param refreshToken 刷新token
   * @param accessToken 访问token
   */
  ApiResult<Map<String, Object>> refreshToken(String refreshToken, String accessToken);

  /**
   * 获取用户详情
   *
   * @return 用户详情
   */
  UserDetailVO getUserDetail();

  /**
   * 更新用户信息
   *
   * @param userEditParam 用户编辑参数
   */
  void updateUser(UserEditParam userEditParam);

  /**
   * 获取用户展示详情
   *
   * @param userId 用户id
   * @return 用户展示详情
   */
  UserShowVO getUserShow(Long userId);
}
