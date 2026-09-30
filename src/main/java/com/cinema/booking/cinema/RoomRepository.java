package com.cinema.booking.cinema;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

	List<Room> findByCinema_Id(Long cinemaId);
}
