package github.lianyutian.cshop.user.service;

import github.lianyutian.cshop.user.model.param.AddressAddParam;
import github.lianyutian.cshop.user.model.param.AddressEditParam;
import github.lianyutian.cshop.user.model.vo.UserAddressVO;
import java.util.List;

/**
 * 用户地址服务接口
 *
 * @author lianyutian
 * @since 2024-12-26 09:54:49
 * @version 1.0
 */
public interface UserAddressService {
  /**
   * 根据用户地址 id 获取用户地址详情
   *
   * @param addressId 地址ID
   * @return 用户地址详情
   */
  UserAddressVO getUserAddressDetail(Long addressId);

  /**
   * 获取用户地址列表
   *
   * @return 用户地址列表
   */
  List<UserAddressVO> getAllUserAddressList();

  /**
   * 新增用户地址
   *
   * @param addressAddParam 新增用户地址参数
   */
  void addUserAddress(AddressAddParam addressAddParam);

  /**
   * 修改用户地址
   *
   * @param addressEditParam 修改用户地址参数
   */
  boolean updateUserAddress(AddressEditParam addressEditParam);

  /**
   * 删除用户地址
   *
   * @param addressId 地址ID
   * @return 删除结果
   */
  int deleteUserAddress(Long addressId);
}
