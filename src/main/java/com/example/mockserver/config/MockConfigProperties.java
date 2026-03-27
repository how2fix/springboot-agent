package com.example.mockserver.config;

import com.example.mockserver.model.MockApiDefinition;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Mock接口配置属性类
 */
@Data
@Component
@ConfigurationProperties(prefix = "mock")
public class MockConfigProperties {
    /**
     * 配置文件路径，默认classpath:mock-config.yml
     */
    private String configPath = "classpath:mock-config.yml";

    /**
     * 是否启用配置热更新
     */
    private boolean hotReload = true;

    /**
     * 热更新扫描间隔（毫秒）
     */
    private long reloadInterval = 5000;

    /**
     * 模拟接口列表
     */
    private List<MockApiDefinition> apis;
}
