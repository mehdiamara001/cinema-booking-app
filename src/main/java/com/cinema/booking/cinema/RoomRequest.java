package com.cinema.booking.cinema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Name and seating capacity for a cinema room.")
public record RoomRequest(
		@NotBlank @Size(max = 100) String name,
		@NotNull @Positive Integer capacity
) {
}
