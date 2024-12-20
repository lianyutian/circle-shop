package github.lianyutian.cshop.user.service.sms.impl;

import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dysmsapi20170525.AsyncClient;
import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsResponse;
import com.google.gson.Gson;
import darabonba.core.client.ClientOverrideConfiguration;
import github.lianyutian.cshop.user.service.sms.SmsCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 阿里云短信服务实现类
 *
 * @author lianyutian
 * @since 2024-12-19 08:42:48
 * @version 1.0
 */
@Service
@Slf4j
public class AliyunSmsCodeServiceImpl implements SmsCodeService {

    @Value("${sms.aliyun.secretId}")
    private String secretId;

    @Value("${sms.aliyun.secretKey}")
    private String secretKey;

    @Value("${sms.aliyun.signName}")
    private String signName;

    @Override
    public boolean sendSmsCode(String phone, String templateId, String[] templateParam) {
        // 配置认证信息，包括访问密钥ID、访问密钥密钥
        StaticCredentialProvider provider = StaticCredentialProvider.create(Credential.builder()
                .accessKeyId(secretId)
                .accessKeySecret(secretKey)
                .build());

        try (AsyncClient client = AsyncClient.builder()
                //.httpClient(httpClient) // 使用配置的 HttpClient，否则使用默认的 HttpClient (Apache HttpClient)
                .credentialsProvider(provider)
                //.serviceConfiguration(Configuration.create()) // 服务级别配置
                // 客户端级别配置重写，可以设置 Endpoint、HTTP 请求参数等
                .overrideConfiguration(
                        ClientOverrideConfiguration.create()
                                // Endpoint 请参考 https://api.aliyun.com/product/Dysmsapi
                                .setEndpointOverride("dysmsapi.aliyuncs.com")
                                .setConnectTimeout(Duration.ofSeconds(30))
                )
                .build()
        ) {

            // 设置API请求参数
            SendSmsRequest sendSmsRequest = SendSmsRequest.builder()
                    .phoneNumbers(phone)
                    .signName(signName)
                    .templateCode(templateId)
                    .templateParam("{\"code\":\"" + templateParam[0] + "\"}")
                    // 请求级别配置重写，可以设置 HTTP 请求参数等
                    // .requestConfiguration(RequestConfiguration.create().setHttpHeaders(new HttpHeaders()))
                    .build();

            // 异步获取API请求的返回值
            CompletableFuture<SendSmsResponse> response = client.sendSms(sendSmsRequest);
            // 同步获取API请求的返回值
            SendSmsResponse resp = response.get();
            log.info("短信模块-短信发送结果：{}", new Gson().toJson(resp));

            String code = resp.getBody().getCode();
            if (Objects.equals(code, "OK")) {
                return true;
            }
        } catch (ExecutionException | InterruptedException e) {
            log.error(e.getMessage(), e);
            return false;
        }
        return false;
    }

}

