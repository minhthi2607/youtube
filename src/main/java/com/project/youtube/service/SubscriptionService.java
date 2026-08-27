package com.project.youtube.service;

import com.project.youtube.dto.subscription.SubscriptionStatusResponse;
import com.project.youtube.entity.Subscription;
import com.project.youtube.entity.User;
import com.project.youtube.exception.BadRequestException;
import com.project.youtube.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    @Transactional
    public SubscriptionStatusResponse subscribe(User subscriber, User channel) {
        if (subscriber.getId().equals(channel.getId())) {
            throw new BadRequestException("You cannot subscribe to your own channel");
        }
        if (!subscriptionRepository.existsBySubscriberAndChannel(subscriber, channel)) {
            subscriptionRepository.save(Subscription.builder().subscriber(subscriber).channel(channel).build());
        }
        return status(subscriber, channel);
    }

    @Transactional
    public SubscriptionStatusResponse unsubscribe(User subscriber, User channel) {
        subscriptionRepository.deleteBySubscriberAndChannel(subscriber, channel);
        return status(subscriber, channel);
    }

    @Transactional(readOnly = true)
    public SubscriptionStatusResponse status(User subscriber, User channel) {
        boolean subscribed = subscriber != null && subscriptionRepository.existsBySubscriberAndChannel(subscriber, channel);
        long subscriberCount = subscriptionRepository.countByChannel(channel);
        return new SubscriptionStatusResponse(subscribed, subscriberCount);
    }
}
