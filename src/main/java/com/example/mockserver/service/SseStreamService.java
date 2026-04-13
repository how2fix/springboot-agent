package com.example.mockserver.service;

import com.example.mockserver.model.MockApiDefinition;
import com.example.mockserver.model.SseConfig;
import com.example.mockserver.utils.ExpressionEvaluator;
import com.example.mockserver.utils.RequestBodyUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SSE流式返回服务
 */
@Slf4j
@Service
public class SseStreamService {

    private final ExpressionEvaluator expressionEvaluator;

    public SseStreamService(ExpressionEvaluator expressionEvaluator) {
        this.expressionEvaluator = expressionEvaluator;
    }

    @Async
    public void streamResponse(MockApiDefinition api, HttpServletRequest request, SseEmitter emitter) {
        SseConfig sseConfig = api.getSse();
        try {
            // 延迟处理
            if (api.getDelay() > 0) {
                Thread.sleep(api.getDelay());
            }

            // 获取请求参数
            Map<String, Object> params = new HashMap<>();
            // 1. 获取URL参数和表单参数
            request.getParameterMap().forEach((k, v) -> params.put(k, v.length > 0 ? v[0] : null));
            // 2. 解析JSON Body参数
            if ("application/json".equalsIgnoreCase(request.getContentType())) {
                try {
                    String body = RequestBodyUtils.getRequestBody(request);
                    Map<String, Object> jsonParams = RequestBodyUtils.parseJsonBody(body);
                    params.putAll(jsonParams);
                    log.info("SSE请求JSON参数: {}", jsonParams);
                } catch (IOException e) {
                    log.warn("解析SSE请求JSON Body失败: {}", e.getMessage());
                }
            }
            log.info("SSE请求所有参数: {}", params);

            List<String> chunks = sseConfig.getChunks();
            if (chunks != null) {
                for (String chunk : chunks) {
                    // 处理表达式
                    Object content = expressionEvaluator.evaluate(chunk, request, params);
                    log.info("SSE输出内容: {}", content);
                    emitter.send(SseEmitter.event().data(content));
                    Thread.sleep(sseConfig.getInterval());
                }
            }

            // 发送结束消息
            if (sseConfig.getEndMessage() != null) {
                emitter.send(SseEmitter.event().data(sseConfig.getEndMessage()));
            }

            emitter.complete();
        } catch (IOException | InterruptedException e) {
            emitter.completeWithError(e);
            Thread.currentThread().interrupt();
        }
    }
}
