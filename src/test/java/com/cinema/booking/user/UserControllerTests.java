package com.cinema.booking.user;

import com.cinema.booking.booking.BookingRepository;
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
class UserControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BookingRepository bookingRepository;

	@BeforeEach
	void clearUsers() {
		bookingRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	void canCreateAndFetchUserWithoutExposingPassword() throws Exception {
		String response = mockMvc.perform(post("/api/users")
					.contentType(MediaType.APPLICATION_JSON)
					.content(userJson("Amina", "amina@example.com", "my-secret-password")))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("Amina"))
				.andExpect(jsonPath("$.email").value("amina@example.com"))
				.andExpect(jsonPath("$.password").doesNotExist())
				.andReturn().getResponse().getContentAsString();

		Number idValue = com.jayway.jsonpath.JsonPath.read(response, "$.id");
		User savedUser = userRepository.findById(idValue.longValue()).orElseThrow();
		org.assertj.core.api.Assertions.assertThat(savedUser.getPassword())
				.startsWith("$2")
				.isNotEqualTo("my-secret-password");

		mockMvc.perform(get("/api/users/{id}", idValue.longValue()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Amina"))
				.andExpect(jsonPath("$.password").doesNotExist());
	}

	@Test
	void getAllUsersDoesNotExposePasswords() throws Exception {
		userRepository.save(new User("Test User", "test@example.com", "test-hash"));

		mockMvc.perform(get("/api/users"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].email").value("test@example.com"))
				.andExpect(jsonPath("$[0].password").doesNotExist());
	}

	@Test
	void duplicateEmailReturnsConflictIncludingDifferentCase() throws Exception {
		mockMvc.perform(post("/api/users")
					.contentType(MediaType.APPLICATION_JSON)
					.content(userJson("Amina", "amina@example.com", "secret-one")))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/users")
					.contentType(MediaType.APPLICATION_JSON)
					.content(userJson("Another Name", "AMINA@example.com", "secret-two")))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("Email is already registered"));
	}

	@Test
	void invalidEmailOrMissingRequiredFieldsReturnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/users")
					.contentType(MediaType.APPLICATION_JSON)
					.content(userJson("Amina", "not-an-email", "secret-password")))
				.andExpect(status().isBadRequest());

		mockMvc.perform(post("/api/users")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"name\":\"\",\"email\":\"\",\"password\":\"\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void missingUserReturnsNotFound() throws Exception {
		mockMvc.perform(get("/api/users/999999"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("User not found"));
	}

	private String userJson(String name, String email, String password) {
		return "{\"name\":\"" + name + "\",\"email\":\"" + email
				+ "\",\"password\":\"" + password + "\"}";
	}
}
