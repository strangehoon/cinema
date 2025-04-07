-- THEATERS (100 rows)
INSERT INTO theaters (name, created_at, created_by, updated_at, updated_by)
SELECT
    CONCAT('Theater_', n),
    NOW(), 1, NOW(), 1
FROM (
    SELECT @n := @n + 1 as n
    FROM (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5) t1,
         (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5) t2,
         (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4) t3,
         (SELECT @n := 0) tmp
) t
LIMIT 100;

-- MOVIES (10000 rows)
INSERT INTO movies (title, rating, released_date, thumbnail_image, running_time_min, genre, created_at, created_by, updated_at, updated_by)
SELECT
    CONCAT('Movie_', n),
    CASE FLOOR(RAND() * 3)
        WHEN 0 THEN 'R_12'
        WHEN 1 THEN 'R_15'
        ELSE 'R_19'
    END,
    CAST(DATE_ADD('2010-01-01', INTERVAL FLOOR(RAND() * 5843) DAY) AS DATE),
    CONCAT('movie_', n, '.jpg'),
    FLOOR(RAND() * 60 + 90),
    CASE FLOOR(RAND() * 12)
        WHEN 0 THEN 'ACTION'
        WHEN 1 THEN 'DRAMA'
        WHEN 2 THEN 'SF'
        WHEN 3 THEN 'HORROR'
        WHEN 4 THEN 'ROMANCE'
        WHEN 5 THEN 'COMEDY'
        WHEN 6 THEN 'THRILLER'
        WHEN 7 THEN 'FANTASY'
        WHEN 8 THEN 'MYSTERY'
        WHEN 9 THEN 'ADVENTURE'
        WHEN 10 THEN 'CRIME'
        ELSE 'ANIMATION'
    END,
    NOW(), 1, NOW(), 1
FROM (
    SELECT @n := @n + 1 as n
    FROM (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) t1,
         (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) t2,
         (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) t3,
         (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) t4,
         (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) t5,
         (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) t6,
         (SELECT @n := 0) tmp
) t
LIMIT 10000;


INSERT INTO screenings (date, started_at, ended_at, movie_id, theater_id, created_at, created_by, updated_at, updated_by)
SELECT
    DATE_ADD(m.released_date, INTERVAL FLOOR(RAND(m.id + s.n) * 100) DAY),
    TIMESTAMP(DATE_ADD(m.released_date, INTERVAL FLOOR(RAND(m.id + s.n) * 100) DAY), SEC_TO_TIME(rand_sec)),
    TIMESTAMP(DATE_ADD(m.released_date, INTERVAL FLOOR(RAND(m.id + s.n) * 100) DAY), SEC_TO_TIME(LEAST(rand_sec + duration_sec, 86399))),
    m.id,
    FLOOR(RAND(m.id + s.n) * 100) + 1,
    NOW(), 1, NOW(), 1
FROM movies m
JOIN (
    SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
) s -- 🎯 각 영화당 5개의 상영 정보
JOIN (
    SELECT
        @row := @row + 1,
        FLOOR(RAND(@row) * 79200) AS rand_sec,
        FLOOR(RAND(@row + 1) * 10800) + 1 AS duration_sec
    FROM (SELECT 1 FROM dual LIMIT 10000) rand_gen, (SELECT @row := 0) r
) r_gen
LIMIT 50000;


-- SCREENING_SEATS (100 theaters * 25 seats = 2500 rows)
INSERT INTO screening_seats (seat_row, seat_col, theater_id, created_at, created_by, updated_at, updated_by)
SELECT
    r.r + 1 AS seat_row,
    c.c + 1 AS seat_col,
    t.id,
    NOW(), 1, NOW(), 1
FROM theaters t
JOIN (
    SELECT 0 AS c UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
) AS c
JOIN (
    SELECT 0 AS r UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
) AS r
ORDER BY t.id, seat_row, seat_col;


-- USERS (100 rows)
INSERT INTO users (created_at, created_by, updated_at, updated_by)
SELECT NOW(), 1, NOW(), 1
FROM (
    SELECT a.N + b.N * 10 + 1 AS num
    FROM (
        SELECT 0 AS N UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
        UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
    ) a
    CROSS JOIN (
        SELECT 0 AS N UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
        UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
    ) b
    ORDER BY num
    LIMIT 100
) AS numbers;