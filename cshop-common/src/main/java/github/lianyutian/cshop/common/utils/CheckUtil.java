package github.lianyutian.cshop.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 校验工具类
 *
 * @author lianyutian
 * @since 2024-12-19 13:38:22
 * @version 1.0
 */
public class CheckUtil {
    /**
     * 手机号正则
     */
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");

    /**
     * 检测是否是手机号
     *
     * @param phone 手机号
     * @return boolean
     */
    public static boolean isPhone(String phone) {
        if (null == phone || "".equals(phone)) {
            return false;
        }
        Matcher m = PHONE_PATTERN.matcher(phone);
        return m.matches();
    }
}
