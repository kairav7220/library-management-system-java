package com.library.lms.repository;

import com.library.lms.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Integer> {

    Optional<Subscription> findByTransactionId(String transactionId);

    List<Subscription> findByMemId(String memId);

    List<Subscription> findBySubscriptionStatus(Integer subscriptionStatus);
}
