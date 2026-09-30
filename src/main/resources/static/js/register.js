import { api, credentialsStore } from "./api.js";

const form = document.querySelector("#registration-form");
const feedback = document.querySelector("#registration-feedback");
const submitButton = form.querySelector("button[type=submit]");

// Submit the registration details and display either a success message or API validation errors.
form.addEventListener("submit", async event => {
	event.preventDefault();
	feedback.className = "feedback";
	feedback.textContent = "";
	submitButton.disabled = true;
	submitButton.textContent = "Creating account…";

	const details = {
		name: form.elements.name.value.trim(),
		email: form.elements.email.value.trim(),
		password: form.elements.password.value
	};

	try {
		const user = await api.register(details);
		credentialsStore.save(details.email, details.password);
		feedback.className = "feedback success";
		feedback.textContent = `Welcome, ${user.name}. Your account is ready. You can continue to choose a movie.`;
		form.reset();
	} catch (error) {
		feedback.className = "feedback error";
		feedback.textContent = error.status === 409
			? "That email is already registered. Try signing in on the booking page."
			: error.message || "Could not create your account. Please check the details and try again.";
	} finally {
		submitButton.disabled = false;
		submitButton.textContent = "Create account";
	}
});
