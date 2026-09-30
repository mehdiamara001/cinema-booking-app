package com.cinema.booking.booking;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@Tag(name = "Bookings", description = "Reserve seats for future screenings")
public class BookingController {

	private final BookingService bookingService;

	public BookingController(BookingService bookingService) {
		this.bookingService = bookingService;
	}

	@PostMapping("/api/bookings")
	@Operation(summary = "Create a booking", security = @SecurityRequirement(name = "basicAuth"))
	@ResponseStatus(HttpStatus.CREATED)
	public BookingResponse createBooking(@Valid @RequestBody BookingRequest request, Authentication authentication) {
		return bookingService.createBooking(request, authentication.getName());
	}

	@GetMapping("/api/bookings")
	public List<BookingResponse> getAllBookings() {
		return bookingService.getAllBookings();
	}

	@GetMapping("/api/bookings/{id}")
	public BookingResponse getBookingById(@PathVariable Long id) {
		return bookingService.getBookingById(id);
	}

	@GetMapping("/api/screenings/{screeningId}/bookings")
	public List<BookingResponse> getBookingsByScreeningId(@PathVariable Long screeningId) {
		return bookingService.getBookingsByScreeningId(screeningId);
	}
}
