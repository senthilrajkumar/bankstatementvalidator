package com.nl.rabobank.bankstatementvalidator.exception;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@ControllerAdvice
@RestController
public class ControllerAdviser extends ResponseEntityExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(ControllerAdviser.class);

	@ExceptionHandler(value = TransactionDataInputException.class)
	public ResponseEntity<Object> handleDataInputException(Exception ex) {
		log.error("handleDataInputException {}", ex.getMessage());
		Map<String, Object> responseBody = new LinkedHashMap<>();
		responseBody.put("result", HttpStatus.BAD_REQUEST);
		responseBody.put("errorRecords", ex.getMessage());
		HttpHeaders httpHeader = new HttpHeaders();
		httpHeader.setContentType(MediaType.APPLICATION_JSON);
		return new ResponseEntity<>(responseBody, httpHeader, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(value = BankStatementDBException.class)
	public ResponseEntity<Object> handleDBException(Exception ex) {
		log.error("handleException {}", ex.getMessage());
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("result", HttpStatus.INTERNAL_SERVER_ERROR);
		body.put("errorRecords", ex.getMessage());
		HttpHeaders header = new HttpHeaders();
		header.setContentType(MediaType.APPLICATION_JSON);
		return new ResponseEntity<>(body, header, HttpStatus.INTERNAL_SERVER_ERROR);
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
		HttpHeaders header = new HttpHeaders();
		header.setContentType(MediaType.APPLICATION_JSON);
		return new ResponseEntity<>(body, header, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(javax.validation.ConstraintViolationException.class)
	protected ResponseEntity<Object> handleConstraintViolation(javax.validation.ConstraintViolationException ex) {
		log.error("handleConstraintViolation---->");
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("result", HttpStatus.BAD_REQUEST);
		List<String> details = ex.getConstraintViolations().parallelStream().map(ConstraintViolation::getMessage)
				.collect(Collectors.toList());
		body.put("errorRecords", details);
		HttpHeaders header = new HttpHeaders();
		header.setContentType(MediaType.APPLICATION_JSON);
		return new ResponseEntity<>(body, header, HttpStatus.BAD_REQUEST);
	}

	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		log.error("handleHttpMessageNotReadable---->");
		Map<String, Object> response = new LinkedHashMap<>();
		response.put("result", HttpStatus.BAD_REQUEST);
		response.put("errorRecords", ex.getMessage());
		HttpHeaders header = new HttpHeaders();
		header.setContentType(MediaType.APPLICATION_JSON);
		return new ResponseEntity<>(response, header, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(value = Exception.class)
	public ResponseEntity<Object> handleException(Exception ex) {
		log.error("handleException {}", ex.getMessage());
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("errorRecords", ex.getMessage());
		body.put("result", HttpStatus.INTERNAL_SERVER_ERROR);
		HttpHeaders header = new HttpHeaders();
		header.setContentType(MediaType.APPLICATION_JSON);
		return new ResponseEntity<>(body, header, HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
