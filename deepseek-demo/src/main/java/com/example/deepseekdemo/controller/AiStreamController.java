package com.example.deepseekdemo.controller;

import com.example.deepseekdemo.dto.ChatDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/chat")
public class AiStreamController {

    @Value("${deepseek.api-url}")
    private String apiUrl;

    @Value("${deepseek.api-key}")
    private String apiKey;

    @Value("${deepseek.model}")
    private String model;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AiStreamController() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @PostMapping("/stream")
    public SseEmitter streamChat(@RequestBody ChatDTO chatDTO, HttpServletResponse response) {
        SseEmitter emitter = new SseEmitter(0L);

        new Thread(() -> {
            try {
                // 构建DeepSeek请求体Map
                Map<String, Object> reqMap = new HashMap<>();
                reqMap.put("model", model);
                reqMap.put("stream", true);
                reqMap.put("messages", chatDTO.getMessages());
                // Jackson序列化，自动生成标准JSON，不会引号出错！
                String reqBody = objectMapper.writeValueAsString(reqMap);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl))
                        .header("Authorization", "Bearer " + apiKey)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(reqBody))
                        .timeout(Duration.ofSeconds(60))
                        .build();

                HttpResponse<java.util.stream.Stream<String>> resp = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofLines());
                int statusCode = resp.statusCode();

                if (statusCode != 200) {
                    String tip;
                    if (statusCode == 402) {
                        tip = "模型调用失败：账户余额不足，请充值后重试";
                    } else if (statusCode == 401) {
                        tip = "模型调用失败：API Key无效、过期或者填写错误";
                    } else if (statusCode == 422) {
                        tip = "模型调用失败：请求参数错误，请检查model/messages字段";
                    } else if (statusCode == 429) {
                        tip = "模型调用失败：请求频率超限，请稍后重试";
                    } else if (statusCode == 400) {
                        tip = "模型调用失败：请求JSON格式错误";
                    } else {
                        tip = "接口调用失败，状态码:" + statusCode;
                    }
                    emitter.send(SseEmitter.event().data("{\"content\":\"" + tip + "\"}"));
                    emitter.complete();
                    return;
                }

                resp.body().forEach(line -> {
                    try {
                        if (line.startsWith("data: ")) {
                            String data = line.substring(6);
                            if ("[DONE]".equals(data)) {
                                emitter.complete();
                                return;
                            }
                            emitter.send(SseEmitter.event().data(data));
                        }
                    } catch (IOException e) {
                        log.error("SSE发送异常", e);
                        emitter.completeWithError(e);
                    }
                });

            } catch (JsonProcessingException e) {
                log.error("序列化请求JSON失败", e);
                sendErrorMsg(emitter, "请求JSON序列化异常：" + e.getMessage());
            } catch (Exception e) {
                log.error("调用大模型接口异常", e);
                sendErrorMsg(emitter, "服务异常：" + e.getMessage());
            }
        }).start();
        return emitter;
    }

    private void sendErrorMsg(SseEmitter emitter, String msg) {
        try {
            emitter.send(SseEmitter.event().data("{\"content\":\"" + msg + "\"}"));
        } catch (IOException ex) {
        }
        emitter.completeWithError(new RuntimeException(msg));
    }
}
