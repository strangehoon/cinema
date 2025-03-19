package com.example.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "movies")
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    private String title;

    @Column
    private String rating;

    @Column
    private LocalDateTime releaseDate;

    @Column
    private String thumbnailImage;

    @Column
    private int runningTime;

    @Column
    private String genre;

    @OneToMany(mappedBy = "movie", fetch = FetchType.LAZY)
    private List<Screening> screenings = new ArrayList<>();

    public Movie(String title, String rating, LocalDateTime releaseDate, String thumbnailImage, int runningTime, String genre){
        this.title = title;
        this.rating = rating;
        this.releaseDate = releaseDate;
        this.thumbnailImage = thumbnailImage;
        this.runningTime = runningTime;
        this.genre = genre;
    }
}