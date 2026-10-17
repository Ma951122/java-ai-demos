# DeepSeek Demo

基于 Spring Boot 3 + Java HttpClient 实现 DeepSeek 大模型 API 的 SSE 流式多轮对话，用于技术演示与简历展示。

## 项目简介

本项目演示如何通过后端服务对接 DeepSeek 开放平台接口，实现：

- 大模型流式对话（Server-Sent Events，逐字推送）
- 多轮对话（自动携带完整历史消息上下文）
- 配置外置化（API Key / 接口地址 / 模型名通过 `application.yml` 管理，不硬编码）

## 技术栈

| 技术 | 说明 |
|------|------|
| Java 17+ | 使用 `java.net.http.HttpClient`，需 JDK 11 及以上 |
| Spring Boot 3.x | Web 容器 + SSE 支持（`SseEmitter`） |
| Jackson | JSON 序列化与 SSE 数据解析 |
| Lombok | 简化实体代码 |
| DeepSeek API | OpenAI 兼容的 Chat Completions 接口 |

## 功能特性

- ✅ 流式输出：模型回复逐字/逐段实时推送给前端，打字机效果
- ✅ 多轮对话：前端每次请求携带完整消息历史，模型具备上下文记忆
- ✅ 配置外置：API Key、接口地址、模型名全部在 `application.yml` 中配置
- ✅ 异常兜底：接口异常、余额不足等情况向前端返回错误提示

## 项目结构

deepseek-demo
├── pom.xml
└── src/main
├── java/com/example/deepseekdemo
│   ├── DeepseekDemoApplication.java   # 启动类
│   ├── config
│   │   └── DeepSeekConfig.java        # 配置类（读取 yml 中 deepseek.* 配置）
│   ├── controller
│   │   └── AiStreamController.java    # SSE 流式对话接口
│   └── dto
│       └── ChatDTO.java               # 请求参数封装
└── resources
└── application.yml                # 应用配置（含 DeepSeek 配置）


## 快速开始

### 环境要求

- JDK 17+（JDK 11 及以上即可）
- Maven 3.6+
- DeepSeek 开放平台账号（已充值余额）

### 1. 获取 DeepSeek API Key

1. 访问 [DeepSeek 开放平台](https://platform.deepseek.com/) 并登录
2. 进入 **API Keys** 页面，创建一个新的 API Key（格式：`sk-` 开头）
3. 确认账户有余额（余额不足会返回 `402 Insufficient Balance`）

### 2. 修改配置文件

编辑 `src/main/resources/application.yml`：

```yaml
server:
  port: 8080

deepseek:
  api-key: sk-你的真实APIKey          # 替换为你的真实 Key
  api-url: https://api.deepseek.com/v1/chat/completions
  model: deepseek-flash

