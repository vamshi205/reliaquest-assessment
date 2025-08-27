package com.reliaquest.api.exception;

import com.reliaquest.api.exception.ErrorResponse;
import java.time.OffsetDateTime;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import jakarta.validation.ConstraintViolationException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        logger.info("Resource not found: path={} message={}", req.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        ex.getMessage(),
                        req.getRequestURI(),
                        OffsetDateTime.now(),
                        HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandler(NoHandlerFoundException ex, HttpServletRequest req) {
        logger.info("No handler found: path={}", req.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        "Not Found",
                        req.getRequestURI(),
                        OffsetDateTime.now(),
                        HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException ex, HttpServletRequest req) {
        logger.info("No resource found: path={}", req.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        "Not Found",
                        req.getRequestURI(),
                        OffsetDateTime.now(),
                        HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        logger.info("Validation failed: path={} errors={}", req.getRequestURI(), ex.getBindingResult().getErrorCount());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        "Invalid request",
                        req.getRequestURI(),
                        OffsetDateTime.now(),
                        HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        logger.info("Constraint violation: path={} message={}", req.getRequestURI(), ex.getMessage());
        String msg = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .findFirst()
                .orElse("Invalid request");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        msg,
                        req.getRequestURI(),
                        OffsetDateTime.now(),
                        HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        logger.info("Bad request: path={} message={}", req.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        "Employee not found",
                        req.getRequestURI(),
                        OffsetDateTime.now(),
                        HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(HttpClientErrorException.TooManyRequests.class)
    public ResponseEntity<ErrorResponse> handleTooManyRequests(HttpClientErrorException.TooManyRequests ex, HttpServletRequest req) {
        logger.warn("Rate limited: path={}", req.getRequestURI());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new ErrorResponse(
                        "Rate limit exceeded. Please try later.",
                        req.getRequestURI(),
                        OffsetDateTime.now(),
                        HttpStatus.TOO_MANY_REQUESTS.value()));
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ErrorResponse> handleClientError(HttpClientErrorException ex, HttpServletRequest req) {
        logger.warn("Upstream client error: path={} status={}", req.getRequestURI(), ex.getStatusCode().value());
        return ResponseEntity.status(ex.getStatusCode())
                .body(new ErrorResponse(
                        "Upstream client error: " + ex.getStatusCode().value(),
                        req.getRequestURI(),
                        OffsetDateTime.now(),
                        ex.getStatusCode().value()));
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<ErrorResponse> handleServerError(HttpServerErrorException ex, HttpServletRequest req) {
        logger.error("Upstream server error: path={} status={}", req.getRequestURI(), ex.getStatusCode().value());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorResponse(
                        "Upstream server error",
                        req.getRequestURI(),
                        OffsetDateTime.now(),
                        HttpStatus.BAD_GATEWAY.value()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex, HttpServletRequest req) {
        logger.error("Unhandled error: path={} message={}", req.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred: " + ex.getMessage());
    }
}


