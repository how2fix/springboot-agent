package com.example.mockserver.service;

import com.example.mockserver.config.MockConfigProperties;
import com.example.mockserver.model.MockApiDefinition;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Mock接口注册和匹配服务
 */
@Service
public class MockApiRegistry {

    private final MockConfigProperties configProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private List<MockApiDefinition> apiDefinitions;

    public MockApiRegistry(MockConfigProperties configProperties) {
        this.configProperties = configProperties;
    }

    @PostConstruct
    public void init() {
        this.apiDefinitions = configProperties.getApis();
    }

    /**
     * 刷新接口配置
     */
    public void refresh() {
        this.apiDefinitions = configProperties.getApis();
    }

    /**
     * 匹配请求对应的接口配置
     */
    public MockApiDefinition matchApi(HttpServletRequest request) {
        String requestPath = request.getRequestURI();
        HttpMethod requestMethod = HttpMethod.resolve(request.getMethod());

        if (apiDefinitions == null) {
            return null;
        }

        for (MockApiDefinition api : apiDefinitions) {
            // 路径匹配
            if (!pathMatcher.match(api.getPath(), requestPath)) {
                continue;
            }
            // 方法匹配，未配置方法则匹配所有
            if (api.getMethod() != null && !api.getMethod().equals(requestMethod)) {
                continue;
            }
            return api;
        }
        return null;
    }
}
