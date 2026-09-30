package com.cinema.booking.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
public class JsonSecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			org.springframework.security.core.AuthenticationException exception) throws IOException {
		write(response, request, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", "Authentication is required");
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException exception) throws IOException {
		write(response, request, HttpServletResponse.SC_FORBIDDEN, "Forbidden", "Access is denied");
	}

	private void write(HttpServletResponse response, HttpServletRequest request, int status,
			String error, String message) throws IOException {
		response.setStatus(status);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write("{\"timestamp\":\"" + Instant.now()
				+ "\",\"status\":" + status
				+ ",\"error\":\"" + error
				+ "\",\"message\":\"" + message
				+ "\",\"path\":\"" + escape(request.getRequestURI())
				+ "\",\"fieldErrors\":{}}");
	}

	private String escape(String value) {
		return value.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
