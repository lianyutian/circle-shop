package github.lianyutian.cshop.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import github.lianyutian.cshop.common.interceptor.LoginInterceptor;
import github.lianyutian.cshop.common.model.LoginUserInfo;
import github.lianyutian.cshop.user.constant.AddressStatusEnum;
import github.lianyutian.cshop.user.mapper.UserAddressMapper;
import github.lianyutian.cshop.user.model.param.AddressAddParam;
import github.lianyutian.cshop.user.model.param.AddressEditParam;
import github.lianyutian.cshop.user.model.po.UserAddress;
import github.lianyutian.cshop.user.model.vo.UserAddressVO;
import github.lianyutian.cshop.user.service.UserAddressService;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * 用户地址服务接口实现类
 *
 * @author lianyutian
 * @since 2024-12-26 09:55:13
 * @version 1.0
 */
@Service
@Slf4j
@AllArgsConstructor
public class UserAddressServiceImpl implements UserAddressService {

  private final UserAddressMapper userAddressMapper;

  @Override
  public UserAddressVO getUserAddressDetail(Long addressId) {
    UserAddress userAddress = userAddressMapper.selectById(addressId);
    if (userAddress == null) {
      return null;
    }
    UserAddressVO userAddressVO = new UserAddressVO();
    BeanUtils.copyProperties(userAddress, userAddressVO);
    return userAddressVO;
  }

  @Override
  public List<UserAddressVO> getAllUserAddressList() {
    LoginUserInfo loginUserInfo = getLoginUserInfo();
    List<UserAddress> userAddressList =
        userAddressMapper.selectList(
            new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, loginUserInfo.getId()));
    if (CollectionUtils.isEmpty(userAddressList)) {
      return List.of();
    }
    return userAddressList.stream().map(this::convertToUserAddressVO).collect(Collectors.toList());
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void addUserAddress(AddressAddParam addressAddParam) {
    LoginUserInfo loginUserInfo = getLoginUserInfo();
    UserAddress userAddress = new UserAddress();
    userAddress.setUserId(loginUserInfo.getId());
    BeanUtils.copyProperties(addressAddParam, userAddress);

    setDefaultStatusIfRequired(userAddress);

    int rows = userAddressMapper.insert(userAddress);
    log.info("用户收货地址模块-新增收货地址：rows={}，data={}", rows, formatUserAddressForLog(userAddress));
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public boolean updateUserAddress(AddressEditParam addressEditParam) {
    UserAddress oldUserAddress = userAddressMapper.selectById(addressEditParam.getId());
    if (oldUserAddress == null) {
      return false;
    }
    LoginUserInfo loginUserInfo = getLoginUserInfo();
    UserAddress userAddress = new UserAddress();
    userAddress.setUserId(loginUserInfo.getId());
    BeanUtils.copyProperties(addressEditParam, userAddress);

    setDefaultStatusIfRequired(userAddress);

    int rows =
        userAddressMapper.update(
            userAddress,
            new LambdaUpdateWrapper<UserAddress>()
                .eq(UserAddress::getId, addressEditParam.getId()));
    log.info("用户收货地址模块-更新收货地址：rows={}，data={}", rows, formatUserAddressForLog(userAddress));
    return true;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public int deleteUserAddress(Long addressId) {
    return userAddressMapper.deleteById(addressId);
  }

  private LoginUserInfo getLoginUserInfo() {
    return LoginInterceptor.USER_THREAD_LOCAL.get();
  }

  private void setDefaultStatusIfRequired(UserAddress userAddress) {
    if (Objects.equals(
        userAddress.getDefaultStatus(), AddressStatusEnum.DEFAULT_STATUS.getCode())) {
      // 设置默认的为非默认状态
      userAddressMapper.update(
          null,
          new LambdaUpdateWrapper<UserAddress>()
              .set(UserAddress::getDefaultStatus, AddressStatusEnum.NOT_DEFAULT_STATUS.getCode())
              .eq(UserAddress::getUserId, userAddress.getUserId())
              .eq(UserAddress::getDefaultStatus, AddressStatusEnum.DEFAULT_STATUS.getCode()));
    }
  }

  private UserAddressVO convertToUserAddressVO(UserAddress userAddress) {
    UserAddressVO userAddressVO = new UserAddressVO();
    BeanUtils.copyProperties(userAddress, userAddressVO);
    return userAddressVO;
  }

  private String formatUserAddressForLog(UserAddress userAddress) {
    // 只打印必要的字段，避免泄露敏感信息
    return "id="
        + userAddress.getId()
        + ", userId="
        + userAddress.getUserId()
        + ", defaultStatus="
        + userAddress.getDefaultStatus();
  }
}
