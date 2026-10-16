# java-ai-demos
Java后端集成大模型实战项目，用于技术演示与简历展示。

## ✨ 项目介绍
deepseek-demo：基于 SpringBoot 对接 DeepSeek 大模型，实现 SSE 流式对话接口。
前端页面实时接收模型分段返回文本，模拟ChatGPT打字输出效果。

## 🛠 技术栈
- Java 17
- Spring Boot 3.x
- Maven
- SSE (Server-Sent Events) 流式推送
- OkHttp 调用大模型HTTP接口
- HTML+JS 前端流式展示页面

## 📌 核心功能
1. HTTP 请求封装，调用 DeepSeek 开放API
2. SSE 长连接，服务端逐块返回模型输出内容
3. DTO分层封装请求/响应实体，参数结构化
4. 简易静态前端页面，直观展示流式对话效果
5. 基础异常捕获，处理大模型接口调用异常

## 🚀 本地运行
1. 修改 `deepseek-demo/src/main/resources/application.yml`，填入 DeepSeek API Key
2. Maven 启动 SpringBoot 项目
3. 浏览器访问 `http://localhost:8080/test-stream.html`，体验流式对话

## ⚠️ 注意事项
- API Key 禁止提交到公开仓库；生产环境建议使用环境变量注入密钥
- 本项目仅用于技术学习与演示，未实现用户鉴权，不适合直接线上生产使用
