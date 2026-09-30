package com.cinema.booking.booking;

import com.cinema.booking.cinema.Seat;
import com.cinema.booking.screening.Screening;
import com.cinema.booking.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings", uniqueConstraints = {
		@UniqueConstraint(
				name = "uk_booking_screening_seat",
				columnNames = {"screening_id", "seat_id"})
})
public class Booking {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "screening_id", nullable = false)
	private Screening screening;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "seat_id", nullable = false)
	private Seat seat;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@NotNull
	@Column(nullable = false, updatable = false)
	private LocalDateTime bookedAt;

	protected Booking() {
		// JPA uses this constructor when loading a booking from the database.
	}

	public Booking(Screening screening, Seat seat, User user) {
		this.screening = screening;
		this.seat = seat;
		this.user = user;
	}

	@PrePersist
	private void setBookedAtBeforeInsert() {
		if (bookedAt == null) {
			bookedAt = LocalDateTime.now();
		}
	}

	public Long getId() {
		return id;
	}

	public Screening getScreening() {
		return screening;
	}

	public Seat getSeat() {
		return seat;
	}

	public User getUser() {
		return user;
	}

	public LocalDateTime getBookedAt() {
		return bookedAt;
	}
}
