package com.example.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Genre {
    ACTION("액션", "ACTION"),
    ROMANCE("로맨스", "ROMANCE"),
    HORROR("호러", "HORROR"),
    SF("SF", "SF"),
    DRAMA("드라마", "DRAMA"),
    COMEDY("코미디", "COMEDY"),
    THRILLER("스릴러", "THRILLER"),
    FANTASY("판타지", "FANTASY"),
    MYSTERY("미스터리", "MYSTERY"),
    ADVENTURE("어드벤처", "ADVENTURE"),
    CRIME("범죄", "CRIME"),
    ANIMATION("애니메이션", "ANIMATION");

    private final String value;
    private final String code;
}
