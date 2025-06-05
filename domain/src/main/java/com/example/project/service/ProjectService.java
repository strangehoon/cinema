package com.example.project.service;

import com.example.db.entity.Project;
import com.example.db.repository.ProjectLikeCountDto;
import com.example.db.repository.ProjectRecommendResponse;
import com.example.db.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final RedisTemplate redisTemplate;
    private static final String PROJECT_SCORE_KEY = "project:likes";

    public List<ProjectRecommendResponse> getRecommendProjectsV1() {
        Pageable pageable = PageRequest.of(0, 10);
        return projectRepository.findTopProjectsByLikes(pageable);
    }

    public List<ProjectRecommendResponse> getRecommendProjectsV2() {
        Pageable pageable = PageRequest.of(0, 10);
        List<ProjectLikeCountDto> projectLikeCountDtos = projectRepository.findTopProjectLikeCounts(pageable);

        if (projectLikeCountDtos.isEmpty()) {
            return List.of();
        }

        // projectId만 추출
        List<Long> projectIds = projectLikeCountDtos.stream()
                .map(ProjectLikeCountDto::getProjectId)
                .toList();

        // 실제 Project 조회
        List<Project> projects = projectRepository.findAllById(projectIds);

        // Map<projectId, Project> 구성
        Map<Long, Project> projectMap = projects.stream()
                .collect(Collectors.toMap(Project::getId, Function.identity()));

        // 원래 순서(projectLikeCountDtos)를 기준으로 DTO 생성
        return projectLikeCountDtos.stream()
                .map(dto -> {
                    Project project = projectMap.get(dto.getProjectId());
                    return new ProjectRecommendResponse(
                            project.getId(),
                            project.getTitle(),
                            dto.getLikeCount()
                    );
                })
                .toList();
    }

    public List<ProjectRecommendResponse> getRecommendProjectsV3() {

        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
        Set<ZSetOperations.TypedTuple<Object>> topProjectsWithScores =
                zSetOps.reverseRangeWithScores(PROJECT_SCORE_KEY, 0, 12 - 1);

        if (topProjectsWithScores == null || topProjectsWithScores.isEmpty()) {
            return List.of();
        }

        List<Long> projectIds = new ArrayList<>();
        Map<Long, Long> scoreMap = new HashMap<>();

        for (ZSetOperations.TypedTuple<Object> tuple : topProjectsWithScores) {
            String projectIdStr = tuple.getValue().toString().replace("project_", "");
            Long projectId = Long.parseLong(projectIdStr);
            projectIds.add(projectId);
            scoreMap.put(projectId, tuple.getScore().longValue());
            System.out.println(projectIdStr+":"+scoreMap.get(projectId));
        }

        List<Project> projects = projectRepository.findAllById(projectIds);

        return projects.stream()
                .map(project -> ProjectRecommendResponse.of(project, scoreMap.get(project.getId())))
                .collect(Collectors.toList());
    }
}
