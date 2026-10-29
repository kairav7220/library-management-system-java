package com.library.lms.repository;

import com.library.lms.entity.BookGenre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookGenreRepository extends JpaRepository<BookGenre, Integer> {

    Optional<BookGenre> findByGenreId(String genreId);

    List<BookGenre> findByStatus(Integer status);
}
