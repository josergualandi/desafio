package br.com.desafio.app.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<ApiError> handleNotFound(NotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ApiError(Instant.now().toString(), HttpStatus.NOT_FOUND.value(), ex.getMessage(), null));
	}

	@ExceptionHandler(RegraNegocioException.class)
	public ResponseEntity<ApiError> handleBusiness(RegraNegocioException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new ApiError(Instant.now().toString(), HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
		Map<String, String> fields = ex.getBindingResult()
				.getFieldErrors()
				.stream()
				.collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a));

		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new ApiError(Instant.now().toString(), HttpStatus.BAD_REQUEST.value(), "Erro de validação", fields));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new ApiError(Instant.now().toString(), HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> handleGeneric(Exception ex) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ApiError(Instant.now().toString(), HttpStatus.INTERNAL_SERVER_ERROR.value(), "Erro interno do servidor", null));
	}

	public record ApiError(String timestamp, int status, String message, Map<String, String> fieldErrors) {
	}
}
