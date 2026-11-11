package com.ledgerguard.ledger.common;

import com.ledgerguard.ledger.idempotency.exception.IdempotencyKeyMismatchException;
import com.ledgerguard.ledger.idempotency.exception.RequestInProgressException;
import com.ledgerguard.ledger.ledger.exception.AccountNotFoundException;
import com.ledgerguard.ledger.payment.exception.IllegalTransitionException;
import com.ledgerguard.ledger.payment.exception.PaymentNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import tools.jackson.core.JacksonException;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404
    @ExceptionHandler({PaymentNotFoundException.class, AccountNotFoundException.class})
    public ResponseEntity<ErrorResponse> notFound(RuntimeException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    // 409
    @ExceptionHandler({IllegalTransitionException.class, RequestInProgressException.class})
    public ResponseEntity<ErrorResponse> conflict(RuntimeException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> optimisticLock(HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "The payment was modified concurrently; retry the request", request);
    }

    // 422
    @ExceptionHandler(IdempotencyKeyMismatchException.class)
    public ResponseEntity<ErrorResponse> keyMismatch(IdempotencyKeyMismatchException ex, HttpServletRequest request) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request);
    }

    // 400
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> validation(ConstraintViolationException ex, HttpServletRequest request) {
        String message = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .sorted()
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler({JacksonException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ErrorResponse> unreadableBody(HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Request body is missing or is not valid JSON", request);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> missingHeader(MissingRequestHeaderException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Missing required header: " + ex.getHeaderName(), request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> badArgument(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Invalid value for '" + ex.getName() + "'", request);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                Instant.now(), status.value(), status.getReasonPhrase(), message, request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
