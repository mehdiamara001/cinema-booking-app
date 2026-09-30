package com.cinema.booking.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	OpenAPI cinemaBookingOpenApi() {
		return new OpenAPI()
				.info(new Info()
						.title("Cinema Booking API")
						.version("v1")
						.description("REST API for movies, cinemas, rooms, seats, screenings, users, and bookings."))
				.components(new Components().addSecuritySchemes("basicAuth",
						new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic")));
	}
}
