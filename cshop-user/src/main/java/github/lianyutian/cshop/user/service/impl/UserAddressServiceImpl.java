package github.lianyutian.cshop.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import github.lianyutian.cshop.common.interceptor.LoginInterceptor;
import github.lianyutian.cshop.common.model.LoginUserInfo;
import github.lianyutian.cshop.common.utils.BeanUtil;
import github.lianyutian.cshop.user.enums.AddressStatusEnum;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
    if (addressId == null || addressId <= 0) {
      throw new IllegalArgumentException("Invalid addressId");
    }
    UserAddress userAddress = userAddressMapper.selectById(addressId);
    if (userAddress == null) {
      return null;
    }
    return BeanUtil.copy(userAddress, UserAddressVO.class);
  }

  @Override
  public List<UserAddressVO> getAllUserAddressList() {
    LoginUserInfo loginUserInfo = getLoginUserInfo();
    List<UserAddress> userAddressList =
        userAddressMapper.selectList(
            new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, loginUserInfo.getId()));
    return userAddressList.stream().map(this::convertToUserAddressVO).collect(Collectors.toList());
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public void addUserAddress(AddressAddParam addressAddParam) {
    LoginUserInfo loginUserInfo = getLoginUserInfo();

    UserAddress userAddress = BeanUtil.copy(addressAddParam, UserAddress.class);
    userAddress.setUserId(loginUserInfo.getId());

    setDefaultStatusIfRequired(userAddress);

    int affectedRows = userAddressMapper.insert(userAddress);
    log.info(
        "用户收货地址模块-新增收货地址：affectedRows={}，data={}",
        affectedRows,
        formatUserAddressForLog(userAddress));
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public boolean updateUserAddress(AddressEditParam addressEditParam) {
    UserAddress oldUserAddress = userAddressMapper.selectById(addressEditParam.getId());
    if (oldUserAddress == null) {
      return false;
    }
    LoginUserInfo loginUserInfo = getLoginUserInfo();
    UserAddress userAddress = BeanUtil.copy(addressEditParam, UserAddress.class);
    userAddress.setUserId(loginUserInfo.getId());

    setDefaultStatusIfRequired(userAddress);

    int affectedRows =
        userAddressMapper.update(
            userAddress,
            new LambdaUpdateWrapper<UserAddress>()
                .eq(UserAddress::getId, addressEditParam.getId()));
    if (affectedRows == 0) {
      log.warn("用户收货地址模块-更新收货地址失败：id={}", addressEditParam.getId());
      return false;
    }
    log.info(
        "用户收货地址模块-更新收货地址：affectedRows={}，data={}",
        affectedRows,
        formatUserAddressForLog(userAddress));
    return true;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public int deleteUserAddress(Long addressId) {
    return userAddressMapper.deleteById(addressId);
  }

  private LoginUserInfo getLoginUserInfo() {
    LoginUserInfo loginUserInfo = LoginInterceptor.USER_THREAD_LOCAL.get();
    if (loginUserInfo == null) {
      throw new IllegalStateException("LoginUserInfo not found in ThreadLocal");
    }
    return loginUserInfo;
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
    return BeanUtil.copy(userAddress, UserAddressVO.class);
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
