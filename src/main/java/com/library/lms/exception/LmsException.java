package com.library.lms.exception;

public abstract class LmsException extends RuntimeException {

    protected LmsException(String message) {
        super(message);
    }
}
