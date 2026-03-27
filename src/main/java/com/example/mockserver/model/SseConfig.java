package com.example.mockserver.model;

import lombok.Data;
import java.util.List;

/**
 * SSE流式返回配置
 */
@Data
public class SseConfig {
    /**
     * 是否启用流式返回
     */
    private boolean enabled = false;

    /**
     * 每段输出的间隔时间（毫秒）
     */
    private long interval = 100;

    /**
     * 流式输出的内容段，会按顺序逐段输出
     */
    private List<String> chunks;

    /**
     * 输出完成后的结束消息
     */
    private String endMessage = "[DONE]";
}
