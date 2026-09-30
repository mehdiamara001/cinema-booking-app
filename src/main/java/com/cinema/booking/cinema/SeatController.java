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
@Tag(name = "Seats", description = "Manage room seats")
public class SeatController {

	private final RoomRepository roomRepository;
	private final SeatRepository seatRepository;

	public SeatController(RoomRepository roomRepository, SeatRepository seatRepository) {
		this.roomRepository = roomRepository;
		this.seatRepository = seatRepository;
	}

	@PostMapping("/api/rooms/{roomId}/seats")
	@ResponseStatus(HttpStatus.CREATED)
	public Seat createSeat(@PathVariable Long roomId, @Valid @RequestBody SeatRequest request) {
		Room room = roomRepository.findById(roomId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));

		boolean alreadyExists = seatRepository.existsByRoom_IdAndRowNumberAndSeatNumber(
				roomId, request.rowNumber(), request.seatNumber());
		if (alreadyExists) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Seat already exists in this room");
		}

		Seat seat = new Seat(request.rowNumber(), request.seatNumber(), room);
		return seatRepository.save(seat);
	}

	@GetMapping("/api/rooms/{roomId}/seats")
	public List<Seat> getSeatsForRoom(@PathVariable Long roomId) {
		if (!roomRepository.existsById(roomId)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found");
		}

		return seatRepository.findByRoom_IdOrderByRowNumberAscSeatNumberAsc(roomId);
	}

	@GetMapping("/api/seats/{seatId}")
	public Seat getSeatById(@PathVariable Long seatId) {
		return seatRepository.findById(seatId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seat not found"));
	}
}
