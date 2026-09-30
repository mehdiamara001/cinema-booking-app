package com.cinema.booking.screening;

import com.cinema.booking.cinema.Room;
import com.cinema.booking.cinema.RoomRepository;
import com.cinema.booking.movie.Movie;
import com.cinema.booking.movie.MovieRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ScreeningService {

	private final ScreeningRepository screeningRepository;
	private final MovieRepository movieRepository;
	private final RoomRepository roomRepository;

	public ScreeningService(ScreeningRepository screeningRepository,
			MovieRepository movieRepository,
			RoomRepository roomRepository) {
		this.screeningRepository = screeningRepository;
		this.movieRepository = movieRepository;
		this.roomRepository = roomRepository;
	}

	@Transactional
	public ScreeningResponse createScreening(ScreeningRequest request) {
		Movie movie = movieRepository.findById(request.movieId())
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.NOT_FOUND, "Movie with id " + request.movieId() + " not found"));

		Room room = roomRepository.findById(request.roomId())
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.NOT_FOUND, "Room with id " + request.roomId() + " not found"));

		Screening screening = new Screening(movie, room, request.startTime());
		return toResponse(screeningRepository.save(screening));
	}

	public List<ScreeningResponse> getAllScreenings() {
		return screeningRepository.findAll().stream().map(this::toResponse).toList();
	}

	public ScreeningResponse getScreeningById(Long id) {
		Screening screening = screeningRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Screening not found"));
		return toResponse(screening);
	}

	public List<ScreeningResponse> getScreeningsByMovieId(Long movieId) {
		return screeningRepository.findByMovie_IdOrderByStartTimeAsc(movieId)
				.stream().map(this::toResponse).toList();
	}

	public List<ScreeningResponse> getScreeningsByRoomId(Long roomId) {
		return screeningRepository.findByRoom_IdOrderByStartTimeAsc(roomId)
				.stream().map(this::toResponse).toList();
	}

	private ScreeningResponse toResponse(Screening screening) {
		return new ScreeningResponse(
				screening.getId(),
				screening.getMovie().getId(),
				screening.getRoom().getId(),
				screening.getStartTime());
	}
}
