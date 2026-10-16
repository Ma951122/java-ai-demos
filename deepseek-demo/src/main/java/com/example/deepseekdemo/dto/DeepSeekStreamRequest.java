package com.example.deepseekdemo.dto;

import lombok.Data;
import java.util.List;

@Data
public class DeepSeekStreamRequest {
    private String model;
    private List<Message> messages;
    private boolean stream;

    @Data
    public static class Message {
        private String role;
        private String content;
    }
}
