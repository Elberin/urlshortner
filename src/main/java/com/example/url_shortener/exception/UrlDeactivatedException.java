package com.example.url_shortener.exception;

public class UrlDeactivatedException extends RuntimeException {

    public UrlDeactivatedException(String message) {
        super(message);
    }
}