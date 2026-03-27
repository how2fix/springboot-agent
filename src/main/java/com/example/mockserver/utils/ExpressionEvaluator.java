package com.example.mockserver.utils;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * SpEL表达式计算工具类
 */
@Component
public class ExpressionEvaluator {

    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * 计算表达式
     * @param expression 表达式字符串，包含#{}包裹的SpEL表达式
     * @param request HTTP请求对象
     * @param params 请求参数
     * @return 计算后的结果
     */
    public Object evaluate(String expression, HttpServletRequest request, Map<String, Object> params) {
        if (!expression.contains("#{")) {
            return expression;
        }

        EvaluationContext context = new StandardEvaluationContext();
        context.setVariable("request", request);
        context.setVariable("params", params);
        context.setVariable("now", System.currentTimeMillis());

        String result = expression;
        while (result.contains("#{")) {
            int start = result.indexOf("#{");
            int end = result.indexOf("}", start);
            if (end == -1) {
                break;
            }
            String expr = result.substring(start + 2, end);
            Object value = parser.parseExpression(expr).getValue(context);
            result = result.substring(0, start) + (value != null ? value.toString() : "") + result.substring(end + 1);
        }
        return result;
    }
}
