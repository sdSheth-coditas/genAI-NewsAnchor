package com.news.newsingestion.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "news_transcripts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsTranscript {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "topic_id")
    private String topic;

    @Column(name = "transcript_date")
    private LocalDate transcriptDate;

    private String language;

    @Column(name = "word_count")
    private Integer wordCount;

    @Column(name = "transcript_text", columnDefinition = "TEXT")
    private String transcriptText;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
