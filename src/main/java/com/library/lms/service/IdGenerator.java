package com.library.lms.service;

import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class IdGenerator {

    public <T> String nextId(String prefix, Iterable<T> rows, Function<T, Integer> rowNum) {
        int max = 0;
        for (T row : rows) {
            Integer value = rowNum.apply(row);
            if (value != null && value > max) {
                max = value;
            }
        }
        return prefix + "_" + (max + 1);
    }
}
