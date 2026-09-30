package com.cinema.booking.cinema;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

	List<Seat> findByRoom_IdOrderByRowNumberAscSeatNumberAsc(Long roomId);

	boolean existsByRoom_IdAndRowNumberAndSeatNumber(Long roomId, Integer rowNumber, Integer seatNumber);
}
