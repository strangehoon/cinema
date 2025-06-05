package com.example.db.repository;

import com.example.db.entity.Project;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("""
        SELECT new com.example.db.repository.ProjectRecommendResponse(
            p.id, p.title, COUNT(l.id)
        )
        FROM Like l
        JOIN l.project p
        GROUP BY p.id, p.title
        ORDER BY COUNT(l.id) DESC
        """)
    List<ProjectRecommendResponse> findTopProjectsByLikes(Pageable pageable);


    @Query("""
        SELECT new com.example.db.repository.ProjectLikeCountDto(l.project.id, COUNT(l))
        FROM Like l
        GROUP BY l.project.id
        ORDER BY COUNT(l) DESC
        """)
    List<ProjectLikeCountDto> findTopProjectLikeCounts(Pageable pageable);

}
