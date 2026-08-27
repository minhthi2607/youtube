package com.project.youtube.repository;

import com.project.youtube.entity.Subscription;
import com.project.youtube.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findBySubscriberAndChannel(User subscriber, User channel);

    boolean existsBySubscriberAndChannel(User subscriber, User channel);

    long countByChannel(User channel);

    Page<Subscription> findBySubscriber(User subscriber, Pageable pageable);

    void deleteBySubscriberAndChannel(User subscriber, User channel);
}
