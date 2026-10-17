package com.example.deepseekdemo;

import com.example.deepseekdemo.config.DeepSeekConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(DeepSeekConfig.class)
public class DeepseekDemoApplication {

    public static void main(String[] args) {

        SpringApplication.run(DeepseekDemoApplication.class, args);
    }

}
