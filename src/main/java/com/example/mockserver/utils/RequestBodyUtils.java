package com.example.mockserver.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 请求体工具类
 */
public class RequestBodyUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 读取请求体内容
     */
    public static String getRequestBody(HttpServletRequest request) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()))) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }

    /**
     * 解析JSON请求体为Map
     */
    public static Map<String, Object> parseJsonBody(String body) {
        if (body == null || body.trim().isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            // 解析失败返回空Map
            return new HashMap<>();
        }
    }
}
