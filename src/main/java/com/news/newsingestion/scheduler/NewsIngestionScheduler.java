package com.news.newsingestion.scheduler;

import com.news.newsingestion.model.SummaryRequest;
import com.news.newsingestion.model.Topic;
import com.news.newsingestion.repository.TopicRepository;
import com.news.newsingestion.service.NewsAnchorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
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
                SummaryRequest request = new SummaryRequest(topic.getName(), LocalDate.now());
                processTopic(request);
                throttle();
                summarizeTopic(request);
                throttle();
                convertTopic(request);
            }
        } catch (Exception e) {
            log.error("Unexpected error occurred during global ingestion job", e);
        }
        log.info("Completed scheduled news ingestion job.");
    }

    private void processTopic(SummaryRequest request) {
        try {
            log.info("Ingesting news for topic: {}", request.getTopic());
            newsAnchorService.ingest(request);
        } catch (Exception e) {
            log.error("Failed to ingest news for topic: {}. Continuing with next topic.", request.getTopic(), e);
        }
    }

    private void summarizeTopic(SummaryRequest request) {
        try {
            log.info("Summarizing news for topic: {}", request.getTopic());
            newsAnchorService.summarizeTopic(request);
        } catch (Exception e) {
            log.error("Failed to ingest news for topic: {}. Continuing with next topic.", request.getTopic(), e);
        }
    }

    private void convertTopic(SummaryRequest request) {
        try {
            log.info("Convert news for topic: {}", request.getTopic());
            newsAnchorService.processNewsToAudio(request);
        } catch (Exception e) {
            log.error("Failed to ingest news for topic: {}. Continuing with next topic.", request.getTopic(), e);
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
