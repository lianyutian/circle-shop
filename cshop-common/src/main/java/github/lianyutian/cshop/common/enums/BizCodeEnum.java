package github.lianyutian.cshop.common.enums;

import lombok.Getter;

/**
 * 统一业务枚举状态码
 *
 * @author lianyutian
 * @since 2024-12-13 13:27:13
 * @version 1.0
 *     <p>整个状态码总共 7 位，前 3 位表示「业务微服务」状态码，后 4 位表示「服务内部接口」状态码，后续需要再进行拆分。 * 公共操作：110。 * 用户服务：210，
 */
@Getter
public enum BizCodeEnum {
  /** 通用操作码 */
  COMMON_OP_REPEAT(110001, "重复操作"),
  COMMON_PARAM_ERROR(110002, "参数错误"),
  COMMON_SERVER_ERROR(110003, "服务异常"),
  COMMON_TOO_MANY_TRY(110004, "当前访问人数过多，请稍候再试..."),

  /** 用户微服务验证码相关 2101 开头 */
  USER_PHONE_ERROR(2101001, "手机号不合法"),
  USER_CODE_FAST_LIMITED(2101002, "验证码已发送，请稍后再发"),
  USER_CODE_PHONE_ERROR(2101003, "手机验证码错误"),
  USER_CODE_CAPTCHA_ERROR(2101004, "图形验证码错误"),
  USER_CODE_EMAIL_ERROR(2101005, "邮箱验证码错误"),
  USER_CODE_SMS_ERROR(2101006, "短信验证码错误"),
  USER_CODE_SMS_SEND_ERROR(2101006, "短信发送失败请稍后重试"),
  USER_CODE_SMS_SEND_REPLICATE_ERROR(2101007, "短信验证码已发送请稍后重试"),
  USER_CODE_SEND_ERROR(2101008, "验证码发送失败，请稍后重试"),

  /** 用户微服务账号相关 2102 开头 */
  USER_ACCOUNT_EXIST(2102001, "用户已存在"),
  USER_ACCOUNT_UNREGISTER(2102002, "用户不存在"),
  USER_ACCOUNT_PWD_ERROR(2102003, "用户账号或密码错误"),
  USER_REFRESH_TOKEN_EMPTY(2102004, "请重新登录"),
  USER_ACCOUNT_UNLOGIN(2102005, "用户账号未登录"),

  USER_LOGIN_SUCCESS(0, "用户登录成功"),
  USER_UPDATE_LOCK_FAIL(2102006, "修改用户信息获取锁失败"),
  USER_SHOW_LOCK_FAIL(2102007, "读取用户信息获取锁失败"),
  USER_DETAIL_UPDATE_FAIL(2102008, "用户详情更新失败"),

  /** 用户微服务上传相关 2103 开头 */
  USER_AVATAR_FILE_UPLOAD_ERROR(2103001, "用户头像上传失败"),

  /** 用户微服务收货地址相关 2104 开头 */
  USER_ADDRESS_NOT_EXITS(2104001, "收货地址不存在"),
  USER_ADDRESS_ADD_FAIL(2104002, "新增收货地址失败"),
  USER_ADDRESS_DEL_FAIL(2104003, "删除收货地址失败"),

  /** 用户关注、取消 2105 开头 博主关注、取消 */
  USER_FOLLOWER_NOT_SELF(2105001, "不能关注自己哦"),
  USER_UN_FOLLOWER_NOT_SELF(2105002, "不能取关自己哦"),
  USER_FOLLOWED(2105003, "已经关注过了哦"),
  USER_UN_FOLLOWED(2105004, "已经取关过了哦"),
  USER_ATTENTION_NOT_SELF(2105005, "博主不能关注自己哦"),
  USER_UN_ATTENTION_NOT_SELF(2105006, "博主不能取关自己哦"),
  USER_ATTENTED(2105007, "已经关注过了哦"),
  USER_UN_ATTENTED(2105008, "已经取关过了哦"),
  USER_FOLLOWER_SUCCESS(0, "关注成功"),
  USER_UN_FOLLOWER_SUCCESS(0, "取关成功"),
  USER_ATTENTION_SUCCESS(0, "关注成功"),
  USER_UN_ATTENTION_SUCCESS(0, "取关成功"),
  USER_FOLLOWER_INFO_LOCK_FAIL(2105009, "查询用户是否关注锁失败"),

  /** 用户笔记相关 2106 开头 */
  NOTE_NOT_EXITS(2106001, "笔记不存在"),
  NOTE_ADD_FAIL(2104002, "新增笔记失败"),
  NOTE_DEL_FAIL(2104003, "删除笔记失败"),
  NOTE_UPDATE_FAIL(2102004, "笔记更新失败"),
  NOTE_UPDATE_LOCK_FAIL(2104006, "修改笔记信息获取锁失败"),
  NOTE_INFO_LOCK_FAIL(2102007, "读取笔记信息获取锁失败");

  /** 错误信息 */
  private final String message;

  /** 状态码 */
  private final int code;

  /**
   * 编码
   *
   * @param code 状态码
   * @param message 错误信息
   */
  BizCodeEnum(int code, String message) {
    this.code = code;
    this.message = message;
  }
}
