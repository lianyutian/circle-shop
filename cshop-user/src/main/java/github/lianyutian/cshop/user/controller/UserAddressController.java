package github.lianyutian.cshop.user.controller;

import github.lianyutian.cshop.common.enums.BizCodeEnums;
import github.lianyutian.cshop.common.utils.ApiResult;
import github.lianyutian.cshop.user.model.param.AddressAddParam;
import github.lianyutian.cshop.user.model.param.AddressEditParam;
import github.lianyutian.cshop.user.model.vo.UserAddressVO;
import github.lianyutian.cshop.user.service.UserAddressService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户地址前端控制器
 *
 * @author lianyutian
 * @since 2024-12-26 09:42:18
 * @version 1.0
 */
@AllArgsConstructor
@RestController
@RequestMapping("/api/address/v1")
public class UserAddressController {

    private final UserAddressService userAddressService;

    /**
     * 根据id查询收货地址详情
     *
     * @param addressId 收货地址id
     * @return 收获地址详情
     */
    @GetMapping("detail/{addressId}")
    public ApiResult<UserAddressVO> detail(@PathVariable("addressId") Long addressId) {
        UserAddressVO userAddressVo = userAddressService.getUserAddressDetail(addressId);
        return userAddressVo == null
                ? ApiResult.result(BizCodeEnums.USER_ADDRESS_NOT_EXITS)
                : ApiResult.success(userAddressVo);
    }

    /**
     * 获取用户地址列表
     *
     * @return 用户地址列表
     */
    @GetMapping("list")
    public ApiResult<List<UserAddressVO>> list() {
        List<UserAddressVO> allUserAddressList = userAddressService.getAllUserAddressList();
        return ApiResult.success(allUserAddressList);
    }

    /**
     * 新增收货地址
     *
     * @param addressAddParam 收货地址
     * @return 新增结果
     */
    @PostMapping("add")
    public ApiResult<Void> add(@RequestBody AddressAddParam addressAddParam) {
        userAddressService.addUserAddress(addressAddParam);
        return ApiResult.success();
    }

    /**
     * 修改指定收货地址
     *
     * @param addressEditParam 收货地址
     * @return 修改结果
     */
    @PostMapping("edit")
    public ApiResult<Void> edit(@RequestBody AddressEditParam addressEditParam) {
        boolean updated = userAddressService.updateUserAddress(addressEditParam);
        return updated ? ApiResult.success() : ApiResult.result(BizCodeEnums.USER_ADDRESS_NOT_EXITS);
    }

    /**
     * 删除指定收货地址
     *
     * @param addressId 收货地址id
     * @return 删除结果
     */
    @GetMapping("delete/{addressId}")
    public ApiResult<Void> delete(@PathVariable("addressId") Long addressId) {
        return userAddressService.deleteUserAddress(addressId) == 1 ?
                ApiResult.success() :
                ApiResult.result(BizCodeEnums.USER_ADDRESS_NOT_EXITS);
    }
}
