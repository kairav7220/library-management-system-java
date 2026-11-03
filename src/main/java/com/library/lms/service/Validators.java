package com.library.lms.service;

import com.library.lms.exception.ValidationException;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public final class Validators {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private Validators() {
    }

    public static void requireNonBlank(String field, String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(field + " is required.");
        }
    }

    public static void requireEmail(String field, String value) {
        if (value != null && !value.isBlank() && !EMAIL.matcher(value.trim()).matches()) {
            throw new ValidationException(field + " must be a valid email address.");
        }
    }

    public static void requireNonNegative(String field, BigDecimal value) {
        if (value != null && value.signum() < 0) {
            throw new ValidationException(field + " cannot be negative.");
        }
    }
}
