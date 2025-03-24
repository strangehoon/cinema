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

    @GetMapping("/screenings")
    public List<MovieScreeningResponse> getSchedules(
            @RequestParam("theaterId") int theaterId,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "genre", required = false) String genre) {
        return screeningService.getSchedules(theaterId, title, genre);
    }
}
