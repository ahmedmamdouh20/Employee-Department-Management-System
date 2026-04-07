package com.demo.ems.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(DepartmentNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleDepartmentNotFoundException(DepartmentNotFoundException ex) {
		HttpStatus status = HttpStatus.NOT_FOUND;
		return ResponseEntity.status(status)
				.body(new ApiErrorResponse(status.value(), ex.getMessage()));
	}

	@ExceptionHandler(EmployeeNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleEmployeeNotFoundException(EmployeeNotFoundException ex) {
		HttpStatus status = HttpStatus.NOT_FOUND;
		return ResponseEntity.status(status)
				.body(new ApiErrorResponse(status.value(), ex.getMessage()));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
		HttpStatus status = HttpStatus.BAD_REQUEST;
		return ResponseEntity.status(status)
				.body(new ApiErrorResponse(status.value(), ex.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
		HttpStatus status = HttpStatus.BAD_REQUEST;
		String message = ex.getBindingResult()
				.getFieldErrors()
				.stream()
				.findFirst()
				.map(fieldError -> fieldError.getDefaultMessage())
				.orElse("Request validation failed");
		return ResponseEntity.status(status)
				.body(new ApiErrorResponse(status.value(), message));
	}
}
