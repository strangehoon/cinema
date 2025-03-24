package com.example.repository;

import com.example.entity.Screening;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ScreeningRepository extends JpaRepository<Screening, Long>, ScreeningRepositoryCustom {

    @Query("SELECT s FROM Screening s JOIN FETCH s.movie JOIN FETCH s.theater WHERE s.theater.id = :theaterId")
    List<Screening> findAllByTheaterIdWithMovieAndTheater(@Param("theaterId") int theaterId);
}
