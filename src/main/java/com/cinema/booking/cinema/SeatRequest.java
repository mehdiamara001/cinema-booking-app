package com.cinema.booking.cinema;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Row and seat position within a room.")
public record SeatRequest(
		@NotNull @Positive Integer rowNumber,
		@NotNull @Positive Integer seatNumber
) {
}
