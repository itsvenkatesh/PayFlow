package org.venky.payflow.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<String> handlePaymentNotFoundException(PaymentNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidPaymentStatusTransitionException.class)
    public ResponseEntity<String> handleInvalidPaymentStatusTransitionException(InvalidPaymentStatusTransitionException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage())
                );

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("message", "Validation failed");
        response.put("errors", errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResourceFoundException(
            NoResourceFoundException ex) {

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.NOT_FOUND.value());
        response.put("message", "Resource not found");

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("message", "Invalid value for parameter: " + ex.getName());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }
}

