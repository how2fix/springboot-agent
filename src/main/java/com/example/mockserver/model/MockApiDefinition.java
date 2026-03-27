package com.example.mockserver.model;

import lombok.Data;
import org.springframework.http.HttpMethod;
import java.util.Map;

/**
 * 模拟接口定义
 */
@Data
public class MockApiDefinition {
    /**
     * 请求路径，支持Ant风格路径匹配
     */
    private String path;

    /**
     * 请求方法，GET/POST/PUT/DELETE等，默认支持所有方法
     */
    private HttpMethod method;

    /**
     * 响应状态码，默认200
     */
    private int status = 200;

    /**
     * 响应头
     */
    private Map<String, String> headers;

    /**
     * 响应内容，支持SpEL表达式
     */
    private Object response;

    /**
     * 响应延迟时间（毫秒），默认0
     */
    private long delay = 0;

    /**
     * SSE流式返回配置
     */
    private SseConfig sse;
}
