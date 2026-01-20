package com.news.newsingestion.repository;

import com.news.newsingestion.model.UserTopicPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserTopicPreferenceRepository extends JpaRepository<UserTopicPreference, UUID> {
}
