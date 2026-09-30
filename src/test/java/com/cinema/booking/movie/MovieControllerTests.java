package com.cinema.booking.movie;

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
class MovieControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MovieRepository movieRepository;

	@BeforeEach
	void clearMovies() {
		movieRepository.deleteAll();
	}

	@Test
	void canCreateAndFetchMovie() throws Exception {
		String movieJson = """
				{
				  "title": "Arrival",
				  "description": "A science fiction film",
				  "duration": 116,
				  "genre": "Science Fiction"
				}
				""";

		String response = mockMvc.perform(post("/api/movies")
					.contentType(MediaType.APPLICATION_JSON)
					.content(movieJson))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.title").value("Arrival"))
				.andReturn().getResponse().getContentAsString();

		Number idValue = com.jayway.jsonpath.JsonPath.read(response, "$.id");
		long id = idValue.longValue();

		mockMvc.perform(get("/api/movies/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Arrival"))
				.andExpect(jsonPath("$.duration").value(116));
	}

	@Test
	void getAllMoviesReturnsSavedMovies() throws Exception {
		movieRepository.save(new Movie("Arrival", "A science fiction film", 116, "Science Fiction"));

		mockMvc.perform(get("/api/movies"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].title").value("Arrival"));
	}

	@Test
	void missingMovieReturnsNotFound() throws Exception {
		mockMvc.perform(get("/api/movies/999999"))
				.andExpect(status().isNotFound());
	}

	@Test
	void invalidMovieReturnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/movies")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"title\":\"\",\"duration\":0,\"genre\":\"Drama\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void missingRequiredDurationReturnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/movies")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"title\":\"Arrival\",\"genre\":\"Science Fiction\"}"))
				.andExpect(status().isBadRequest());
	}
}
