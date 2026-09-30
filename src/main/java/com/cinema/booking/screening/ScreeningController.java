package com.cinema.booking.screening;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Screenings", description = "Schedule movies in rooms")
public class ScreeningController {

	private final ScreeningService screeningService;

	public ScreeningController(ScreeningService screeningService) {
		this.screeningService = screeningService;
	}

	@PostMapping("/api/screenings")
	@ResponseStatus(HttpStatus.CREATED)
	public ScreeningResponse createScreening(@Valid @RequestBody ScreeningRequest request) {
		return screeningService.createScreening(request);
	}

	@GetMapping("/api/screenings")
	public List<ScreeningResponse> getAllScreenings() {
		return screeningService.getAllScreenings();
	}

	@GetMapping("/api/screenings/{id}")
	public ScreeningResponse getScreeningById(@PathVariable Long id) {
		return screeningService.getScreeningById(id);
	}

	@GetMapping("/api/movies/{movieId}/screenings")
	public List<ScreeningResponse> getScreeningsByMovieId(@PathVariable Long movieId) {
		return screeningService.getScreeningsByMovieId(movieId);
	}

	@GetMapping("/api/rooms/{roomId}/screenings")
	public List<ScreeningResponse> getScreeningsByRoomId(@PathVariable Long roomId) {
		return screeningService.getScreeningsByRoomId(roomId);
	}
}
