package com.example.deepseekdemo.dto;

import lombok.Data;
import java.util.List;

@Data
public class ChatDTO {
    private List<ChatMessage> messages;

    @Data
    public static class ChatMessage {
        // role: system / user / assistant
        private String role;
        private String content;
    }
}
