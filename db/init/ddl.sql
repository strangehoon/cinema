CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at DATETIME,
    created_by BIGINT,
    updated_at DATETIME,
    updated_by BIGINT
);

CREATE TABLE theaters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(30),
    created_at DATETIME,
    created_by BIGINT,
    updated_at DATETIME,
    updated_by BIGINT
);

CREATE TABLE movies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(30),
    rating VARCHAR(20),
    released_at DATETIME,
    thumbnail_image VARCHAR(50),
    running_time INT,
    genre VARCHAR(20),
    created_at DATETIME,
    created_by BIGINT,
    updated_at DATETIME,
    updated_by BIGINT
);

CREATE TABLE screenings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    date DATE,
    started_at DATETIME,
    ended_at DATETIME,
    movie_id BIGINT NOT NULL,
    theater_id BIGINT NOT NULL,
    created_at DATETIME,
    created_by BIGINT,
    updated_at DATETIME,
    updated_by BIGINT
);

CREATE TABLE screening_seats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    `row` int,
    `col` int,
    theater_id BIGINT NOT NULL,
    created_at DATETIME,
    created_by BIGINT,
    updated_at DATETIME,
    updated_by BIGINT
);

CREATE TABLE reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    screening_seat_id BIGINT NOT NULL,
    screening_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME,
    created_by BIGINT,
    updated_at DATETIME,
    updated_by BIGINT
);


CREATE INDEX idx_screening_movie_theater ON screenings(movie_id, theater_id);
CREATE INDEX idx_movie_genre ON movies(genre);
CREATE FULLTEXT INDEX idx_movie_title ON movies(title);