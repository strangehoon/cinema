CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at DATETIME,
    created_by VARCHAR(255),
    updated_at DATETIME,
    updated_by VARCHAR(255)
);

CREATE TABLE theaters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    created_at DATETIME,
    created_by VARCHAR(255),
    updated_at DATETIME,
    updated_by VARCHAR(255)
);

CREATE TABLE movies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255),
    rating VARCHAR(10),
    release_date DATETIME,
    thumbnail_image VARCHAR(255),
    running_time INT,
    genre VARCHAR(20),
    created_at DATETIME,
    created_by VARCHAR(255),
    updated_at DATETIME,
    updated_by VARCHAR(255)
);

CREATE TABLE screenings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    date DATETIME,
    start_time DATETIME,
    end_time DATETIME,
    movie_id INT NOT NULL,
    theater_id INT NOT NULL,
    created_at DATETIME,
    created_by VARCHAR(255),
    updated_at DATETIME,
    updated_by VARCHAR(255)
);

CREATE TABLE seats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seat_number VARCHAR(255),
    user_id INT NOT NULL,
    screening_id INT NOT NULL,
    created_at DATETIME,
    created_by VARCHAR(255),
    updated_at DATETIME,
    updated_by VARCHAR(255)
);


CREATE INDEX idx_screening_movie_theater ON screenings(movie_id, theater_id);
CREATE INDEX idx_movie_genre ON movies(genre);
CREATE FULLTEXT INDEX idx_movie_title ON movies(title);