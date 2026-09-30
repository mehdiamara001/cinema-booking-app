package com.cinema.booking.booking;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Booking details including the user authenticated when it was created.")
public record BookingResponse(Long bookingId, Long screeningId, Long seatId, Long userId, LocalDateTime bookedAt) {
}
