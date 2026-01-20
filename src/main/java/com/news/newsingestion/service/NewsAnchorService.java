package com.news.newsingestion.service;

import com.news.newsingestion.model.EventRegistryResponse;
import com.news.newsingestion.model.NewsTranscript;
import com.news.newsingestion.model.Topic;
import com.news.newsingestion.repository.NewsTranscriptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class NewsAnchorService {

    private static final int TOP_K = 3;

    private final EventRegistryClient client;
    private final ChunkingService chunking;
    private final SummarizationClient summarizationClient;
    private final NewsAudioService newsAudioService;
    private final VectorStore vectorStore;
    private final NewsTranscriptRepository newsTranscriptRepository;



    public void ingest(String topic) {

        EventRegistryResponse response = client.fetch(topic);

        if (response == null || response.getArticles() == null) {
            log.warn("No articles found for topic {}", topic);
            return;
        }

        for (var article : response.getArticles().getResults()) {
            String combined = article.getTitle() + "\n" + article.getBody();
            chunking.streamChunks(combined, chunk -> {

                Document doc = new Document(
                        chunk,
                        Map.of("topic", topic)
                );
                vectorStore.add(List.of(doc));
            });
        }

        log.info("Completed ingestion for topic {}", topic);
    }

    public String summarizeTopic(Topic topic) {

        log.info("Running vector similarity search for topic={}", topic);

        NewsTranscript transcriptOpt = newsTranscriptRepository.findFirstByTopicAndTranscriptDateOrderByCreatedAtDesc(topic, LocalDate.now());

        if(Objects.nonNull(transcriptOpt)){
            return transcriptOpt.getTranscriptText();
        }
        SearchRequest searchRequest = SearchRequest.builder()
                .query(topic.getName())
                .topK(TOP_K)
                .filterExpression("topic == '" + topic.getName() + "'")
                .build();
        List<Document> docs = vectorStore.similaritySearch(searchRequest);

        if (docs.isEmpty()) {
            ingest(topic.getName());
            NewsTranscript newsTranscript = newsTranscriptRepository.findFirstByTopicAndTranscriptDateOrderByCreatedAtDesc(topic, LocalDate.now());
            if (Objects.nonNull(newsTranscript)) {
                return newsTranscript.getTranscriptText();
            }
        }

        List<String> context =
                docs.stream()
                        .map(Document::getText)
                        .toList();

        String summary = summarizationClient.summarize(topic.getName(), context);
        newsTranscriptRepository.save(buildNewsTranscript(topic, summary));
        return summary;
    }

    private NewsTranscript buildNewsTranscript(Topic topic, String summary) {
        return NewsTranscript.builder()
                .topic(topic)
                .transcriptDate(LocalDate.now())
                .language("en")
                .wordCount(summary.length())
                .transcriptText(summary).build();
    }

    public void processNewsToAudio(Topic topic) {
        // 1. Get the text summary from your previous client
        String transcript = summarizeTopic(topic);

        // 2. Convert to audio
        byte[] audioBytes = newsAudioService.generateNewsAudio(transcript);

        // 3. Save or Return
        String filename = "news_" + topic.getName() + "_"+ System.currentTimeMillis() + ".mp3";
        newsAudioService.saveAudioToFile(audioBytes, filename);
    }
}
