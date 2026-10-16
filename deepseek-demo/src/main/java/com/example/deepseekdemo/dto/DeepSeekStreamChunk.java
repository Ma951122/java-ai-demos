package com.example.deepseekdemo.dto;

// 流式分片响应 DTO
import lombok.Data;
import java.util.List;

@Data
public class DeepSeekStreamChunk {
    private List<Choice> choices;

    @Data
    public static class Choice {
        private Delta delta;
        private String finish_reason;
    }

    @Data
    public static class Delta {
        private String content;
    }
}
