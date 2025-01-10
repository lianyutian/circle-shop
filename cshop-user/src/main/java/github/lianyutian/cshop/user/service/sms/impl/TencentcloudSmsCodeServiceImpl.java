package github.lianyutian.cshop.user.service.sms.impl;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20210111.models.SendSmsResponse;
import github.lianyutian.cshop.common.utils.JsonUtil;
import github.lianyutian.cshop.user.service.sms.SmsCodeService;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 腾讯云短信服务实现类
 *
 * @author lianyutian
 * @since 2024-12-18 14:44:54
 * @version 1.0
 */
@Service
@Slf4j
public class TencentcloudSmsCodeServiceImpl implements SmsCodeService {
  @Value("${sms.tencent.secretId}")
  private String secretId;

  @Value("${sms.tencent.secretKey}")
  private String secretKey;

  @Value("${sms.tencent.signName}")
  private String signName;

  @Override
  public boolean sendSmsCode(String phone, String templateId, String[] templateParam) {
    try {
      // 实例化一个认证对象，入参需要传入腾讯云账户 SecretId，SecretKey。
      // 为了保护密钥安全，建议将密钥设置在环境变量中或者配置文件中，请参考凭证管理
      // https://github.com/TencentCloud/tencentcloud-sdk-java?tab=readme-ov-file#%E5%87%AD%E8%AF%81%E7%AE%A1%E7%90%86。
      // 硬编码密钥到代码中有可能随代码泄露而暴露，有安全隐患，并不推荐。
      // SecretId、SecretKey 查询: https://console.cloud.tencent.com/cam/capi
      // Credential cred = new Credential("SecretId", "SecretKey");
      Credential cred = new Credential(secretId, secretKey);

      /* 实例化要请求产品(以sms为例)的client对象
       * 第二个参数是地域信息，可以直接填写字符串ap-guangzhou，支持的地域列表参考
       * https://cloud.tencent.com/document/api/382/52071#.E5.9C.B0.E5.9F.9F.E5.88.97.E8.A1.A8 */
      SendSmsResponse sendSmsResponse =
          sendSms(cred, signName, templateId, new String[] {phone}, templateParam);
      log.info("短信模块-短信发送结果：{}", JsonUtil.toJson(sendSmsResponse));

      if (Objects.equals(sendSmsResponse.getSendStatusSet()[0].getCode(), "Ok")) {
        return true;
      }

      /* 当出现以下错误码时，快速解决方案参考
       * [FailedOperation.SignatureIncorrectOrUnapproved](https://cloud.tencent.com/document/product/382/9558#.E7.9F.AD.E4.BF.A1.E5.8F.91.E9.80.81.E6.8F.90.E7.A4.BA.EF.BC.9Afailedoperation.signatureincorrectorunapproved-.E5.A6.82.E4.BD.95.E5.A4.84.E7.90.86.EF.BC.9F)
       * [FailedOperation.TemplateIncorrectOrUnapproved](https://cloud.tencent.com/document/product/382/9558#.E7.9F.AD.E4.BF.A1.E5.8F.91.E9.80.81.E6.8F.90.E7.A4.BA.EF.BC.9Afailedoperation.templateincorrectorunapproved-.E5.A6.82.E4.BD.95.E5.A4.84.E7.90.86.EF.BC.9F)
       * [UnauthorizedOperation.SmsSdkAppIdVerifyFail](https://cloud.tencent.com/document/product/382/9558#.E7.9F.AD.E4.BF.A1.E5.8F.91.E9.80.81.E6.8F.90.E7.A4.BA.EF.BC.9Aunauthorizedoperation.smssdkappidverifyfail-.E5.A6.82.E4.BD.95.E5.A4.84.E7.90.86.EF.BC.9F)
       * [UnsupportedOperation.ContainDomesticAndInternationalPhoneNumber](https://cloud.tencent.com/document/product/382/9558#.E7.9F.AD.E4.BF.A1.E5.8F.91.E9.80.81.E6.8F.90.E7.A4.BA.EF.BC.9Aunsupportedoperation.containdomesticandinternationalphonenumber-.E5.A6.82.E4.BD.95.E5.A4.84.E7.90.86.EF.BC.9F)
       * 更多错误，可咨询[腾讯云助手](https://tccc.qcloud.com/web/im/index.html#/chat?webAppId=8fa15978f85cb41f7e2ea36920cb3ae1&title=Sms)
       */

    } catch (TencentCloudSDKException e) {
      log.error(e.getMessage(), e);
      return false;
    }
    return false;
  }

  /**
   * 获取发送短信的响应 该方法负责构造并发送短信请求，然后返回发送短信的响应
   *
   * @param cred 用于调用腾讯云API的凭证
   * @param signName 短信签名内容，必须是已审核通过的签名
   * @param templateId 必须填写已审核通过的模板ID
   * @param phoneNumberSet 下发手机号码数组，采用E.164标准
   * @param templateParam 模板参数数组，需与模板变量个数保持一致
   * @throws TencentCloudSDKException 当调用腾讯云SDK发生错误时抛出
   */
  private static SendSmsResponse sendSms(
      Credential cred,
      String signName,
      String templateId,
      String[] phoneNumberSet,
      String[] templateParam)
      throws TencentCloudSDKException {

    // 实例化短信客户端
    SmsClient client = new SmsClient(cred, "ap-guangzhou");
    /* 实例化一个请求对象，根据调用的接口和实际情况，可以进一步设置请求参数
     * 您可以直接查询SDK源码确定接口有哪些属性可以设置
     * 属性可能是基本类型，也可能引用了另一个数据结构
     * 推荐使用IDE进行开发，可以方便的跳转查阅各个接口和数据结构的文档说明 */
    SendSmsRequest req = new SendSmsRequest();

    /* 填充请求参数,这里request对象的成员变量即对应接口的入参
     * 您可以通过官网接口文档或跳转到request对象的定义处查看请求参数的定义
     * 基本类型的设置:
     * 帮助链接：
     * 短信控制台: https://console.cloud.tencent.com/smsv2
     * 腾讯云短信小助手: https://cloud.tencent.com/document/product/382/3773#.E6.8A.80.E6.9C.AF.E4.BA.A4.E6.B5.81 */

    /* 短信应用ID: 短信SdkAppId在 [短信控制台] 添加应用后生成的实际SdkAppId，示例如1400006666 */
    // 应用 ID 可前往 [短信控制台](https://console.cloud.tencent.com/smsv2/app-manage) 查看
    String sdkAppId = "1400955677";
    req.setSmsSdkAppId(sdkAppId);

    /* 短信签名内容: 使用 UTF-8 编码，必须填写已审核通过的签名 */
    // 签名信息可前往 [国内短信](https://console.cloud.tencent.com/smsv2/csms-sign)
    // 或 [国际/港澳台短信](https://console.cloud.tencent.com/smsv2/isms-sign) 的签名管理查看
    req.setSignName(signName);

    /* 模板 ID: 必须填写已审核通过的模板 ID */
    // 模板 ID 可前往 [国内短信](https://console.cloud.tencent.com/smsv2/csms-template)
    // 或 [国际/港澳台短信](https://console.cloud.tencent.com/smsv2/isms-template) 的正文模板管理查看
    req.setTemplateId(templateId);

    /* 模板参数: 模板参数的个数需要与 TemplateId 对应模板的变量个数保持一致，若无模板参数，则设置为空 */
    req.setTemplateParamSet(templateParam);

    /* 下发手机号码，采用 E.164 标准，+[国家或地区码][手机号]
     * 示例如：+8613711112222， 其中前面有一个+号 ，86为国家码，13711112222为手机号，最多不要超过200个手机号 */
    req.setPhoneNumberSet(phoneNumberSet);

    /* 通过 client 对象调用 SendSms 方法发起请求。注意请求方法名与请求对象是对应的
     * 返回的 res 是一个 SendSmsResponse 类的实例，与请求对象对应 */
    return client.SendSms(req);
  }
}
