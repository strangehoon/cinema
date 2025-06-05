package com.example.project.controller;

import com.example.common.ApiResponse;
import com.example.db.repository.ProjectRecommendResponse;
import com.example.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping("/recommend/v1")
    public ApiResponse<List<ProjectRecommendResponse>> getRecommendedProjectV1() {
        return ApiResponse.ok(projectService.getRecommendProjectsV1());
    }

    @GetMapping("/recommend/v2")
    public ApiResponse<List<ProjectRecommendResponse>> getRecommendedProjectV2() {
        return ApiResponse.ok(projectService.getRecommendProjectsV2());
    }

    @GetMapping("/recommend/v3")
    public ApiResponse<List<ProjectRecommendResponse>> getRecommendedProjectV3() {
        return ApiResponse.ok(projectService.getRecommendProjectsV3());
    }

}
