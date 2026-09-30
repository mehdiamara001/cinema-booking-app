package com.cinema.booking.booking;

import com.cinema.booking.cinema.Room;
import com.cinema.booking.cinema.Seat;
import com.cinema.booking.cinema.SeatRepository;
import com.cinema.booking.screening.Screening;
import com.cinema.booking.screening.ScreeningRepository;
import com.cinema.booking.user.User;
import com.cinema.booking.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class BookingService {

	private final BookingRepository bookingRepository;
	private final ScreeningRepository screeningRepository;
	private final SeatRepository seatRepository;
	private final UserRepository userRepository;

	public BookingService(BookingRepository bookingRepository,
			ScreeningRepository screeningRepository,
			SeatRepository seatRepository,
			UserRepository userRepository) {
		this.bookingRepository = bookingRepository;
		this.screeningRepository = screeningRepository;
		this.seatRepository = seatRepository;
		this.userRepository = userRepository;
	}

	@Transactional
	public BookingResponse createBooking(BookingRequest request, String authenticatedEmail) {
		Screening screening = screeningRepository.findById(request.screeningId())
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.NOT_FOUND, "Screening with id " + request.screeningId() + " not found"));

		Seat seat = seatRepository.findById(request.seatId())
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.NOT_FOUND, "Seat with id " + request.seatId() + " not found"));

		User user = userRepository.findByEmail(authenticatedEmail)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Authenticated user not found"));

		Room screeningRoom = screening.getRoom();
		Room seatRoom = seat.getRoom();
		if (!screeningRoom.getId().equals(seatRoom.getId())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Seat does not belong to the room used by this screening");
		}

		if (!screening.getStartTime().isAfter(LocalDateTime.now())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Cannot book a screening that has already started");
		}

		if (bookingRepository.existsByScreening_IdAndSeat_Id(screening.getId(), seat.getId())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"This seat is already booked for this screening");
		}

		Booking booking = bookingRepository.saveAndFlush(new Booking(screening, seat, user));
		return toResponse(booking);
	}

	public List<BookingResponse> getAllBookings() {
		return bookingRepository.findAll().stream().map(this::toResponse).toList();
	}

	public BookingResponse getBookingById(Long id) {
		Booking booking = bookingRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
		return toResponse(booking);
	}

	public List<BookingResponse> getBookingsByScreeningId(Long screeningId) {
		if (!screeningRepository.existsById(screeningId)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,
					"Screening with id " + screeningId + " not found");
		}

		return bookingRepository.findByScreening_IdOrderByBookedAtAsc(screeningId)
				.stream().map(this::toResponse).toList();
	}

	private BookingResponse toResponse(Booking booking) {
		return new BookingResponse(
				booking.getId(),
				booking.getScreening().getId(),
				booking.getSeat().getId(),
				booking.getUser().getId(),
				booking.getBookedAt());
	}
}
