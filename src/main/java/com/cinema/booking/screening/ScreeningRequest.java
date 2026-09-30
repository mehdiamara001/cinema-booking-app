package com.cinema.booking.screening;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Movie, room, and start time for a screening.")
public record ScreeningRequest(
		@NotNull @Positive Long movieId,
		@NotNull @Positive Long roomId,
		@NotNull @Future LocalDateTime startTime
) {
}
