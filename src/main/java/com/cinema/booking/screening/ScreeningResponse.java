package com.cinema.booking.screening;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A scheduled movie screening in a room.")
public record ScreeningResponse(Long id, Long movieId, Long roomId, LocalDateTime startTime) {
}
