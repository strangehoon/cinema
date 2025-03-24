package com.example;

import com.example.dto.MovieScreeningResponse;
import com.example.entity.Movie;
import com.example.entity.Screening;
import com.example.enums.Genre;
import com.example.repository.ScreeningRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScreeningService {

    private final ScreeningRepository screeningRepository;

    public List<MovieScreeningResponse> getSchedules(int theaterId, String title, String genre) {

        Genre genreEnum = null;
        if(genre!=null)
            genreEnum = Genre.valueOf(genre.toUpperCase());

        List<Screening> screenings = screeningRepository.searchScreenings(theaterId, title, genreEnum);

        Map<Movie, List<Screening>> groupedByScreen = screenings.stream()
                .collect(Collectors.groupingBy(Screening::getMovie));

        return groupedByScreen.entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().getReleaseDate(),
                        Comparator.reverseOrder()))
                .map(entry -> MovieScreeningResponse.of(
                        entry.getKey(),
                        entry.getValue()))
                .collect(Collectors.toList());
    }
}
