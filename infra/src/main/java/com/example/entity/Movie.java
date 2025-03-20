package com.example.entity;

import com.example.enums.Genre;
import com.example.enums.Rating;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "movies")
public class Movie extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    private String title;

    @Column
    @Enumerated(EnumType.STRING)
    private Rating rating;

    @Column
    private LocalDateTime releaseDate;

    @Column
    private String thumbnailImage;

    @Column
    private int runningTime;

    @Column
    @Enumerated(EnumType.STRING)
    private Genre genre;

    @OneToMany(mappedBy = "movie", fetch = FetchType.LAZY)
    private List<Screening> screenings = new ArrayList<>();

    @Builder
    private Movie(String title, Rating rating, LocalDateTime releaseDate, String thumbnailImage, int runningTime, Genre genre) {
        this.title = title;
        this.rating = rating;
        this.releaseDate = releaseDate;
        this.thumbnailImage = thumbnailImage;
        this.runningTime = runningTime;
        this.genre = genre;
    }
}