package com.library.lms.repository;

import com.library.lms.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Integer> {

    Optional<Book> findByBookId(String bookId);

    List<Book> findByStatus(Integer status);
}
