package com.cinema.booking.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Public user details; password data is deliberately omitted.")
public record UserResponse(Long id, String name, String email) {
}
