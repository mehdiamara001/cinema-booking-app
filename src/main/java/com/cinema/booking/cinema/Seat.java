package com.cinema.booking.cinema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "seats", uniqueConstraints = {
		@UniqueConstraint(name = "uk_seat_room_row_seat", columnNames = {"room_id", "row_number", "seat_number"})
})
public class Seat {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@Positive
	@Column(name = "row_number", nullable = false)
	private Integer rowNumber;

	@NotNull
	@Positive
	@Column(name = "seat_number", nullable = false)
	private Integer seatNumber;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "room_id", nullable = false)
	private Room room;

	protected Seat() {
		// Required by JPA.
	}

	public Seat(Integer rowNumber, Integer seatNumber, Room room) {
		this.rowNumber = rowNumber;
		this.seatNumber = seatNumber;
		this.room = room;
	}

	public Long getId() {
		return id;
	}

	public Integer getRowNumber() {
		return rowNumber;
	}

	public Integer getSeatNumber() {
		return seatNumber;
	}

	public Room getRoom() {
		return room;
	}
}
