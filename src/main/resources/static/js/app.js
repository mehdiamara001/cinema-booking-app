import { api, credentialsStore } from "./api.js";

const elements = {
	movieGrid: document.querySelector("#movie-grid"),
	movieCount: document.querySelector("#movie-count"),
	notice: document.querySelector("#page-notice"),
	screeningsSection: document.querySelector("#screenings-section"),
	screeningList: document.querySelector("#screening-list"),
	selectedMovieLabel: document.querySelector("#selected-movie-label"),
	seatsSection: document.querySelector("#seats-section"),
	selectedScreeningLabel: document.querySelector("#selected-screening-label"),
	seatGrid: document.querySelector("#seat-grid"),
	seatSelectionLabel: document.querySelector("#seat-selection-label"),
	bookButton: document.querySelector("#book-button"),
	authDialog: document.querySelector("#auth-dialog"),
	authForm: document.querySelector("#auth-form"),
	authFeedback: document.querySelector("#auth-feedback"),
	resultDialog: document.querySelector("#result-dialog"),
	accountChip: document.querySelector("#account-chip"),
	accountLabel: document.querySelector("#account-label")
};

const state = {
	selectedMovie: null,
	selectedScreening: null,
	selectedSeat: null,
	bookedSeatIds: new Set(),
	roomDirectory: new Map()
};

// Load the public movie list and build a room ID -> room/cinema label lookup.
async function initialize() {
	updateAccountChip();
	try {
		const [movies] = await Promise.all([api.getMovies(), loadRoomDirectory()]);
		renderMovies(movies);
	} catch (error) {
		showPageNotice(error.message || "Could not load cinema information. Please refresh and try again.", "error");
		elements.movieGrid.replaceChildren();
	}
}

// The screening response includes roomId only; reuse public cinema/room APIs for display names.
async function loadRoomDirectory() {
	const cinemas = await api.getCinemas();
	const roomGroups = await Promise.all(cinemas.map(async cinema => ({
		cinema,
		rooms: await api.getRooms(cinema.id)
	})));
	for (const { cinema, rooms } of roomGroups) {
		for (const room of rooms) state.roomDirectory.set(room.id, { name: room.name, cinema: cinema.name });
	}
}

// Create movie cards from API data without inserting server text as HTML.
function renderMovies(movies) {
	elements.movieCount.textContent = `${movies.length} ${movies.length === 1 ? "film" : "films"}`;
	elements.movieGrid.replaceChildren();
	if (movies.length === 0) {
		elements.movieGrid.append(makeMessage("No movies are listed yet. Please check back soon."));
		return;
	}

	movies.forEach((movie, index) => {
		const card = document.createElement("article");
		card.className = "movie-card";
		const poster = document.createElement("div");
		poster.className = `movie-poster poster-${index % 5}`;
		poster.setAttribute("aria-hidden", "true");
		const posterTitle = document.createElement("span");
		posterTitle.textContent = movie.title;
		poster.append(posterTitle);
		const details = document.createElement("div");
		details.className = "movie-details";
		const genre = document.createElement("span");
		genre.className = "pill";
		genre.textContent = movie.genre;
		const title = document.createElement("h3");
		title.textContent = movie.title;
		const meta = document.createElement("p");
		meta.className = "muted movie-meta";
		meta.textContent = `${movie.duration} min`;
		const description = document.createElement("p");
		description.className = "movie-description";
		description.textContent = movie.description || "A great story is waiting to be discovered.";
		const button = document.createElement("button");
		button.type = "button";
		button.className = "button button-outline button-full";
		button.textContent = "See screenings";
		button.addEventListener("click", () => selectMovie(movie, card));
		details.append(genre, title, meta, description, button);
		card.append(poster, details);
		elements.movieGrid.append(card);
	});
}

// Selecting a new movie resets later choices, then loads screenings for that movie.
async function selectMovie(movie, card) {
	state.selectedMovie = movie;
	state.selectedScreening = null;
	state.selectedSeat = null;
	document.querySelectorAll(".movie-card").forEach(item => item.classList.remove("selected"));
	card.classList.add("selected");
	elements.selectedMovieLabel.textContent = movie.title;
	elements.screeningsSection.hidden = false;
	elements.seatsSection.hidden = true;
	elements.screeningList.replaceChildren(makeMessage("Loading screenings…", "loading"));
	setActiveStep(2);
	elements.screeningsSection.scrollIntoView({ behavior: "smooth", block: "start" });
	try {
		const screenings = await api.getScreeningsForMovie(movie.id);
		renderScreenings(screenings);
	} catch (error) {
		elements.screeningList.replaceChildren(makeMessage(error.message || "Could not load screenings.", "error"));
	}
}

// Show times in the viewer's locale and pair each screening with its room lookup result.
function renderScreenings(screenings) {
	elements.screeningList.replaceChildren();
	if (screenings.length === 0) {
		elements.screeningList.append(makeMessage("There are no upcoming screenings for this movie."));
		return;
	}

	for (const screening of screenings) {
		const room = state.roomDirectory.get(screening.roomId);
		const date = new Date(screening.startTime);
		const button = document.createElement("button");
		button.type = "button";
		button.className = "screening-card";
		const datePart = document.createElement("span");
		datePart.className = "screening-date";
		datePart.textContent = new Intl.DateTimeFormat(undefined, { weekday: "short", month: "short", day: "numeric" }).format(date);
		const timePart = document.createElement("strong");
		timePart.className = "screening-time";
		timePart.textContent = new Intl.DateTimeFormat(undefined, { hour: "numeric", minute: "2-digit" }).format(date);
		const roomPart = document.createElement("span");
		roomPart.className = "screening-room";
		roomPart.textContent = room ? `${room.name} · ${room.cinema}` : `Room ${screening.roomId}`;
		const arrow = document.createElement("span");
		arrow.className = "screening-arrow";
		arrow.textContent = "→";
		button.append(datePart, timePart, roomPart, arrow);
		button.addEventListener("click", () => selectScreening(screening, room));
		elements.screeningList.append(button);
	}
}

// Load seats and current bookings together, then mark unavailable seats in the map.
async function selectScreening(screening, room) {
	state.selectedScreening = screening;
	state.selectedSeat = null;
	const date = new Date(screening.startTime);
	elements.selectedScreeningLabel.textContent = `${new Intl.DateTimeFormat(undefined, {
		weekday: "long", month: "long", day: "numeric", hour: "numeric", minute: "2-digit"
	}).format(date)} · ${room ? `${room.name}, ${room.cinema}` : `Room ${screening.roomId}`}`;
	elements.seatsSection.hidden = false;
	elements.seatGrid.replaceChildren(makeMessage("Loading seats…", "loading"));
	elements.seatsSection.scrollIntoView({ behavior: "smooth", block: "start" });
	setActiveStep(3);
	try {
		const [seats, bookings] = await Promise.all([
			api.getSeatsForRoom(screening.roomId),
			api.getBookingsForScreening(screening.id)
		]);
		state.bookedSeatIds = new Set(bookings.map(booking => booking.seatId));
		renderSeatGrid(seats);
	} catch (error) {
		elements.seatGrid.replaceChildren(makeMessage(error.message || "Could not load seats.", "error"));
	}
}

// Group seats by row and render available, selected, and already-booked states.
function renderSeatGrid(seats) {
	elements.seatGrid.replaceChildren();
	if (seats.length === 0) {
		elements.seatGrid.append(makeMessage("This room has no seats configured yet."));
		return;
	}
	const rows = new Map();
	for (const seat of seats) {
		if (!rows.has(seat.rowNumber)) rows.set(seat.rowNumber, []);
		rows.get(seat.rowNumber).push(seat);
	}
	for (const [rowNumber, rowSeats] of [...rows.entries()].sort(([a], [b]) => a - b)) {
		const row = document.createElement("div");
		row.className = "seat-row";
		const label = document.createElement("span");
		label.className = "row-label";
		label.textContent = `R${rowNumber}`;
		const seatsInRow = document.createElement("div");
		seatsInRow.className = "seat-row-items";
		rowSeats.sort((a, b) => a.seatNumber - b.seatNumber).forEach(seat => {
			const isBooked = state.bookedSeatIds.has(seat.id);
			const button = document.createElement("button");
			button.type = "button";
			button.className = `seat ${isBooked ? "taken" : "available"}`;
			button.textContent = seat.seatNumber;
			button.title = `Row ${seat.rowNumber}, seat ${seat.seatNumber}${isBooked ? " (booked)" : ""}`;
			button.setAttribute("aria-label", button.title);
			button.disabled = isBooked;
			if (!isBooked) button.addEventListener("click", () => selectSeat(seat, button));
			seatsInRow.append(button);
		});
		row.append(label, seatsInRow);
		elements.seatGrid.append(row);
	}
	updateSeatSelection();
}

// Keep only one seat selected and update the booking button's accessible summary.
function selectSeat(seat, button) {
	document.querySelectorAll(".seat.chosen").forEach(item => item.classList.replace("chosen", "available"));
	button.classList.replace("available", "chosen");
	state.selectedSeat = seat;
	updateSeatSelection();
}

function updateSeatSelection() {
	const seat = state.selectedSeat;
	elements.seatSelectionLabel.textContent = seat
		? `Selected: row ${seat.rowNumber}, seat ${seat.seatNumber}`
		: "Select an available seat to continue.";
	elements.bookButton.disabled = !seat;
}

// Submit only screeningId and seatId; the backend takes the user from HTTP Basic credentials.
async function bookSelectedSeat() {
	if (!state.selectedScreening || !state.selectedSeat) return;
	let credentials = credentialsStore.read();
	if (!credentials) {
		openAuthDialog();
		return;
	}
	elements.bookButton.disabled = true;
	elements.bookButton.textContent = "Reserving seat…";
	try {
		const booking = await api.createBooking({
			screeningId: state.selectedScreening.id,
			seatId: state.selectedSeat.id
		}, credentials.email, credentials.password);
		showResult(true, "Your seat is reserved", `Booking #${booking.bookingId} is confirmed. We look forward to seeing you.`);
		await selectScreening(state.selectedScreening, state.roomDirectory.get(state.selectedScreening.roomId));
	} catch (error) {
		if (error.status === 401) {
			credentialsStore.clear();
			updateAccountChip();
			openAuthDialog(credentials.email);
			elements.authFeedback.textContent = "Email or password is incorrect. Please try again.";
		} else if (error.status === 409) {
			showResult(false, "That seat was just booked", "Someone else reserved this seat moments ago. Choose another available seat.");
			await selectScreening(state.selectedScreening, state.roomDirectory.get(state.selectedScreening.roomId));
		} else if (error.status === 400) {
			showResult(false, "We couldn’t complete that booking", error.message || "This screening or seat is no longer valid.");
		} else {
			showResult(false, "Booking unavailable", error.message || "Please try again in a moment.");
		}
	} finally {
		elements.bookButton.textContent = "Continue to booking";
		updateSeatSelection();
	}
}

// Ask for credentials when none are stored for this tab, or when stored credentials were rejected.
function openAuthDialog(email = "") {
	elements.authFeedback.textContent = "";
	elements.authForm.elements.email.value = email;
	elements.authDialog.showModal();
	if (!email) elements.authForm.elements.email.focus();
}

function showResult(success, title, message) {
	document.querySelector("#result-icon").textContent = success ? "✓" : "!";
	document.querySelector("#result-icon").classList.toggle("result-failure", !success);
	document.querySelector("#result-title").textContent = title;
	document.querySelector("#result-message").textContent = message;
	elements.resultDialog.showModal();
}

function showPageNotice(message, type = "error") {
	elements.notice.hidden = false;
	elements.notice.className = `notice ${type}`;
	elements.notice.textContent = message;
}

function makeMessage(message, className = "empty-state") {
	const paragraph = document.createElement("p");
	paragraph.className = className;
	paragraph.textContent = message;
	return paragraph;
}

function setActiveStep(step) {
	document.querySelectorAll("[data-step-indicator]").forEach(item => {
		const number = Number(item.dataset.stepIndicator);
		item.classList.toggle("active", number <= step);
		item.classList.toggle("current", number === step);
	});
}

function updateAccountChip() {
	const credentials = credentialsStore.read();
	elements.accountChip.hidden = !credentials;
	if (credentials) elements.accountLabel.textContent = credentials.email;
}

elements.bookButton.addEventListener("click", bookSelectedSeat);
elements.authForm.addEventListener("submit", async event => {
	event.preventDefault();
	const email = elements.authForm.elements.email.value.trim();
	const password = elements.authForm.elements.password.value;
	credentialsStore.save(email, password);
	elements.authDialog.close();
	updateAccountChip();
	await bookSelectedSeat();
});
document.querySelector("#close-dialog").addEventListener("click", () => elements.authDialog.close());
document.querySelector("#close-result").addEventListener("click", () => elements.resultDialog.close());
document.querySelector("#result-done").addEventListener("click", () => elements.resultDialog.close());
document.querySelector("#sign-out-button").addEventListener("click", () => {
	credentialsStore.clear();
	updateAccountChip();
});

initialize();
