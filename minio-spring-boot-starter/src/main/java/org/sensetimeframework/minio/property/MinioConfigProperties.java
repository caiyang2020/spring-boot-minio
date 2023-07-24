package org.sensetimeframework.minio.property;

import lombok.Data;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Data
@Validated
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioConfigProperties {
    /**
     * 服务地址
     */
    @NotEmpty(message = "minio服务地址不可为空")
    @URL(message = "minio服务地址格式错误")
    private String endpoint;
    /**
     * 认证账户
     */
    @NotEmpty(message = "minio认证账户不可为空")
    private String accessKey;
    /**
     * 认证密码
     */
    @NotEmpty(message = "minio认证密码不可为空")
    private String secretKey;
    /**
     * 设置HTTP连接、写入和读取超时。值为0意味着没有超时
     * HTTP连接超时，以毫秒为单位。
     */
    private long connectTimeout;
    /**
     * 设置HTTP连接、写入和读取超时。值为0意味着没有超时
     * HTTP写超时，以毫秒为单位。
     */
    private long writeTimeout;
    /**
     * 设置HTTP连接、写入和读取超时。值为0意味着没有超时
     * HTTP读取超时，以毫秒为单位。
     */
    private long readTimeout;
    /**
     * 创建bucket时是否设置为可读写(public)
     */
    private boolean bucketPublic;
}
