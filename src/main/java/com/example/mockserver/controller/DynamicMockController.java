package com.example.mockserver.controller;

import com.example.mockserver.model.MockApiDefinition;
import com.example.mockserver.service.MockApiRegistry;
import com.example.mockserver.service.ResponseGenerator;
import com.example.mockserver.service.SseStreamService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 动态Mock控制器，处理所有请求
 */
@RestController
public class DynamicMockController {

    private final MockApiRegistry apiRegistry;
    private final ResponseGenerator responseGenerator;
    private final SseStreamService sseStreamService;

    public DynamicMockController(MockApiRegistry apiRegistry,
                                 ResponseGenerator responseGenerator,
                                 SseStreamService sseStreamService) {
        this.apiRegistry = apiRegistry;
        this.responseGenerator = responseGenerator;
        this.sseStreamService = sseStreamService;
    }

    @RequestMapping("/**")
    public Object handleRequest(HttpServletRequest request) throws InterruptedException {
        // 匹配接口配置
        MockApiDefinition api = apiRegistry.matchApi(request);
        if (api == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("code", 404, "message", "未找到对应的Mock接口配置"));
        }

        // 延迟处理
        if (api.getDelay() > 0 && (api.getSse() == null || !api.getSse().isEnabled())) {
            Thread.sleep(api.getDelay());
        }

        // 处理流式返回
        if (api.getSse() != null && api.getSse().isEnabled()) {
            SseEmitter emitter = new SseEmitter();
            sseStreamService.streamResponse(api, request, emitter);
            return emitter;
        }

        // 处理普通返回
        Object response = responseGenerator.generateResponse(api, request);
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(api.getStatus());
        if (api.getHeaders() != null) {
            api.getHeaders().forEach(builder::header);
        }
        return builder.body(response);
    }
}
