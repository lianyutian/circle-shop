package github.lianyutian.cshop.user.service.oss;

import org.springframework.web.multipart.MultipartFile;

/**
 * 对象存储服务
 *
 * @author lianyutian
 * @since 2024-12-23 14:04:03
 * @version 1.0
 */
public interface OssService {
  /**
   * 上传用户头像
   *
   * @param file 头像文件
   * @return 头像地址
   */
  String uploadUserAvatar(MultipartFile file);
}
