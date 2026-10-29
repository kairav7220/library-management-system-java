package com.library.lms.repository;

import com.library.lms.entity.BookSell;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookSellRepository extends JpaRepository<BookSell, Integer> {

    Optional<BookSell> findByOrderId(String orderId);

    List<BookSell> findByMemId(String memId);
}
