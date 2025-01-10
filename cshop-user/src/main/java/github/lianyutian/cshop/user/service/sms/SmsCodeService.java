/** 短信服务接口 提供短信验证码发送功能 */
package github.lianyutian.cshop.user.service.sms;

/**
 * 短信服务接口
 *
 * @author lianyutian
 * @since 2024-12-18 14:39:43
 * @version 1.0
 */
public interface SmsCodeService {
  /**
   * 发送短信验证码
   *
   * @param phone 接收短信的手机号码
   * @param templateId 短信模板ID，用于指定短信的内容格式
   * @param templateParam 短信模板参数，用于替换短信模板中的占位符
   * @return 发送结果，通常为成功或失败的标识
   */
  boolean sendSmsCode(String phone, String templateId, String[] templateParam);
}
