package com.library.lms.repository;

import com.library.lms.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    Optional<Payment> findByTransactionId(String transactionId);

    List<Payment> findByPaidBy(String paidBy);
}
