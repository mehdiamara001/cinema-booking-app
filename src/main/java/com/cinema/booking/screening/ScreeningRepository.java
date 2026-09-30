package com.cinema.booking.screening;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScreeningRepository extends JpaRepository<Screening, Long> {

	List<Screening> findByMovie_IdOrderByStartTimeAsc(Long movieId);

	List<Screening> findByRoom_IdOrderByStartTimeAsc(Long roomId);
}
