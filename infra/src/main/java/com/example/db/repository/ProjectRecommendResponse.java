package com.example.db.repository;

import com.example.db.entity.Project;
import lombok.Getter;

@Getter
public class ProjectRecommendResponse {
    private final Long id;
    private final String title;
    private final Long likeCnt;

    public ProjectRecommendResponse(Long id, String title, Long likeCnt) {
        this.id = id;
        this.title = title;
        this.likeCnt = likeCnt;
    }

    public static ProjectRecommendResponse of(Project project, Long likeCnt) {
        return new ProjectRecommendResponse(
                project.getId(),
                project.getTitle(),
                likeCnt
        );
    }
}
