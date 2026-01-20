package com.news.newsingestion.repository;

import com.news.newsingestion.model.NewsTranscript;
import com.news.newsingestion.model.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NewsTranscriptRepository extends JpaRepository<NewsTranscript, UUID> {

    NewsTranscript findFirstByTopicAndTranscriptDateOrderByCreatedAtDesc(String topic, LocalDate transcriptDate);
}
