package com.news.newsingestion.repository;

import com.news.newsingestion.model.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TopicRepository extends JpaRepository<Topic, UUID> {
    Optional<Topic> findByName(String name);

    List<Topic> findByNameIn(Collection<String> names);

}
