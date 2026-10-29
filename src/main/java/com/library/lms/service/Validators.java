package com.library.lms.service;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public final class Validators {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private Validators() {
    }

    public static void requireNonBlank(String field, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required.");
        }
    }

    public static void requireEmail(String field, String value) {
        if (value != null && !value.isBlank() && !EMAIL.matcher(value.trim()).matches()) {
            throw new IllegalArgumentException(field + " must be a valid email address.");
        }
    }

    public static void requireNonNegative(String field, BigDecimal value) {
        if (value != null && value.signum() < 0) {
            throw new IllegalArgumentException(field + " cannot be negative.");
        }
    }
}
