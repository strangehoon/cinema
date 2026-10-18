package com.example.db.repository;

import com.example.db.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long>, MovieRepositoryCustom{
    @Query("select distinct m from Movie m left join fetch m.screenings s left join fetch s.theater where m.id in :ids")
    List<Movie> findAllWithScreeningsByIdIn(@Param("ids") List<Long> ids);
}
