package org.sensetimeframework.minio.config;

import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.sensetimeframework.minio.property.MinioConfigProperties;
import org.sensetimeframework.minio.service.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConditionalOnClass(Template.class)
@EnableConfigurationProperties(MinioConfigProperties.class)
public class MinioConfig {
    @Autowired
    private MinioConfigProperties minioConfigProperties;

    @Bean
    public MinioClient minioClient() {
        MinioClient minioClient = MinioClient
                .builder()
                .endpoint(minioConfigProperties.getEndpoint())
                .credentials(minioConfigProperties.getAccessKey(), minioConfigProperties.getSecretKey())
                .build();

        minioClient.setTimeout(
                minioConfigProperties.getConnectTimeout(),
                minioConfigProperties.getWriteTimeout(),
                minioConfigProperties.getReadTimeout()
        );

        log.info("MinioClient初始化成功!");

        return minioClient;
    }
}
