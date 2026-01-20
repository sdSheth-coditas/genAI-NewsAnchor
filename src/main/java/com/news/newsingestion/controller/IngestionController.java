package com.news.newsingestion.controller;

import com.news.newsingestion.model.SummaryRequest;
import com.news.newsingestion.repository.TopicRepository;
import com.news.newsingestion.service.NewsAnchorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/news")
@Slf4j
@RequiredArgsConstructor
public class IngestionController {

    private final NewsAnchorService service;
    private final TopicRepository topicRepository;

    @PostMapping("ingest/{topic}")
    public ResponseEntity<String> ingest(@RequestBody SummaryRequest request) {
        service.ingest(request);
        return ResponseEntity.ok("Ingestion started");
    }

    @PostMapping("summarize/{topic}")
    public ResponseEntity<String> summarize(@RequestBody SummaryRequest request) {

        log.info("Summarization request received for topic={}", request);
        String summary = service.summarizeTopic(request);
        if(StringUtils.isBlank(summary)){
            summary = service.ingestAndSummarize(request);
        }
        return ResponseEntity.ok(summary);
    }

    @PostMapping("audio/summarize/{topic}")
    public ResponseEntity<String> summarizeAudio(@RequestBody SummaryRequest request) {
        log.info("Audio Summarization request received for topic={}", request);
        service.processNewsToAudio(request);
        return ResponseEntity.ok("Audio Summarization of News is Ready");
    }
}