package com.cinema.booking.screening;

import com.cinema.booking.cinema.Cinema;
import com.cinema.booking.cinema.CinemaRepository;
import com.cinema.booking.cinema.Room;
import com.cinema.booking.cinema.RoomRepository;
import com.cinema.booking.cinema.SeatRepository;
import com.cinema.booking.movie.Movie;
import com.cinema.booking.movie.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ScreeningControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ScreeningRepository screeningRepository;

	@Autowired
	private MovieRepository movieRepository;

	@Autowired
	private RoomRepository roomRepository;

	@Autowired
	private SeatRepository seatRepository;

	@Autowired
	private CinemaRepository cinemaRepository;

	@BeforeEach
	void clearScreeningsAndRelatedData() {
		screeningRepository.deleteAll();
		seatRepository.deleteAll();
		roomRepository.deleteAll();
		cinemaRepository.deleteAll();
		movieRepository.deleteAll();
	}

	@Test
	void canCreateScreeningAndFetchIt() throws Exception {
		Movie movie = createMovie("Arrival");
		Room room = createRoom("Room 1");
		LocalDateTime startTime = futureStartTime();

		String response = mockMvc.perform(post("/api/screenings")
					.contentType(MediaType.APPLICATION_JSON)
					.content(screeningJson(movie.getId(), room.getId(), startTime)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.movieId").value(movie.getId()))
				.andExpect(jsonPath("$.roomId").value(room.getId()))
				.andReturn().getResponse().getContentAsString();

		Number idValue = com.jayway.jsonpath.JsonPath.read(response, "$.id");

		String fetchedScreening = mockMvc.perform(get("/api/screenings/{id}", idValue.longValue()))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		String fetchedStartTime = com.jayway.jsonpath.JsonPath.read(fetchedScreening, "$.startTime");
		org.assertj.core.api.Assertions.assertThat(LocalDateTime.parse(fetchedStartTime)).isEqualTo(startTime);
		mockMvc.perform(get("/api/screenings"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].movieId").value(movie.getId()));
	}

	@Test
	void invalidMovieIdReturnsClearNotFoundError() throws Exception {
		Room room = createRoom("Room 1");

		mockMvc.perform(post("/api/screenings")
					.contentType(MediaType.APPLICATION_JSON)
					.content(screeningJson(999999L, room.getId(), futureStartTime())))
				.andExpect(status().isNotFound())
				.andExpect(result -> org.assertj.core.api.Assertions.assertThat(result.getResponse().getContentAsString())
						.contains("Movie with id 999999 not found"));
	}

	@Test
	void invalidRoomIdReturnsClearNotFoundError() throws Exception {
		Movie movie = createMovie("Arrival");

		mockMvc.perform(post("/api/screenings")
					.contentType(MediaType.APPLICATION_JSON)
					.content(screeningJson(movie.getId(), 999999L, futureStartTime())))
				.andExpect(status().isNotFound())
				.andExpect(result -> org.assertj.core.api.Assertions.assertThat(result.getResponse().getContentAsString())
						.contains("Room with id 999999 not found"));
	}

	@Test
	void screeningInPastReturnsBadRequest() throws Exception {
		Movie movie = createMovie("Arrival");
		Room room = createRoom("Room 1");

		mockMvc.perform(post("/api/screenings")
					.contentType(MediaType.APPLICATION_JSON)
					.content(screeningJson(movie.getId(), room.getId(), LocalDateTime.now().minusDays(1))))
				.andExpect(status().isBadRequest());
	}

	@Test
	void canGetScreeningsByMovieId() throws Exception {
		Movie firstMovie = createMovie("Arrival");
		Movie secondMovie = createMovie("Dune");
		Room room = createRoom("Room 1");
		createScreening(firstMovie, room, futureStartTime());
		createScreening(secondMovie, room, futureStartTime().plusHours(2));

		mockMvc.perform(get("/api/movies/{movieId}/screenings", firstMovie.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].movieId").value(firstMovie.getId()))
				.andExpect(jsonPath("$[1]").doesNotExist());
	}

	@Test
	void canGetScreeningsByRoomId() throws Exception {
		Movie movie = createMovie("Arrival");
		Room firstRoom = createRoom("Room 1");
		Room secondRoom = createRoom("Room 2");
		createScreening(movie, firstRoom, futureStartTime());
		createScreening(movie, secondRoom, futureStartTime().plusHours(2));

		mockMvc.perform(get("/api/rooms/{roomId}/screenings", firstRoom.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].roomId").value(firstRoom.getId()))
				.andExpect(jsonPath("$[1]").doesNotExist());
	}

	private Movie createMovie(String title) {
		return movieRepository.save(new Movie(title, "A film description", 120, "Drama"));
	}

	private Room createRoom(String name) {
		Cinema cinema = cinemaRepository.save(new Cinema("Central Cinema", "10 Main Street"));
		return roomRepository.save(new Room(name, 120, cinema));
	}

	private Screening createScreening(Movie movie, Room room, LocalDateTime startTime) {
		return screeningRepository.save(new Screening(movie, room, startTime));
	}

	private LocalDateTime futureStartTime() {
		return LocalDateTime.now().plusDays(1).withNano(0);
	}

	private String screeningJson(Long movieId, Long roomId, LocalDateTime startTime) {
		return "{\"movieId\":" + movieId + ",\"roomId\":" + roomId
				+ ",\"startTime\":\"" + startTime + "\"}";
	}
}
