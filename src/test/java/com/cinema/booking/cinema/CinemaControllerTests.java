package com.cinema.booking.cinema;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CinemaControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CinemaRepository cinemaRepository;

	@Autowired
	private RoomRepository roomRepository;

	@BeforeEach
	void clearCinemasAndRooms() {
		roomRepository.deleteAll();
		cinemaRepository.deleteAll();
	}

	@Test
	void canCreateAndFetchCinema() throws Exception {
		String response = mockMvc.perform(post("/api/cinemas")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"name\":\"Central Cinema\",\"address\":\"10 Main Street\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("Central Cinema"))
				.andReturn().getResponse().getContentAsString();

		Number idValue = com.jayway.jsonpath.JsonPath.read(response, "$.id");

		mockMvc.perform(get("/api/cinemas/{id}", idValue.longValue()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.address").value("10 Main Street"));
	}

	@Test
	void getAllCinemasReturnsSavedCinemas() throws Exception {
		cinemaRepository.save(new Cinema("Central Cinema", "10 Main Street"));

		mockMvc.perform(get("/api/cinemas"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].name").value("Central Cinema"));
	}

	@Test
	void canCreateRoomAndGetRoomsForCinema() throws Exception {
		Cinema cinema = cinemaRepository.save(new Cinema("Central Cinema", "10 Main Street"));

		mockMvc.perform(post("/api/cinemas/{cinemaId}/rooms", cinema.getId())
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"name\":\"Room 1\",\"capacity\":120}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("Room 1"))
				.andExpect(jsonPath("$.capacity").value(120));

		mockMvc.perform(get("/api/cinemas/{cinemaId}/rooms", cinema.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].name").value("Room 1"))
				.andExpect(jsonPath("$[0].capacity").value(120));
	}

	@Test
	void invalidCinemaOrRoomInputReturnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/cinemas")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"name\":\" \",\"address\":\"10 Main Street\"}"))
				.andExpect(status().isBadRequest());

		Cinema cinema = cinemaRepository.save(new Cinema("Central Cinema", "10 Main Street"));
		mockMvc.perform(post("/api/cinemas/{cinemaId}/rooms", cinema.getId())
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"name\":\"Room 1\",\"capacity\":0}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void missingCinemaReturnsNotFound() throws Exception {
		mockMvc.perform(get("/api/cinemas/999999"))
				.andExpect(status().isNotFound());

		mockMvc.perform(get("/api/cinemas/999999/rooms"))
				.andExpect(status().isNotFound());
	}
}
