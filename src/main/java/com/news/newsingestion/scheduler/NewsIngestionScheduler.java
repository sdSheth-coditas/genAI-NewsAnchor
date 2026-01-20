package com.news.newsingestion.scheduler;

import com.news.newsingestion.model.Topic;
import com.news.newsingestion.repository.TopicRepository;
import com.news.newsingestion.service.NewsAnchorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NewsIngestionScheduler {

    private final TopicRepository topicRepository;
    private final NewsAnchorService newsAnchorService;

    @Value("${news.ingestion.throttle-delay-ms:5000}")
    private long throttleDelayMs;

    @Scheduled(cron = "${news.ingestion.schedule:0 0 * * * *}")
    public void ingestAllTopics() {
        log.info("Starting scheduled news ingestion job.");
        try {
            List<Topic> topics = topicRepository.findAll();

            if (topics.isEmpty()) {
                log.info("No topics found in the database. Skipping ingestion.");
                return;
            }

            for (Topic topic : topics) {
                processTopic(topic);
                throttle();
                summarizeTopic(topic);
                throttle();
                convertTopic(topic);
            }
        } catch (Exception e) {
            log.error("Unexpected error occurred during global ingestion job", e);
        }
        log.info("Completed scheduled news ingestion job.");
    }

    private void processTopic(Topic topic) {
        try {
            log.info("Ingesting news for topic: {}", topic.getName());
            newsAnchorService.ingest(topic.getName());
        } catch (Exception e) {
            log.error("Failed to ingest news for topic: {}. Continuing with next topic.", topic.getName(), e);
        }
    }

    private void summarizeTopic(Topic topic) {
        try {
            log.info("Summarizing news for topic: {}", topic.getName());
            newsAnchorService.summarizeTopic(topic);
        } catch (Exception e) {
            log.error("Failed to ingest news for topic: {}. Continuing with next topic.", topic.getName(), e);
        }
    }

    private void convertTopic(Topic topic) {
        try {
            log.info("Convert news for topic: {}", topic.getName());
            newsAnchorService.processNewsToAudio(topic);
        } catch (Exception e) {
            log.error("Failed to ingest news for topic: {}. Continuing with next topic.", topic.getName(), e);
        }
    }

    private void throttle() {
        try {
            Thread.sleep(throttleDelayMs);
        } catch (InterruptedException e) {
            log.warn("Ingestion throttle interrupted", e);
            Thread.currentThread().interrupt();
        }
    }
}
