package com.example.movie.controller;

import com.example.movie.service.CinemaCatalog;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CinemaCatalogController {
    private final CinemaCatalog catalog;

    @GetMapping("/movies")
    public CinemaCatalog.MoviePage movies(@RequestParam(required = false) String title,
                                          @RequestParam(required = false) String genre,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "12") int size) {
        return catalog.movies(title, genre, page, size);
    }

    @GetMapping("/movies/{id}")
    public CinemaCatalog.MovieView movie(@PathVariable Long id) { return catalog.movie(id); }

    @GetMapping("/screenings/{id}/seats")
    public CinemaCatalog.SeatMap seats(@PathVariable Long id) { return catalog.seats(id); }
}
