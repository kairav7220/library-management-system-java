package com.library.lms.repository;

import com.library.lms.entity.BookCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookCategoryRepository extends JpaRepository<BookCategory, Integer> {

    Optional<BookCategory> findByCatId(String catId);

    List<BookCategory> findByStatus(Integer status);
}
