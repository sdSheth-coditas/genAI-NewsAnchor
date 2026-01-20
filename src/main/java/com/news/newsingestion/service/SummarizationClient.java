package com.news.newsingestion.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class SummarizationClient {

    private final ChatClient chatClient;

    public SummarizationClient(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public String summarize(String topic, List<String> contextChunks) {
        String context = String.join("\n\n", contextChunks);

        return chatClient.prompt()
                .system("You are a professional news anchor AI...")
                .user(u -> u.text("Topic: {topic}\nContext: {context}")
                        .param("topic", topic)
                        .param("context", context))
                .call()
                .content(); // Returns the string content directly
    }
}
