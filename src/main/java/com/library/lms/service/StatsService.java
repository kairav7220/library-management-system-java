package com.library.lms.service;

import com.library.lms.repository.BookIssueRepository;
import com.library.lms.repository.BookRepository;
import com.library.lms.repository.MemberRepository;
import com.library.lms.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class StatsService {

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final BookIssueRepository bookIssueRepository;
    private final PaymentRepository paymentRepository;
    private final FineCalculator fineCalculator;

    public StatsService(BookRepository bookRepository, MemberRepository memberRepository,
                        BookIssueRepository bookIssueRepository, PaymentRepository paymentRepository,
                        FineCalculator fineCalculator) {
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
        this.bookIssueRepository = bookIssueRepository;
        this.paymentRepository = paymentRepository;
        this.fineCalculator = fineCalculator;
    }

    public long totalBooks() {
        return bookRepository.findByStatus(0).size();
    }

    public long totalMembers() {
        return memberRepository.findByStatus(0).size();
    }

    public long issuedCount() {
        return bookIssueRepository.findByReturnedDateIsNull().size();
    }

    public long overdueCount() {
        return bookIssueRepository.findByReturnedDateIsNull().stream()
                .filter(issue -> fineCalculator.isOverdue(issue.getIssuedDate()))
                .count();
    }

    public BigDecimal revenue() {
        return paymentRepository.findAll().stream()
                .map(p -> p.getPaymentAmount() == null ? BigDecimal.ZERO : p.getPaymentAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
