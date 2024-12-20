package github.lianyutian.cshop.user.controller;

import github.lianyutian.cshop.common.utils.ApiResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * UserController
 *
 * @author lianyutian
 * @since 2024-12-13 14:19:30
 * @version 1.0
 */
@RestController
@RequestMapping("/api/user/v1")
public class UserController {
    @PostMapping("test")
    public ApiResult<String> test() {
        int i = 1 / 0;
        return ApiResult.success("hello circle-shop");
    }
}
