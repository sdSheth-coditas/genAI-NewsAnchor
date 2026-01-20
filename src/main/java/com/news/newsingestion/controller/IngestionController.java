package com.news.newsingestion.controller;

import com.news.newsingestion.model.Topic;
import com.news.newsingestion.repository.TopicRepository;
import com.news.newsingestion.service.NewsAnchorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/news")
@Slf4j
@RequiredArgsConstructor
public class IngestionController {

    private final NewsAnchorService service;
    private final TopicRepository topicRepository;

    @PostMapping("ingest/{topic}")
    public ResponseEntity<String> ingest(@PathVariable String topic) {
        service.ingest(topic);
        return ResponseEntity.ok("Ingestion started");
    }

    @GetMapping("summarize/{topic}")
    public ResponseEntity<String> summarize(@PathVariable String topic) {

        log.info("Summarization request received for topic={}", topic);
        Optional<Topic> dbTopicOpt = topicRepository.findByName(topic);
        if(dbTopicOpt.isPresent()) {
            String summary = service.summarizeTopic(dbTopicOpt.get());
            return ResponseEntity.ok(summary);
        }else{
            throw new RuntimeException("Topic not found");
        }
    }

    @GetMapping("audio/summarize/{topic}")
    public ResponseEntity<String> summarizeAudio(@PathVariable String topic) {

        log.info("Audio Summarization request received for topic={}", topic);
        Optional<Topic> dbTopicOpt = topicRepository.findByName(topic);
        if(dbTopicOpt.isPresent()) {
            service.processNewsToAudio(dbTopicOpt.get());
            return ResponseEntity.ok("News audio processed");
        }else{
            throw new RuntimeException("Topic not found");
        }
    }
}