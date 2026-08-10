package com.example.url_shortener.exception;

import jakarta.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(UrlNotFoundException.class)
        @ResponseStatus(HttpStatus.NOT_FOUND)
        public ApiError handleUrlNotFound(
                        UrlNotFoundException exception) {

                return new ApiError(
                                "URL_NOT_FOUND",
                                exception.getMessage(),
                                Instant.now(),
                                Map.of());
        }

        @ExceptionHandler(UrlExpiredException.class)
        @ResponseStatus(HttpStatus.GONE)
        public ApiError handleUrlExpired(
                        UrlExpiredException exception) {

                return new ApiError(
                                "URL_EXPIRED",
                                exception.getMessage(),
                                Instant.now(),
                                Map.of());
        }

        @ExceptionHandler(UrlDeactivatedException.class)
        @ResponseStatus(HttpStatus.GONE)
        public ApiError handleUrlDeactivated(
                        UrlDeactivatedException exception) {

                return new ApiError(
                                "URL_DEACTIVATED",
                                exception.getMessage(),
                                Instant.now(),
                                Map.of());
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public ApiError handleValidation(
                        MethodArgumentNotValidException exception) {

                Map<String, String> fieldErrors = new LinkedHashMap<>();

                exception.getBindingResult()
                                .getFieldErrors()
                                .forEach(error -> fieldErrors.put(
                                                error.getField(),
                                                error.getDefaultMessage()));

                return new ApiError(
                                "VALIDATION_ERROR",
                                "Request validation failed",
                                Instant.now(),
                                fieldErrors);
        }

        @ExceptionHandler(ConstraintViolationException.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public ApiError handleConstraintViolation(
                        ConstraintViolationException exception) {

                return new ApiError(
                                "VALIDATION_ERROR",
                                exception.getMessage(),
                                Instant.now(),
                                Map.of());
        }

        @ExceptionHandler(Exception.class)
        @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        public ApiError handleUnexpectedException(Exception exception) {

                exception.printStackTrace();

                return new ApiError(
                                "INTERNAL_SERVER_ERROR",
                                exception.getMessage(),
                                Instant.now(),
                                Map.of());
        }
}