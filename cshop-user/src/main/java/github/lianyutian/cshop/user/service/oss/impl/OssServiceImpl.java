package github.lianyutian.cshop.user.service.oss.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectResult;
import github.lianyutian.cshop.common.utils.CommonUtil;
import github.lianyutian.cshop.user.service.oss.OssService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * 对象存储实现
 *
 * @author lianyutian
 * @since 2024-12-23 14:06:09
 * @version 1.0
 */
@Slf4j
@Service
public class OssServiceImpl implements OssService {

    @Value("${oss.aliyun.secretId}")
    private String secretId;

    @Value("${oss.aliyun.secretKey}")
    private String secretKey;

    @Value("${oss.aliyun.bucketName}")
    private String bucketName;

    @Value("${oss.aliyun.endPoint}")
    private String endPoint;

    @Override
    public String uploadUserAvatar(MultipartFile file) {
        // 获取原始文件名
        String originalFilename = Objects.requireNonNullElse(file.getOriginalFilename(), "default_filename");
        // 获取日期格式，上传时作为目录名使用
        LocalDateTime localDateTime = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        // OSS 上传存储路径：user/2024/12/23/xxx.jpg
        String folder = dateTimeFormatter.format(localDateTime);
        // 获取扩展名
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        // 新文件名，通过 uuid 生成文件名，
        String fileName = "user" + "/" + folder + "/" + CommonUtil.generateUUID() + extension;

        OSS ossClient = new OSSClientBuilder().build(endPoint, secretId, secretKey);
        try (InputStream inputStream = file.getInputStream()) {
            // 写入 OSS
            PutObjectResult putObjectResult = ossClient.putObject(bucketName, fileName, inputStream);
            // 返回图片路径
            if (putObjectResult != null) {
                return "https://" + bucketName + "." + endPoint + "/" + fileName;
            } else {
                log.error("用户微服务-上传模块-文件上传失败，putObjectResult 为空");
                return null;
            }
        } catch (IOException e) {
            log.error("用户微服务-上传模块-文件上传失败：{}", e.getMessage(), e);
            return null;
        } finally {
            ossClient.shutdown();
        }
    }
}
