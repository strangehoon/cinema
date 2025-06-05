package com.example.db.repository;

import lombok.Getter;

@Getter
public class ProjectLikeCountDto {

    private Long projectId;
    private Long likeCount;

    public ProjectLikeCountDto(Long projectId, Long likeCount) {
        this.projectId = projectId;
        this.likeCount = likeCount;
    }
}
