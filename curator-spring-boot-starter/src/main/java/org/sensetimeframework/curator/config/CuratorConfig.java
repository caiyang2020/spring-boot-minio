package org.sensetimeframework.curator.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.sensetimeframework.curator.property.CuratorConfigProperties;
import org.sensetimeframework.curator.service.CuratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConditionalOnClass(CuratorService.class)
@EnableConfigurationProperties(CuratorConfigProperties.class)
public class CuratorConfig {
    @Autowired
    private CuratorConfigProperties curatorConfigProperties;

    @Bean(initMethod = "start")
    public CuratorFramework curatorFramework() {
        return CuratorFrameworkFactory.newClient(
                curatorConfigProperties.getConnectString(),
                curatorConfigProperties.getSessionTimeoutMs(),
                curatorConfigProperties.getConnectionTimeoutMs(),
                new RetryNTimes(curatorConfigProperties.getRetryCount(), curatorConfigProperties.getElapsedTimeMs()));
    }
}
