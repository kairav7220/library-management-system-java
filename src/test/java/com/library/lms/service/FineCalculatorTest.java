package com.library.lms.service;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FineCalculatorTest {

    private FineCalculator calculatorWith(int finePerDay) {
        FineCalculator calculator = new FineCalculator();
        ReflectionTestUtils.setField(calculator, "finePerDay", finePerDay);
        return calculator;
    }

    @Test
    void noFineInsideLoanPeriod() {
        FineCalculator calculator = calculatorWith(10);
        assertEquals(0, calculator.fineFor("2026-01-01", "2026-01-15"));
    }

    @Test
    void chargesPerDayAfterLoanPeriod() {
        FineCalculator calculator = calculatorWith(10);
        assertEquals(170, calculator.fineFor("2026-01-01", "2026-02-01"));
    }

    @Test
    void respectsConfiguredRate() {
        FineCalculator calculator = calculatorWith(5);
        assertEquals(85, calculator.fineFor("2026-01-01", "2026-02-01"));
    }

    @Test
    void parsesLegacyDateFormat() {
        FineCalculator calculator = calculatorWith(10);
        assertEquals(170, calculator.fineFor("1-Jan-2026", "1-Feb-2026"));
    }

    @Test
    void returnsZeroForUnknownDates() {
        FineCalculator calculator = calculatorWith(10);
        assertEquals(0, calculator.fineFor("not-a-date", "2026-02-01"));
        assertEquals(0, calculator.fineFor(null, "2026-02-01"));
        assertEquals(0, calculator.fineFor("", "2026-02-01"));
    }

    @Test
    void overdueFlagWithinAndBeyondLoan() {
        FineCalculator calculator = calculatorWith(10);
        assertTrue(calculator.isOverdue(java.time.LocalDate.now().minusDays(20).toString()));
        assertFalse(calculator.isOverdue(java.time.LocalDate.now().minusDays(5).toString()));
        assertFalse(calculator.isOverdue("garbage"));
        assertFalse(calculator.isOverdue(null));
    }
}
