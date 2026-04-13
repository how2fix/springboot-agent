package com.example.mockserver.service;

import com.example.mockserver.model.MockApiDefinition;
import com.example.mockserver.utils.ExpressionEvaluator;
import com.example.mockserver.utils.RequestBodyUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 响应内容生成服务
 */
@Slf4j
@Service
public class ResponseGenerator {

    private final ExpressionEvaluator expressionEvaluator;

    public ResponseGenerator(ExpressionEvaluator expressionEvaluator) {
        this.expressionEvaluator = expressionEvaluator;
    }

    /**
     * 生成最终的响应内容
     */
    public Object generateResponse(MockApiDefinition api, HttpServletRequest request) {
        Object response = api.getResponse();
        if (response == null) {
            return null;
        }

        // 获取所有请求参数
        Map<String, Object> params = new HashMap<>();
        // 1. 获取URL参数和表单参数
        request.getParameterMap().forEach((k, v) -> params.put(k, v.length > 0 ? v[0] : null));
        // 2. 解析JSON Body参数
        if ("application/json".equalsIgnoreCase(request.getContentType())) {
            try {
                String body = RequestBodyUtils.getRequestBody(request);
                Map<String, Object> jsonParams = RequestBodyUtils.parseJsonBody(body);
                params.putAll(jsonParams);
                log.info("普通请求JSON参数: {}", jsonParams);
            } catch (IOException e) {
                log.warn("解析普通请求JSON Body失败: {}", e.getMessage());
            }
        }
        log.info("普通请求所有参数: {}", params);

        Object result;
        // 处理字符串类型的响应，支持表达式
        if (response instanceof String) {
            result = expressionEvaluator.evaluate((String) response, request, params);
        } else {
            // 其他类型直接返回
            result = response;
        }
        log.info("普通请求响应结果: {}", result);
        return result;
    }
}
