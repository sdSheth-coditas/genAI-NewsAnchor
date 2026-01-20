package com.news.newsingestion.service;

import com.news.newsingestion.constants.EventRegistryConstants;
import com.news.newsingestion.model.EventRegistryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@Slf4j
public class EventRegistryClient {

    private final WebClient webClient;

    @Value("${eventregistry.apiKey}")
    private String apiKey;

    public EventRegistryClient(WebClient.Builder builder, @Value("${eventregistry.baseUrl}") String baseUrl) {
        this.webClient = builder
                .baseUrl(baseUrl)
                .build();
    }

    public EventRegistryResponse fetch(String topic) {

        String categoryUri = "news/" + topic;

        log.info("Fetching news for categoryUri={}", categoryUri);

        return webClient.get()
                .uri(uri -> uri.path("/api/v1/article/getArticles")
                        .queryParam(EventRegistryConstants.RESULT_TYPE_PARAM, EventRegistryConstants.RESULT_TYPE_ARTICLES)
                        .queryParam(EventRegistryConstants.ARTICLES_COUNT_PARAM, 12)
                        .queryParam(EventRegistryConstants.ARTICLES_SORT_BY_PARAM, EventRegistryConstants.SORT_BY_DATE)
                        .queryParam(EventRegistryConstants.ARTICLE_BODY_LEN_PARAM, 500)
                        .queryParam(EventRegistryConstants.DATA_TYPE_PARAM, EventRegistryConstants.DATA_TYPE_NEWS)
                        .queryParam(EventRegistryConstants.KEYWORD_PARAM, EventRegistryConstants.KEYWORD_INDIA)
                        .queryParam(EventRegistryConstants.CATEGORY_URI_PARAM, categoryUri)
                        .queryParam(EventRegistryConstants.LANG_PARAM, EventRegistryConstants.LANG_ENG)
                        .queryParam(EventRegistryConstants.DATE_START_PARAM,"2026-01-18")
                        .queryParam(EventRegistryConstants.API_KEY_PARAM, apiKey)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(EventRegistryResponse.class)
                .block();

    }
}
