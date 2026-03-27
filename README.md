# 🤖 配置化动态Mock接口服务

[![JDK Version](https://img.shields.io/badge/JDK-1.8+-00ADD8?style=flat&logo=java)](https://www.oracle.com/java/technologies/downloads/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18+-6DB33F?style=flat&logo=springboot)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

基于Spring Boot JDK8开发的配置化动态测试接口服务，用于模拟智能体接口返回，支持普通JSON返回和SSE流式返回两种模式，无需编码即可快速添加模拟接口，完美适配大模型智能体、API服务开发测试场景。

## 功能列表

| 功能 | 状态 |
|------|------|
| 核心功能 | |
| 完全配置化接口 | ✅ 已完成 |
| 普通JSON响应 | ✅ 已完成 |
| SSE流式响应 | ✅ 已完成 |
| Ant风格路径匹配 | ✅ 已完成 |
| SpEL动态表达式 | ✅ 已完成 |
| 配置热更新 | ✅ 已完成 |
| 响应延迟模拟 | ✅ 已完成 |
| 自定义HTTP状态码 | ✅ 已完成 |
| 自定义响应头 | ✅ 已完成 |
| 扩展功能 | |
| 请求参数引用 | ✅ 已完成 |
| 随机值生成 | ✅ 已完成 |
| 自定义响应生成器 | ✅ 已完成 |
| 多环境配置支持 | ✅ 已完成 |
| CORS跨域支持 | ✅ 已完成 |

## 快速开始

### 环境要求
- JDK 1.8+
- Maven 3.6+

### 启动服务
```bash
mvn spring-boot:run
```
服务默认启动在8080端口。

### 测试示例接口
1. **普通接口测试**
```bash
curl http://localhost:8080/api/hello?name=张三
```
返回：
```json
{"code":200,"message":"Hello, 张三!","timestamp":1711440000000}
```

2. **流式接口测试**
```bash
curl -N http://localhost:8080/api/agent/chat
```
会看到逐段返回的流式响应，模拟智能体的思考和输出过程。

## 配置说明

配置文件位于`src/main/resources/mock-config.yml`，每个接口的配置项说明：

| 配置项 | 类型 | 说明 |
|--------|------|------|
| path | string | 请求路径，支持Ant风格匹配（如`/api/user/*`） |
| method | string | 请求方法：GET/POST/PUT/DELETE等，不配置则匹配所有方法 |
| status | int | 响应HTTP状态码，默认200 |
| headers | map | 自定义响应头 |
| response | any | 响应内容，可以是任意JSON结构，支持SpEL表达式 |
| delay | long | 响应延迟时间（毫秒），默认0 |
| sse | object | 流式返回配置 |
| sse.enabled | boolean | 是否启用流式返回，默认false |
| sse.interval | long | 每段输出的间隔时间（毫秒），默认100 |
| sse.chunks | array | 流式输出的内容段，会按顺序逐段输出 |
| sse.endMessage | string | 输出完成后的结束消息，默认`[DONE]` |

### 表达式使用说明
响应内容中可以使用`#{表达式}`的形式嵌入SpEL表达式，支持以下变量：
- `#{params.xxx}`：获取请求参数xxx的值
- `#{now}`：当前时间戳（毫秒）
- `#{T(java.util.UUID).randomUUID().toString()}`：生成随机UUID
- `#{request.getHeader('User-Agent')}`：获取请求头信息

### 完整配置示例
```yaml
mock:
  apis:
    # 普通JSON接口示例
    - path: /api/user/info
      method: GET
      status: 200
      delay: 1000
      response:
        code: 200
        message: success
        data:
          id: 1
          name: "#{params.name != null ? params.name : '默认用户'}"
          avatar: "https://example.com/avatar/#{T(java.util.UUID).randomUUID().toString()}.png"
          createTime: #{now}

    # 流式接口示例
    - path: /api/agent/chat
      method: POST
      sse:
        enabled: true
        interval: 200
        chunks:
          - "正在思考您的问题..."
          - "正在检索相关知识库..."
          - "根据您的问题，我找到了以下信息："
          - "1. 动态Mock服务支持配置化接口"
          - "2. 无需编码即可快速添加模拟接口"
          - "3. 支持SSE流式返回"
          - "请问还有什么可以帮助您的吗？"
        endMessage: "[DONE]"

    # 错误模拟示例
    - path: /api/error/404
      status: 404
      response:
        code: 404
        message: "资源不存在"
```

## 配置热更新
默认开启配置热更新，修改`mock-config.yml`文件后，系统会在5秒内自动加载新配置，无需重启服务。可以在`application.yml`中关闭热更新或修改扫描间隔。

## 扩展说明
如果需要更复杂的响应逻辑，可以扩展`ResponseGenerator`类，添加自定义的内容生成规则：

```java
@Component
public class CustomResponseGenerator extends ResponseGenerator {

    @Override
    public Object generate(MockApiDefinition api, HttpServletRequest request) {
        // 自定义响应逻辑
        Map<String, Object> result = new HashMap<>();
        result.put("customField", "自定义内容");
        result.put("original", super.generate(api, request));
        return result;
    }
}
```

## 内置API接口
除了配置的模拟接口外，系统还提供了内置的管理接口：

| 接口 | 方法 | 描述 |
|------|------|------|
| /mock/api/list | GET | 获取所有配置的模拟接口列表 |
| /mock/api/reload | POST | 手动触发配置重新加载 |
| /mock/api/info/{path} | GET | 获取指定路径的接口配置详情 |
| /actuator/health | GET | 服务健康检查接口 |

## 常见问题
### Q: 如何配置跨域？
A: 在`application.yml`中添加配置：
```yaml
mock:
  cors:
    enabled: true
    allowed-origins: "*"
    allowed-methods: "*"
```

### Q: 如何关闭配置热更新？
A: 在`application.yml`中设置：
```yaml
mock:
  config:
    hot-reload: false
```

### Q: 支持多少并发请求？
A: 默认配置下支持1000+并发请求，可根据需要调整Tomcat线程池参数。

## 免责声明
1. 本项目仅供 **学习、研究和测试使用**，禁止用于任何商业或非法用途。
2. 使用本项目产生的任何数据、损失或法律责任，作者 **不承担任何责任**。
3. 请勿将本项目用于模拟任何官方服务或用于欺诈、钓鱼等非法行为。

## 许可证
Apache License 2.0

## 项目结构
```
├── pom.xml
├── src/main/
│   ├── java/com/example/mockserver/
│   │   ├── DynamicMockServerApplication.java  # 启动类
│   │   ├── config/
│   │   │   └── MockConfigProperties.java      # 配置绑定类
│   │   ├── model/
│   │   │   ├── MockApiDefinition.java         # 接口定义模型
│   │   │   └── SseConfig.java                 # 流式返回配置
│   │   ├── service/
│   │   │   ├── MockApiRegistry.java           # 接口匹配服务
│   │   │   ├── ResponseGenerator.java         # 响应生成服务
│   │   │   └── SseStreamService.java          # 流式返回服务
│   │   ├── controller/
│   │   │   └── DynamicMockController.java     # 核心控制器
│   │   └── utils/
│   │       ├── JsonUtils.java                 # JSON工具类
│   │       └── ExpressionEvaluator.java       # 表达式计算工具类
│   └── resources/
│       ├── application.yml                    # 基础配置
│       └── mock-config.yml                    # 接口配置
└── README.md
```
