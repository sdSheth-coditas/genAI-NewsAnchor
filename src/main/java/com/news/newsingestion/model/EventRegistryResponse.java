package com.news.newsingestion.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class EventRegistryResponse {

    private Articles articles;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class Articles {
        private List<Result> results;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class Result {
        private String title;
        private String body;
    }
}
