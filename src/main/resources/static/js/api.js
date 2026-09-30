const API_ROOT = "/api";

async function request(path, options = {}) {
	const response = await fetch(`${API_ROOT}${path}`, {
		...options,
		headers: {
			...(options.body ? { "Content-Type": "application/json" } : {}),
			...(options.headers || {})
		}
	});

	const body = response.status === 204 ? null : await response.json().catch(() => null);
	if (!response.ok) {
		const validation = body?.fieldErrors
			? Object.entries(body.fieldErrors).map(([field, message]) => `${field}: ${message}`).join(" ")
			: "";
		const error = new Error(validation || body?.message || `Request failed (${response.status})`);
		error.status = response.status;
		error.data = body;
		throw error;
	}
	return body;
}

function basicAuthorization(email, password) {
	const bytes = new TextEncoder().encode(`${email}:${password}`);
	const binary = Array.from(bytes, byte => String.fromCharCode(byte)).join("");
	return `Basic ${btoa(binary)}`;
}

export const api = {
	getMovies: () => request("/movies"),
	getCinemas: () => request("/cinemas"),
	getRooms: cinemaId => request(`/cinemas/${cinemaId}/rooms`),
	getScreeningsForMovie: movieId => request(`/movies/${movieId}/screenings`),
	getSeatsForRoom: roomId => request(`/rooms/${roomId}/seats`),
	getBookingsForScreening: screeningId => request(`/screenings/${screeningId}/bookings`),
	register: details => request("/users", { method: "POST", body: JSON.stringify(details) }),
	createBooking: (booking, email, password) => request("/bookings", {
		method: "POST",
		body: JSON.stringify(booking),
		headers: { Authorization: basicAuthorization(email, password) }
	})
};

export const credentialsStore = {
	read() {
		const email = sessionStorage.getItem("cinema-booking-email");
		const password = sessionStorage.getItem("cinema-booking-password");
		return email && password ? { email, password } : null;
	},
	save(email, password) {
		sessionStorage.setItem("cinema-booking-email", email);
		sessionStorage.setItem("cinema-booking-password", password);
	},
	clear() {
		sessionStorage.removeItem("cinema-booking-email");
		sessionStorage.removeItem("cinema-booking-password");
	}
};
