package com.example;

import com.example.dto.MovieScreeningResponse;
import com.example.entity.Movie;
import com.example.entity.Screening;
import com.example.repository.ScreeningRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ScreeningService {

    private final ScreeningRepository screeningRepository;

    public List<MovieScreeningResponse> getSchedules(Long theaterId) {
        List<Screening> screening = screeningRepository.findByTheaterId(theaterId);

        Map<Movie, List<Screening>> groupedByScreen = screening.stream()
                .collect(Collectors.groupingBy(Screening::getMovie));

        return groupedByScreen.entrySet().stream()
                .sorted((e1, e2) -> e2.getKey().getReleaseDate().compareTo(e1.getKey().getReleaseDate()))
                .map(entry -> MovieScreeningResponse.from(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }
}
