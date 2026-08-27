package com.project.youtube.service;

import com.project.youtube.dto.user.CurrentUserResponse;
import com.project.youtube.dto.user.UserProfileResponse;
import com.project.youtube.entity.User;
import com.project.youtube.exception.ResourceNotFoundException;
import com.project.youtube.repository.SubscriptionRepository;
import com.project.youtube.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;

    public User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    public UserProfileResponse getProfile(Long id) {
        User user = getUserOrThrow(id);
        long subscriberCount = subscriptionRepository.countByChannel(user);
        return new UserProfileResponse(user.getId(), user.getUsername(), user.getDisplayName(),
                user.getAvatarUrl(), subscriberCount, user.getCreatedAt());
    }

    public CurrentUserResponse getCurrentUser(Long id) {
        User user = getUserOrThrow(id);
        return new CurrentUserResponse(user.getId(), user.getUsername(), user.getEmail(),
                user.getDisplayName(), user.getAvatarUrl(), user.getCreatedAt());
    }
}
