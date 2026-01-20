package com.news.newsingestion.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@Slf4j
public class ChunkingService {

    @Value("${chunking.size}")
    private int chunkSize;

    @Value("${chunking.overlap}")
    private int overlap;

    public void streamChunks(String text, Consumer<String> chunkConsumer) {

        if (text == null || text.isEmpty()) {
            return;
        }

        int length = text.length();
        int start = 0;

        while (start < length) {
            int end = Math.min(start + chunkSize, length);
            chunkConsumer.accept(text.substring(start, end));
            if (end == length) {
                break;
            }
            start = Math.max(end - overlap, 0);
        }
    }
}
