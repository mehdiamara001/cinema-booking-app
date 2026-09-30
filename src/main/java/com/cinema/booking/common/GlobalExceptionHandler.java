package com.cinema.booking.common;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ApiError> handleResponseStatus(
			ResponseStatusException exception, WebRequest request) {
		HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
		return response(status, exception.getReason() == null ? status.getReasonPhrase() : exception.getReason(),
				path(request), Map.of());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception, WebRequest request) {
		Map<String, String> fieldErrors = new TreeMap<>();
		exception.getBindingResult().getFieldErrors().forEach(error ->
				fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage()));
		return response(HttpStatus.BAD_REQUEST, "Request validation failed", path(request), fieldErrors);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiError> handleUnreadableBody(HttpMessageNotReadableException exception, WebRequest request) {
		return response(HttpStatus.BAD_REQUEST, "Request body is missing or malformed", path(request), Map.of());
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiError> handleConflict(DataIntegrityViolationException exception, WebRequest request) {
		String detail = exception.getMostSpecificCause().getMessage();
		String normalized = detail == null ? "" : detail.toLowerCase();
		String message = normalized.contains("uk_user_email") || normalized.contains("users_email")
				? "Email is already registered"
				: normalized.contains("uk_booking_screening_seat") || normalized.contains("bookings_screening_id_seat_id")
						? "This seat is already booked for this screening"
						: "A database constraint was violated";
		return response(HttpStatus.CONFLICT, message, path(request), Map.of());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> handleUnexpected(Exception exception, WebRequest request) {
		logger.error("Unhandled request failure at {}", path(request), exception);
		return response(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected server error occurred", path(request), Map.of());
	}

	private ResponseEntity<ApiError> response(HttpStatus status, String message, String path,
			Map<String, String> fieldErrors) {
		return ResponseEntity.status(status).body(new ApiError(
				Instant.now(), status.value(), status.getReasonPhrase(), message, path, fieldErrors));
	}

	private String path(WebRequest request) {
		return request instanceof ServletWebRequest servletRequest
				? servletRequest.getRequest().getRequestURI() : "";
	}
}
