package com.cinema.booking.screening;

import com.cinema.booking.cinema.Room;
import com.cinema.booking.movie.Movie;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "screenings")
public class Screening {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "movie_id", nullable = false)
	private Movie movie;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "room_id", nullable = false)
	private Room room;

	@NotNull
	@Future
	private LocalDateTime startTime;

	protected Screening() {
		// Required by JPA.
	}

	public Screening(Movie movie, Room room, LocalDateTime startTime) {
		this.movie = movie;
		this.room = room;
		this.startTime = startTime;
	}

	public Long getId() {
		return id;
	}

	public Movie getMovie() {
		return movie;
	}

	public Room getRoom() {
		return room;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}
}
