package github.lianyutian.cshop.user.service.sms;

import github.lianyutian.cshop.user.service.sms.impl.AliyunSmsCodeServiceImpl;
import github.lianyutian.cshop.user.service.sms.impl.TencentcloudSmsCodeServiceImpl;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 短信发送管理
 *
 * @author lianyutian
 * @since 2024-12-19 09:50:10
 * @version 1.0
 */
@Service
@Slf4j
public class SmsSender {
  /** 当前使用的短信服务 */
  private SmsCodeService currentService;

  /** 当前短信模板ID */
  private String currentTemplateId;

  /** 失败计数 */
  private final AtomicInteger failureCount = new AtomicInteger(0);

  /** 阿里云短信服务实现 */
  private final AliyunSmsCodeServiceImpl aliyunSmsCodeService;

  /** 腾讯云短信服务实现 */
  private final TencentcloudSmsCodeServiceImpl tencentcloudSmsCodeService;

  /** 最大失败次数 */
  private final int maxFailures;

  private final String aliyunTemplateId;

  private final String tencentcloudTemplateId;

  /** 构造方法，默认使用阿里云短信服务 */
  public SmsSender(
      @Value("${sms.send.fail}") int maxFailures,
      @Value("${sms.send.templateId.aliyun}") String aliyunTemplateId,
      @Value("${sms.send.templateId.tencentcloud}") String tencentcloudTemplateId,
      AliyunSmsCodeServiceImpl aliyunSmsCodeService,
      TencentcloudSmsCodeServiceImpl tencentcloudSmsCodeService) {
    if (maxFailures <= 0) {
      throw new IllegalArgumentException("最大失败次数必须大于0");
    }
    this.maxFailures = maxFailures;
    this.aliyunTemplateId = aliyunTemplateId;
    this.tencentcloudTemplateId = tencentcloudTemplateId;
    this.aliyunSmsCodeService = aliyunSmsCodeService;
    this.tencentcloudSmsCodeService = tencentcloudSmsCodeService;
    this.currentService = aliyunSmsCodeService;
    this.currentTemplateId = aliyunTemplateId;
  }

  /**
   * 发送短信
   *
   * @param phoneNumber 接收短信的电话号码
   * @param templateParam 短信模板参数
   * @return 短信发送结果，true表示发送成功，false表示发送失败
   */
  public boolean send(String phoneNumber, String[] templateParam) {
    try {
      boolean result = currentService.sendSmsCode(phoneNumber, currentTemplateId, templateParam);
      // 发送成功时，重置失败次数
      failureCount.set(0);
      return result;
    } catch (Exception e) {
      log.error("短信模块-短信发送失败, {}", e.getMessage(), e);
      int currentFailureCount = failureCount.incrementAndGet();
      if (currentFailureCount >= maxFailures) {
        // 切换到另一个厂商
        switchVendor(currentFailureCount);
      }
      return false;
    }
  }

  /** 切换短信服务供应商 */
  private void switchVendor(int currentFailureCount) {
    if (currentService instanceof AliyunSmsCodeServiceImpl) {
      log.info("短信模块-切换到腾讯云, 当前失败次数: {}", currentFailureCount);
      currentService = tencentcloudSmsCodeService;
      currentTemplateId = tencentcloudTemplateId;
    } else {
      log.info("短信模块-切换到阿里云, 当前失败次数: {}", currentFailureCount);
      currentService = aliyunSmsCodeService;
      currentTemplateId = aliyunTemplateId;
    }
  }
}
