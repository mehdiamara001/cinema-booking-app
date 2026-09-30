package com.cinema.booking.cinema;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/cinemas")
@Tag(name = "Cinemas", description = "Manage cinemas")
public class CinemaController {

	private final CinemaRepository cinemaRepository;

	public CinemaController(CinemaRepository cinemaRepository) {
		this.cinemaRepository = cinemaRepository;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Cinema createCinema(@Valid @RequestBody Cinema cinema) {
		return cinemaRepository.save(cinema);
	}

	@GetMapping
	public List<Cinema> getAllCinemas() {
		return cinemaRepository.findAll();
	}

	@GetMapping("/{id}")
	public Cinema getCinemaById(@PathVariable Long id) {
		return cinemaRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cinema not found"));
	}
}
