package com.news.newsingestion.service;

import com.news.newsingestion.dto.LoginRequest;
import com.news.newsingestion.dto.SignUpRequest;
import com.news.newsingestion.model.Topic;
import com.news.newsingestion.model.User;
import com.news.newsingestion.model.UserTopicPreference;
import com.news.newsingestion.repository.TopicRepository;
import com.news.newsingestion.repository.UserRepository;
import com.news.newsingestion.repository.UserTopicPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final TopicRepository topicRepository;
    private final UserTopicPreferenceRepository userTopicPreferenceRepository;

    @Transactional
    public User signUp(SignUpRequest request) {
        log.info("Signing up user: {}", request.getEmail());
        
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("User with email " + request.getEmail() + " already exists");
        }

        User user = User.builder()
                .email(request.getEmail())
                .name(request.getName())
                .passwordHash(request.getPassword())
                .preferredLanguage("en")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        user = userRepository.save(user);

        if (request.getTopics() != null) {
            for (String topicName : request.getTopics()) {
                Topic topic = topicRepository.findByName(topicName)
                        .orElseGet(() -> topicRepository.save(Topic.builder().name(topicName).build()));

                UserTopicPreference preference = UserTopicPreference.builder()
                        .user(user)
                        .topic(topic)
                        .priority(1)
                        .build();

                userTopicPreferenceRepository.save(preference);
            }
        }

        return user;
    }

    public User login(LoginRequest request) {
        log.info("Logging in user: {}", request.getEmail());
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getPasswordHash().equals(request.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return user;
    }
}
