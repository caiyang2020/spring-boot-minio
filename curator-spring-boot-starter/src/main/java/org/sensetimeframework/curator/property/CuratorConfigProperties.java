package org.sensetimeframework.curator.property;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Component
@ConfigurationProperties(prefix = "curator")
public class CuratorConfigProperties {
    /**
     * 服务地址
     */
    @NotEmpty(message = "zookeeper服务地址不可为空")
    private String connectString;
    /**
     * 重试回数
     */
    private int retryCount;
    /**
     * 超时时间
     */
    private int elapsedTimeMs;
    /**
     * 会话超时时间
     */
    private int sessionTimeoutMs;
    /**
     * 连接超时时间
     */
    private int connectionTimeoutMs;
}
