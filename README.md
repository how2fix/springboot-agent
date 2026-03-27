# 配置化动态Mock接口服务

基于Spring Boot JDK8开发的配置化动态测试接口服务，用于模拟智能体接口返回，支持普通JSON返回和SSE流式返回两种模式，无需编码即可快速添加模拟接口。

## 特性

✅ **完全配置化**：只需要修改YAML配置文件即可添加新的模拟接口，无需编码
✅ **双模式支持**：支持普通JSON响应和SSE流式响应，完美模拟智能体接口返回场景
✅ **路径匹配**：支持Ant风格路径匹配，可以配置通配符接口
✅ **动态内容**：支持SpEL表达式，可以动态生成响应内容（当前时间、随机值、请求参数引用等）
✅ **配置热更新**：修改配置文件无需重启服务，自动生效
✅ **延迟模拟**：支持配置接口响应延迟，模拟网络环境
✅ **错误模拟**：可以配置任意HTTP状态码，模拟各种错误场景

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

## 配置热更新
默认开启配置热更新，修改`mock-config.yml`文件后，系统会在5秒内自动加载新配置，无需重启服务。可以在`application.yml`中关闭热更新或修改扫描间隔。

## 扩展说明
如果需要更复杂的响应逻辑，可以扩展`ResponseGenerator`类，添加自定义的内容生成规则。

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
