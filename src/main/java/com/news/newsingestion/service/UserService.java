package com.news.newsingestion.service;

import com.news.newsingestion.dto.LoginRequest;
import com.news.newsingestion.dto.LoginResponse;
import com.news.newsingestion.dto.SignUpRequest;
import com.news.newsingestion.model.Topic;
import com.news.newsingestion.model.User;
import com.news.newsingestion.model.UserTopicPreference;
import com.news.newsingestion.repository.TopicRepository;
import com.news.newsingestion.repository.UserRepository;
import com.news.newsingestion.repository.UserTopicPreferenceRepository;
import com.news.newsingestion.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final TopicRepository topicRepository;
    private final UserTopicPreferenceRepository userTopicPreferenceRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public String signUp(SignUpRequest request) {
        log.info("Signing up user: {}", request.getEmail());
        
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("User with email " + request.getEmail() + " already exists");
        }

        User user = User.builder()
                .email(request.getEmail())
                .name(request.getName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .preferredLanguage("en")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        user = userRepository.save(user);

        if (request.getTopics() != null) {
            List<Topic> dbTopics = topicRepository.findByNameIn(request.getTopics());
            List<UserTopicPreference> userTopicPreferenceList = new ArrayList<>();
            for(Topic topic : dbTopics) {
                UserTopicPreference preference = UserTopicPreference.builder()
                        .user(user)
                        .topic(topic)
                        .priority(1)
                        .build();
                userTopicPreferenceList.add(preference);
            }
            if(userTopicPreferenceList.size() > 0) {
                userTopicPreferenceRepository.saveAll(userTopicPreferenceList);
            }
        }

        return user.getId().toString();
    }

    public LoginResponse login(LoginRequest request) {
        log.info("Logging in user: {}", request.getEmail());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        String jwtToken = jwtService.generateToken(user);
        return LoginResponse.builder()
                .token(jwtToken)
                .build();
    }
}
