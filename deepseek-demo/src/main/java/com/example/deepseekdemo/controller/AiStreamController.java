package com.example.deepseekdemo.controller;

import com.example.deepseekdemo.dto.ChatDTO;
import com.example.deepseekdemo.service.DeepSeekStreamService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class AiStreamController {

    private final DeepSeekStreamService deepSeekStreamService;

    /**
     * SSE 多轮流式对话
     * @param chatDTO 携带messages历史对话
     * @return SseEmitter
     */
    @PostMapping("/api/chat/stream")
    public SseEmitter chatStream(@RequestBody ChatDTO chatDTO) {
        // 设置超时时间，0=永不超时，单位ms
        SseEmitter emitter = new SseEmitter(0L);

        // 异步调用，防止阻塞controller
        new Thread(() -> {
            try {
                deepSeekStreamService.streamChat(chatDTO.getMessages(), emitter);
            } catch (Exception e) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("服务异常：" + e.getMessage()));
                } catch (IOException ex) {
                }
                emitter.completeWithError(e);
            } finally {
                emitter.complete();
            }
        }).start();
        return emitter;
    }
}
