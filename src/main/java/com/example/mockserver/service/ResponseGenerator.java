package com.example.mockserver.service;

import com.example.mockserver.model.MockApiDefinition;
import com.example.mockserver.utils.ExpressionEvaluator;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 响应内容生成服务
 */
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
        request.getParameterMap().forEach((k, v) -> params.put(k, v.length > 0 ? v[0] : null));

        // 处理字符串类型的响应，支持表达式
        if (response instanceof String) {
            return expressionEvaluator.evaluate((String) response, request, params);
        }

        // 其他类型直接返回
        return response;
    }
}
