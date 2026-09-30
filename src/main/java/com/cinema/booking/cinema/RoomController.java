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
@RequestMapping("/api/cinemas/{cinemaId}/rooms")
@Tag(name = "Rooms", description = "Manage cinema rooms")
public class RoomController {

	private final CinemaRepository cinemaRepository;
	private final RoomRepository roomRepository;

	public RoomController(CinemaRepository cinemaRepository, RoomRepository roomRepository) {
		this.cinemaRepository = cinemaRepository;
		this.roomRepository = roomRepository;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Room createRoom(@PathVariable Long cinemaId, @Valid @RequestBody RoomRequest request) {
		Cinema cinema = cinemaRepository.findById(cinemaId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cinema not found"));

		Room room = new Room(request.name(), request.capacity(), cinema);
		return roomRepository.save(room);
	}

	@GetMapping
	public List<Room> getRoomsForCinema(@PathVariable Long cinemaId) {
		if (!cinemaRepository.existsById(cinemaId)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cinema not found");
		}

		return roomRepository.findByCinema_Id(cinemaId);
	}
}
