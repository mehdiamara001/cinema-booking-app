package com.cinema.booking.booking;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

	boolean existsByScreening_IdAndSeat_Id(Long screeningId, Long seatId);

	List<Booking> findByScreening_IdOrderByBookedAtAsc(Long screeningId);
}
