package com.epam.library.exceptions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @ExceptionHandler(InvalidDataException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDataException(InvalidDataException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                new Date().toString(),
                HttpStatus.BAD_REQUEST.name(),
                ex.getMessage(),
                request.getDescription(false));

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                new Date().toString(),
                HttpStatus.NOT_FOUND.name(),
                ex.getMessage(),
                request.getDescription(false));

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeignException(FeignException exception, WebRequest request) throws JsonProcessingException {
        HttpStatus status = HttpStatus.resolve(exception.status());
        String errorMessage = "An error occurred in the Feign client";

        // Let fallback handle 5xx and connection issues
        if (status != null && status.is5xxServerError()) {
            throw exception; // Don't handle, let fallback take over
        }

        // Continue parsing error response for 4xx
        if (exception.responseBody().isPresent()) {
            String responseBody = new String(exception.responseBody().get().array());
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            if (jsonNode.has("message")) {
                errorMessage = jsonNode.get("message").asText();
            } else if (jsonNode.has("error")) {
                errorMessage = jsonNode.get("error").asText();
            }
        }

        ErrorResponse errorResponse = new ErrorResponse(
                new Date().toString(),
                status != null ? status.name() : HttpStatus.BAD_GATEWAY.name(),
                errorMessage,
                request.getDescription(false)
        );

        return new ResponseEntity<>(errorResponse, status != null ? status : HttpStatus.BAD_GATEWAY);
    }
/*  @ExceptionHandler(FeignException.class)
public ResponseEntity<ErrorResponse> handleFeignException(FeignException ex, WebRequest request) throws JsonProcessingException {
    HttpStatus status = HttpStatus.resolve(ex.status());

    // Let fallback handle 5xx errors (don't catch)
    if (status != null && status.is5xxServerError()) {
        throw ex;
    }

    // Default message
    String message = "Feign client error";

    // Try to extract message from response body
    if (ex.responseBody().isPresent()) {
        String body = new String(ex.responseBody().get().array());
        JsonNode json = objectMapper.readTree(body);
        message = json.has("message") ? json.get("message").asText()
                 : json.has("error") ? json.get("error").asText()
                 : message;
    }

    ErrorResponse errorResponse = new ErrorResponse(
            new Date().toString(),
            status != null ? status.name() : "BAD_GATEWAY",
            message,
            request.getDescription(false)
    );

    return new ResponseEntity<>(errorResponse, status != null ? status : HttpStatus.BAD_GATEWAY);
}

 */

    /* @RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<String> handleFeignStatusException(FeignException ex) {
        if (ex.status() == 404) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Service not found: " + ex.getMessage());
        } else if (ex.status() == 400) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bad request: " + ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal error: " + ex.getMessage());
    }
}
*/


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                new Date().toString(),
                HttpStatus.INTERNAL_SERVER_ERROR.name(),
                ex.getMessage(),
                request.getDescription(false));

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}