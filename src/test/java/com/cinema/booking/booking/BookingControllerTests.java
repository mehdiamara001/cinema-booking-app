package com.cinema.booking.booking;

import com.cinema.booking.cinema.Cinema;
import com.cinema.booking.cinema.CinemaRepository;
import com.cinema.booking.cinema.Room;
import com.cinema.booking.cinema.RoomRepository;
import com.cinema.booking.cinema.Seat;
import com.cinema.booking.cinema.SeatRepository;
import com.cinema.booking.movie.Movie;
import com.cinema.booking.movie.MovieRepository;
import com.cinema.booking.screening.Screening;
import com.cinema.booking.screening.ScreeningRepository;
import com.cinema.booking.user.User;
import com.cinema.booking.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookingControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private BookingRepository bookingRepository;

	@Autowired
	private ScreeningRepository screeningRepository;

	@Autowired
	private SeatRepository seatRepository;

	@Autowired
	private RoomRepository roomRepository;

	@Autowired
	private CinemaRepository cinemaRepository;

	@Autowired
	private MovieRepository movieRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	void clearBookingsAndRelatedData() {
		bookingRepository.deleteAll();
		userRepository.deleteAll();
		screeningRepository.deleteAll();
		seatRepository.deleteAll();
		roomRepository.deleteAll();
		cinemaRepository.deleteAll();
		movieRepository.deleteAll();
	}

	@Test
	void canCreateBookingAndGetItById() throws Exception {
		Screening screening = createScreening("Arrival", "Room 1");
		Seat seat = createSeat(screening.getRoom(), 1, 1);
		User user = createUser();

		String response = mockMvc.perform(post("/api/bookings")
					.with(httpBasic("test@example.com", "test-password"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(bookingJson(screening.getId(), seat.getId())))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.bookingId").isNumber())
				.andExpect(jsonPath("$.screeningId").value(screening.getId()))
				.andExpect(jsonPath("$.seatId").value(seat.getId()))
				.andExpect(jsonPath("$.userId").value(user.getId()))
				.andExpect(jsonPath("$.bookedAt").isNotEmpty())
				.andReturn().getResponse().getContentAsString();

		Number idValue = com.jayway.jsonpath.JsonPath.read(response, "$.bookingId");
		mockMvc.perform(get("/api/bookings/{id}", idValue.longValue()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.screeningId").value(screening.getId()));
	}

	@Test
	void unauthenticatedBookingCreationIsRejected() throws Exception {
		Screening screening = createScreening("Arrival", "Room 1");
		Seat seat = createSeat(screening.getRoom(), 1, 1);

		mockMvc.perform(post("/api/bookings")
					.contentType(MediaType.APPLICATION_JSON)
					.content(bookingJson(screening.getId(), seat.getId())))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.message").value("Authentication is required"))
				.andExpect(jsonPath("$.fieldErrors").isMap());
	}

	@Test
	void invalidCredentialsAreRejected() throws Exception {
		Screening screening = createScreening("Arrival", "Room 1");
		Seat seat = createSeat(screening.getRoom(), 1, 1);
		createUser();

		mockMvc.perform(post("/api/bookings")
					.with(httpBasic("test@example.com", "wrong-password"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(bookingJson(screening.getId(), seat.getId())))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void bookingAlwaysUsesAuthenticatedUserEvenIfRequestIncludesAnotherUserId() throws Exception {
		Screening screening = createScreening("Arrival", "Room 1");
		Seat seat = createSeat(screening.getRoom(), 1, 1);
		User authenticatedUser = createUser();
		User otherUser = userRepository.save(new User(
				"Other User", "other@example.com", passwordEncoder.encode("other-password")));
		String bodyWithUntrustedUserId = "{\"screeningId\":" + screening.getId()
				+ ",\"seatId\":" + seat.getId() + ",\"userId\":" + otherUser.getId() + "}";

		mockMvc.perform(post("/api/bookings")
					.with(httpBasic("test@example.com", "test-password"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(bodyWithUntrustedUserId))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.userId").value(authenticatedUser.getId()));
	}

	@Test
	void unknownScreeningReturnsNotFound() throws Exception {
		Screening screening = createScreening("Arrival", "Room 1");
		Seat seat = createSeat(screening.getRoom(), 1, 1);
		User user = createUser();

		mockMvc.perform(post("/api/bookings")
					.with(httpBasic("test@example.com", "test-password"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(bookingJson(999999L, seat.getId())))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Screening with id 999999 not found"));
	}

	@Test
	void unknownSeatReturnsNotFound() throws Exception {
		Screening screening = createScreening("Arrival", "Room 1");
		User user = createUser();

		mockMvc.perform(post("/api/bookings")
					.with(httpBasic("test@example.com", "test-password"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(bookingJson(screening.getId(), 999999L)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Seat with id 999999 not found"));
	}

	@Test
	void seatFromWrongRoomReturnsBadRequest() throws Exception {
		Screening screening = createScreening("Arrival", "Room 1");
		Room otherRoom = createRoom("Room 2");
		Seat seat = createSeat(otherRoom, 1, 1);
		User user = createUser();

		mockMvc.perform(post("/api/bookings")
					.with(httpBasic("test@example.com", "test-password"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(bookingJson(screening.getId(), seat.getId())))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Seat does not belong to the room used by this screening"));
	}

	@Test
	void duplicateBookingReturnsConflict() throws Exception {
		Screening screening = createScreening("Arrival", "Room 1");
		Seat seat = createSeat(screening.getRoom(), 1, 1);
		User user = createUser();
		String request = bookingJson(screening.getId(), seat.getId());

		mockMvc.perform(post("/api/bookings").with(httpBasic("test@example.com", "test-password"))
					.contentType(MediaType.APPLICATION_JSON).content(request))
				.andExpect(status().isCreated());
		mockMvc.perform(post("/api/bookings").with(httpBasic("test@example.com", "test-password"))
					.contentType(MediaType.APPLICATION_JSON).content(request))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("This seat is already booked for this screening"));

		assertThrows(DataIntegrityViolationException.class,
				() -> bookingRepository.saveAndFlush(new Booking(screening, seat, user)));
	}

	@Test
	void bookingPastScreeningReturnsBadRequest() throws Exception {
		Screening screening = createScreening("Arrival", "Room 1");
		Seat seat = createSeat(screening.getRoom(), 1, 1);
		User user = createUser();
		jdbcTemplate.update("UPDATE screenings SET start_time = ? WHERE id = ?",
				Timestamp.valueOf(LocalDateTime.now().minusDays(1)), screening.getId());

		mockMvc.perform(post("/api/bookings")
					.with(httpBasic("test@example.com", "test-password"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(bookingJson(screening.getId(), seat.getId())))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Cannot book a screening that has already started"));
	}

	@Test
	void canListBookingsByScreening() throws Exception {
		Screening screening = createScreening("Arrival", "Room 1");
		Screening anotherScreening = createScreening("Dune", "Room 1");
		Seat firstSeat = createSeat(screening.getRoom(), 1, 1);
		Seat secondSeat = createSeat(screening.getRoom(), 1, 2);
		Seat thirdSeat = createSeat(screening.getRoom(), 1, 3);
		User user = createUser();
		bookingRepository.save(new Booking(screening, firstSeat, user));
		bookingRepository.save(new Booking(screening, secondSeat, user));
		bookingRepository.save(new Booking(anotherScreening, thirdSeat, user));

		mockMvc.perform(get("/api/screenings/{screeningId}/bookings", screening.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].screeningId").value(screening.getId()))
				.andExpect(jsonPath("$[1].screeningId").value(screening.getId()))
				.andExpect(jsonPath("$[2]").doesNotExist());
	}

	private Screening createScreening(String movieTitle, String roomName) {
		Movie movie = movieRepository.save(new Movie(movieTitle, "A film description", 120, "Drama"));
		Room room = createRoom(roomName);
		return screeningRepository.save(new Screening(movie, room, LocalDateTime.now().plusDays(1)));
	}

	private Room createRoom(String roomName) {
		Cinema cinema = cinemaRepository.save(new Cinema("Central Cinema", "10 Main Street"));
		return roomRepository.save(new Room(roomName, 120, cinema));
	}

	private Seat createSeat(Room room, int rowNumber, int seatNumber) {
		return seatRepository.save(new Seat(rowNumber, seatNumber, room));
	}

	private User createUser() {
		return userRepository.save(new User("Test User", "test@example.com", passwordEncoder.encode("test-password")));
	}

	private String bookingJson(Long screeningId, Long seatId) {
		return "{\"screeningId\":" + screeningId + ",\"seatId\":" + seatId + "}";
	}
}
