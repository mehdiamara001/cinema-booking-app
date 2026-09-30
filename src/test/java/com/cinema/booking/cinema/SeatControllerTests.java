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
class SeatControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CinemaRepository cinemaRepository;

	@Autowired
	private RoomRepository roomRepository;

	@Autowired
	private SeatRepository seatRepository;

	@BeforeEach
	void clearSeatsRoomsAndCinemas() {
		seatRepository.deleteAll();
		roomRepository.deleteAll();
		cinemaRepository.deleteAll();
	}

	@Test
	void canCreateSeatAndGetItById() throws Exception {
		Room room = createRoom("Room 1");
		String response = mockMvc.perform(post("/api/rooms/{roomId}/seats", room.getId())
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"rowNumber\":2,\"seatNumber\":5}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.rowNumber").value(2))
				.andExpect(jsonPath("$.seatNumber").value(5))
				.andReturn().getResponse().getContentAsString();

		Number idValue = com.jayway.jsonpath.JsonPath.read(response, "$.id");

		mockMvc.perform(get("/api/seats/{seatId}", idValue.longValue()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.rowNumber").value(2))
				.andExpect(jsonPath("$.seatNumber").value(5));
	}

	@Test
	void getSeatsForRoomReturnsSeatsInSeatOrder() throws Exception {
		Room room = createRoom("Room 1");
		seatRepository.save(new Seat(2, 4, room));
		seatRepository.save(new Seat(1, 3, room));

		mockMvc.perform(get("/api/rooms/{roomId}/seats", room.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].rowNumber").value(1))
				.andExpect(jsonPath("$[0].seatNumber").value(3))
				.andExpect(jsonPath("$[1].rowNumber").value(2))
				.andExpect(jsonPath("$[1].seatNumber").value(4));
	}

	@Test
	void rejectsDuplicateSeatInSameRoomButAllowsItInAnotherRoom() throws Exception {
		Room firstRoom = createRoom("Room 1");
		Room secondRoom = createRoom("Room 2");
		String seatJson = "{\"rowNumber\":1,\"seatNumber\":1}";

		mockMvc.perform(post("/api/rooms/{roomId}/seats", firstRoom.getId())
					.contentType(MediaType.APPLICATION_JSON).content(seatJson))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/rooms/{roomId}/seats", firstRoom.getId())
					.contentType(MediaType.APPLICATION_JSON).content(seatJson))
				.andExpect(status().isConflict());

		mockMvc.perform(post("/api/rooms/{roomId}/seats", secondRoom.getId())
					.contentType(MediaType.APPLICATION_JSON).content(seatJson))
				.andExpect(status().isCreated());
	}

	@Test
	void invalidRowOrSeatNumberReturnsBadRequest() throws Exception {
		Room room = createRoom("Room 1");

		mockMvc.perform(post("/api/rooms/{roomId}/seats", room.getId())
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"rowNumber\":0,\"seatNumber\":0}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void missingRoomOrSeatReturnsNotFound() throws Exception {
		mockMvc.perform(get("/api/rooms/999999/seats"))
				.andExpect(status().isNotFound());
		mockMvc.perform(post("/api/rooms/999999/seats")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"rowNumber\":1,\"seatNumber\":1}"))
				.andExpect(status().isNotFound());
		mockMvc.perform(get("/api/seats/999999"))
				.andExpect(status().isNotFound());
	}

	private Room createRoom(String roomName) {
		Cinema cinema = cinemaRepository.save(new Cinema("Central Cinema", "10 Main Street"));
		return roomRepository.save(new Room(roomName, 120, cinema));
	}
}
