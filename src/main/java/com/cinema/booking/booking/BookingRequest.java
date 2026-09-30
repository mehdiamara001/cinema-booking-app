package com.cinema.booking.booking;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A seat and screening to reserve. The booking owner comes from HTTP Basic authentication.")
public record BookingRequest(
		@NotNull @Positive Long screeningId,
		@NotNull @Positive Long seatId
) {
}
