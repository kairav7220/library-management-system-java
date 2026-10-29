package com.library.lms.web;

public record SelectOption(String id, String label, String name, String price) {

    public SelectOption(String id, String label) {
        this(id, label, null, null);
    }
}
