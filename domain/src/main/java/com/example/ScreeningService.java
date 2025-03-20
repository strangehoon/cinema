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

    public List<MovieScreeningResponse> getSchedules(int theaterId) {
        List<Screening> screening = screeningRepository.findAllByTheaterIdWithMovieAndTheater(theaterId);

        Map<Movie, List<Screening>> groupedByScreen = screening.stream()
                .collect(Collectors.groupingBy(Screening::getMovie));

        return groupedByScreen.entrySet().stream()
                // 상영일 기준 내림차순 정렬
                .sorted(Comparator.comparing(entry -> entry.getKey().getReleaseDate(),
                        Comparator.reverseOrder()))
                // 엔트리를 MovieScreeningResponse로 변환
                .map(entry -> MovieScreeningResponse.of(
                        entry.getKey(),
                        entry.getValue()))
                .collect(Collectors.toList());
    }
}
