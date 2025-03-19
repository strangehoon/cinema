package com.example;

import com.example.dto.MovieScreeningResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ScreeningController {

    private final ScreeningService screeningService;

    @GetMapping("/api/screenings")
    public List<MovieScreeningResponse> getSchedules(@RequestParam("theaterId") Long theaterId) {
        return screeningService.getSchedules(theaterId);
    }
}
