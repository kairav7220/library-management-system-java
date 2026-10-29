package com.library.lms.repository;

import com.library.lms.entity.BookIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookIssueRepository extends JpaRepository<BookIssue, Integer> {

    Optional<BookIssue> findByTransactionId(String transactionId);

    @Query("SELECT i FROM BookIssue i WHERE i.bookId = :bookId AND i.returnedDate IS NULL")
    Optional<BookIssue> findOpenIssueByBook(@Param("bookId") String bookId);

    @Query("SELECT i FROM BookIssue i WHERE i.issuedTo = :memId AND i.returnedDate IS NULL")
    List<BookIssue> findOpenIssuesByMember(@Param("memId") String memId);

    List<BookIssue> findByIssuedTo(String memId);

    List<BookIssue> findByReturnedDateIsNull();
}
