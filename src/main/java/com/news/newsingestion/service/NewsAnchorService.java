package com.news.newsingestion.service;

import com.news.newsingestion.model.EventRegistryResponse;
import com.news.newsingestion.model.NewsTranscript;
import com.news.newsingestion.model.SummaryRequest;
import com.news.newsingestion.repository.NewsTranscriptRepository;
import com.news.newsingestion.repository.TopicRepository;
import com.news.newsingestion.utils.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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



    public void ingest(SummaryRequest request) {

        EventRegistryResponse response = client.fetch(request);

        if (response == null || response.getArticles() == null) {
            log.warn("No articles found for topic {}", request);
            return;
        }

        for (var article : response.getArticles().getResults()) {
            String combined = article.getTitle() + "\n" + article.getBody();
            chunking.streamChunks(combined, chunk -> {

                Document doc = new Document(
                        chunk,
                        Map.of("topic", request.getTopic(), "title", article.getTitle(),
                                "date", HashUtil.dateToString(request.getDate()))
                );
                vectorStore.add(List.of(doc));
            });
        }

        log.info("Completed ingestion for topic {}", request.getTopic());
    }

    public String summarizeTopic(SummaryRequest request) {

        log.info("Running vector similarity search for topic={}", request);

        NewsTranscript transcriptOpt = newsTranscriptRepository.findFirstByTopicAndTranscriptDateOrderByCreatedAtDesc(request.getTopic(), request.getDate());

        if(Objects.nonNull(transcriptOpt)){
            return transcriptOpt.getTranscriptText();
        }


        FilterExpressionBuilder fb =  new FilterExpressionBuilder();
        Filter.Expression filter = null;
        if(Objects.nonNull(request.getDate())){
            filter = fb.or(fb.eq("date", request.getDate().toString())
                    , fb.eq("topic", request.getTopic())).build();
        }else{
            filter = fb.eq("topic", request.getTopic()).build();
        }
        SearchRequest.Builder builder = SearchRequest.builder()
                .query(request.getTopic())
                .topK(TOP_K);
        if(Objects.nonNull(request.getDate())){
            builder.filterExpression(filter);
        }
        SearchRequest searchRequest = builder.build();
        List<Document> docs = vectorStore.similaritySearch(searchRequest);

        List<String> context = docs.stream().map(Document::getText).toList();

        String summary = summarizationClient.summarize(request.getTopic(), context);

        newsTranscriptRepository.save(buildNewsTranscript(request.getTopic(), summary));
        return summary;
    }

    public String ingestAndSummarize(SummaryRequest request) {

        log.info("Running ingestAndSummarize for topic={}", request);

        SearchRequest searchRequest = SearchRequest.builder()
                .query(request.getTopic())
                .topK(TOP_K)
                .build();
        List<Document> docs = vectorStore.similaritySearch(searchRequest);
        log.info("Found {} docs for topic {} in vectorStore", docs.size(), request);
        if (docs.isEmpty()) {
            ingest(request);
            NewsTranscript newsTranscript = newsTranscriptRepository.findFirstByTopicAndTranscriptDateOrderByCreatedAtDesc(request.getTopic(), LocalDate.now());
            if (Objects.nonNull(newsTranscript)) {
                return newsTranscript.getTranscriptText();
            }
        }

        List<String> context = docs.stream().map(Document::getText).toList();
        String summary = summarizationClient.summarize(request.getTopic(), context);
        newsTranscriptRepository.save(buildNewsTranscript(request.getTopic(), summary));
        return summary;
    }

    private NewsTranscript buildNewsTranscript(String topic, String summary) {
        return NewsTranscript.builder()
                .topic(topic)
                .transcriptDate(LocalDate.now())
                .language("en")
                .wordCount(summary.length())
                .transcriptText(summary).build();
    }

    public void processNewsToAudio(SummaryRequest request) {
        // 1. Get the text summary from your previous client
        String transcript = summarizeTopic(request);

        // 2. Convert to audio
        byte[] audioBytes = newsAudioService.generateNewsAudio(transcript);

        // 3. Save or Return
        String filename = "news_" + request.getTopic() + "_"+ System.currentTimeMillis() + ".mp3";
        newsAudioService.saveAudioToFile(audioBytes, filename);
    }
}
