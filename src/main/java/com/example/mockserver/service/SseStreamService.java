package com.example.mockserver.service;

import com.example.mockserver.model.MockApiDefinition;
import com.example.mockserver.model.SseConfig;
import com.example.mockserver.utils.ExpressionEvaluator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * SSE流式返回服务
 */
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
            request.getParameterMap().forEach((k, v) -> params.put(k, v.length > 0 ? v[0] : null));

            List<String> chunks = sseConfig.getChunks();
            if (chunks != null) {
                for (String chunk : chunks) {
                    // 处理表达式
                    Object content = expressionEvaluator.evaluate(chunk, request, params);
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
