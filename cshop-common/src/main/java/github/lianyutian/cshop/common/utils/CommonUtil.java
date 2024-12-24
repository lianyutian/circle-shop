package github.lianyutian.cshop.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * 通用工具类
 *
 * @author lianyutian
 * @since 2024-12-17 14:57:04
 * @version 1.0
 */
@Slf4j
public class CommonUtil {
    /**
     * 加密串
     */
    private static final String SECRET_STRING = "./0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    /**
     * 获取客户端 ip
     *
     * @param request HTTP 请求对象，用于获取客户端 IP 地址
     * @return 客户端的 IP 地址
     * @throws RuntimeException 如果获取 IP 地址失败
     */
    public static String getRemoteIpAddr(HttpServletRequest request) {
        String ipAddress;
        try {
            // 尝试从请求头中获取客户端 IP 地址
            ipAddress = request.getHeader("x-forwarded-for");
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                // 如果请求头中没有 IP 地址信息，则直接获取远程地址
                ipAddress = request.getRemoteAddr();
                if ("127.0.0.1".equals(ipAddress)) {
                    // 如果是本地地址，则尝试获取本地主机的 IP 地址
                    InetAddress inet = InetAddress.getLocalHost();
                    ipAddress = inet.getHostAddress();
                }
            }
            // 处理通过多个代理的情况，提取出真实的客户端 IP 地址
            if (ipAddress != null && ipAddress.length() > 15) {
                if (ipAddress.indexOf(",") > 0) {
                    ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
                }
            }
            if (ipAddress == null || ipAddress.isEmpty()) {
                throw new RuntimeException("Failed to get remote IP address");
            }
        } catch (UnknownHostException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Failed to get local host IP address", e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Failed to get remote IP address", e);
        }
        return ipAddress;
    }

    /**
     * MD5 加密
     *
     * @param data 需要加密的数据
     * @return 加密后的数据
     * @throws RuntimeException 如果加密失败
     */
    public static String MD5(String data) {
        try {
            // 创建 MD5 加密器实例
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 对数据进行加密处理
            byte[] array = md.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte item : array) {
                // 将加密后的字节数组转换为十六进制字符串
                sb.append(Integer.toHexString((item & 0xFF) | 0x100), 1, 3);
            }
            // 返回加密后的字符串
            return sb.toString().toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Failed to create MD5 digest", e);
        }
    }

    /**
     * 生成指定长度的数字验证码
     *
     * @param length 验证码的长度
     * @return 生成的数字验证码
     */
    public static String getRandomCode(int length) {
        String numbers = "0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * numbers.length());
            sb.append(numbers.charAt(index));
        }
        return sb.toString();
    }

    /**
     * 获取当前时间戳
     *
     * @return 当前时间戳
     */
    public static long getCurrentTimestamp(){
        return System.currentTimeMillis();
    }

    /**
     * 生成 UUID
     *
     * @return 生成的 UUID 字符串
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
