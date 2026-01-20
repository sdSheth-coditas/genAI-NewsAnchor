package com.news.newsingestion.repository;

import com.news.newsingestion.model.NewsTranscript;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NewsTranscriptRepository extends JpaRepository<NewsTranscript, UUID> {
}
