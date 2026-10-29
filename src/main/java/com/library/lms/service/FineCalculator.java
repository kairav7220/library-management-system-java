package com.library.lms.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

@Service
public class FineCalculator {

    public static final int LOAN_DAYS = 14;

    private static final DateTimeFormatter LEGACY = DateTimeFormatter.ofPattern("d-MMM-yyyy", Locale.ENGLISH);

    @Value("${lumina.fine-per-day:10}")
    private int finePerDay;

    public int getFinePerDay() {
        return finePerDay;
    }

    public int fineFor(String issuedDate, String endDate) {
        LocalDate issued = parse(issuedDate);
        LocalDate end = endDate == null || endDate.isBlank() ? LocalDate.now() : parse(endDate);
        if (issued == null || end == null) {
            return 0;
        }
        long daysLate = ChronoUnit.DAYS.between(issued, end) - LOAN_DAYS;
        return daysLate > 0 ? (int) daysLate * finePerDay : 0;
    }

    public boolean isOverdue(String issuedDate) {
        LocalDate issued = parse(issuedDate);
        if (issued == null) {
            return false;
        }
        return ChronoUnit.DAYS.between(issued, LocalDate.now()) > LOAN_DAYS;
    }

    private LocalDate parse(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (RuntimeException ignored) {
            try {
                return LocalDate.parse(value, LEGACY);
            } catch (RuntimeException ignoredAgain) {
                return null;
            }
        }
    }
}
