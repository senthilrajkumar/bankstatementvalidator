package com.nl.rabobank.bankstatementvalidator.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolation;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
@RestController
@Slf4j
public class ControllerAdviser extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = TransactionDataInputException.class)
    public ResponseEntity<Object> handleDataInputException(Exception ex) {
        log.error("handleDataInputException {}", ex.getMessage());
        return createResponseEntity(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = BankStatementDBException.class)
    public ResponseEntity<Object> handleDBException(Exception ex) {
        log.error("handleException {}", ex.getMessage());
        return createResponseEntity(ex, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.error("handleMethodArgumentNotValid---->");
        Map<String, Object> body = new LinkedHashMap<>();
        List<String> errors = ex.getBindingResult().getFieldErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());
        body.put("result", HttpStatus.BAD_REQUEST);
        body.put("errorRecords", errors);
        return createResponseEntity(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(javax.validation.ConstraintViolationException.class)
    protected ResponseEntity<Object> handleConstraintViolation(javax.validation.ConstraintViolationException ex) {
        log.error("handleConstraintViolation---->");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("result", HttpStatus.BAD_REQUEST);
        List<String> details = ex.getConstraintViolations().parallelStream().map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());
        body.put("errorRecords", details);
        return createResponseEntity(body, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.error("handleHttpMessageNotReadable---->");
        return createResponseEntity(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Object> handleException(Exception ex) {
        log.error("handleException {}", ex.getMessage());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("errorRecords", ex.getMessage());
        body.put("result", HttpStatus.INTERNAL_SERVER_ERROR);
        return createResponseEntity(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<Object> createResponseEntity(Exception ex, HttpStatus badRequest) {
        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("result", badRequest);
        responseBody.put("errorRecords", ex.getMessage());
        return createResponseEntity(responseBody, badRequest);
    }

    private ResponseEntity<Object> createResponseEntity(Map<String, Object> body, HttpStatus badRequest) {
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(body, header, badRequest);
    }

}
