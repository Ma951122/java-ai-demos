package com.example.deepseekdemo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.example.deepseekdemo.dto.ChatDTO;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
public class DeepSeekStreamService {

    @Value("${deepseek.api-key}")
    private String apiKey;

    @Value("${deepseek.api-url}")
    private String apiUrl;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public void streamChat(List<ChatDTO.ChatMessage> messages, SseEmitter emitter) throws IOException, InterruptedException {
        // 组装请求体，stream:true开启流式，messages携带全部历史实现多轮对话
        String reqBody = """
                {
                  "model": "deepseek-chat",
                  "stream": true,
                  "messages": %s
                }
                """.formatted(buildMessagesJson(messages));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(reqBody))
                .build();

        HttpResponse<Stream<String>> response = httpClient.send(request, BodyHandlers.ofLines());

        // 逐行解析SSE流
        response.body().forEach(line -> {
            try {
                log.debug("sse raw:{}", line);
                if (!line.startsWith("data: ")) {
                    return;
                }
                String dataStr = line.substring("data: ".length()).trim();
                if ("[DONE]".equals(dataStr)) {
                    log.info("流式对话结束");
                    return;
                }
                // 把分片文本推送给前端SSE
                emitter.send(SseEmitter.event()
                        .name("message")
                        .data(dataStr));
            } catch (Exception e) {
                log.error("sse发送异常", e);
            }
        });
    }

    /**
     * 把List<ChatMessage>转json字符串，简易序列化（生产建议直接用ObjectMapper）
     */
    private String buildMessagesJson(List<ChatDTO.ChatMessage> messages) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < messages.size(); i++) {
            ChatDTO.ChatMessage msg = messages.get(i);
            sb.append("{")
                    .append("\"role\":\"").append(msg.getRole()).append("\",")
                    .append("\"content\":\"").append(escapeJson(msg.getContent())).append("\"")
                    .append("}");
            if (i != messages.size() -1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    // json简单转义，避免引号破坏json
    private String escapeJson(String str) {
        if(str == null) return "";
        return str.replace("\"", "\\\"");
    }
}
