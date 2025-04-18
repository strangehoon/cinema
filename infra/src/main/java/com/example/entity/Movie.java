package com.example.entity;

import com.example.enums.Genre;
import com.example.enums.Rating;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "movies")
public class Movie extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30)
    private String title;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private Rating rating;

    @Column
    private LocalDate releasedDate;

    @Column(length = 50)
    private String thumbnailImage;

    @Column
    private int runningTimeMin;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private Genre genre;

    @OneToMany(mappedBy = "movie", fetch = FetchType.LAZY)
    //@BatchSize(size = 100)
    private List<Screening> screenings = new ArrayList<>();

    @Builder
    private Movie(String title, Rating rating, LocalDate releasedDate, String thumbnailImage, int runningTimeMin, Genre genre) {
        this.title = title;
        this.rating = rating;
        this.releasedDate = releasedDate;
        this.thumbnailImage = thumbnailImage;
        this.runningTimeMin = runningTimeMin;
        this.genre = genre;
    }
}