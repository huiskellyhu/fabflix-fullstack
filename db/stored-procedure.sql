USE moviedb;
-- REFERENCES: https://www.geeksforgeeks.org/what-is-stored-procedures-in-sql/
-- https://stackoverflow.com/questions/6260157/mysql-how-to-quit-exit-from-stored-procedure

DROP procedure IF EXISTS add_movie;
-- Change DELIMITER to $$
DELIMITER $$

CREATE PROCEDURE add_movie (
    IN movie_title VARCHAR(100),
    IN movie_year INT,
    IN movie_director VARCHAR(100),
    IN star_name VARCHAR(100),
    IN birth_year INT,
    IN genre_name VARCHAR(32)
-- FLOOR(1+RAND() * 100) for price?
)
proc: BEGIN
    DECLARE movie_id VARCHAR(10);
    DECLARE star_id VARCHAR(10);
    DECLARE genre_id INT;

-- dupe movie?
    SELECT id INTO movie_id FROM movies WHERE title = movie_title AND year = movie_year AND director = movie_director LIMIT 1;
    IF movie_id IS NOT NULL THEN
        SELECT 'Movie is already in database.' AS status_message;
        LEAVE proc;
    END IF;
-- new movie
    SELECT CONCAT('tt', (SELECT max(substring(id, 3)) FROM movies) + 1) INTO movie_id;
    INSERT INTO movies (id, title, year, director, price) VALUES (movie_id, movie_title, movie_year, movie_director, FLOOR(1+RAND() * 100));

-- new star or exists
    SELECT id INTO star_id FROM stars WHERE name = star_name LIMIT 1;
    IF star_id IS NULL THEN
        SELECT CONCAT('nm', (SELECT max(substring(id, 3)) FROM stars) + 1) INTO star_id;
        INSERT INTO stars (id, name, birthYear) VALUES (star_id, star_name, birth_year);
    END IF;

-- modify stars_in_movies
    INSERT INTO stars_in_movies (starId, movieId) VALUES (star_id, movie_id);

-- new genre or exists
    SELECT id INTO genre_id FROM genres WHERE name = genre_name LIMIT 1;
    IF genre_id IS NULL THEN
       (SELECT max(id)+1 FROM genres) INTO genre_id;
        INSERT INTO genres (id, name) VALUES (genre_id, genre_name);

    END IF;
-- modify genres_in_movies
    INSERT INTO genres_in_movies (genreId, movieId) VALUES (genre_id, movie_id);

-- set status message
    SELECT CONCAT('Movie successfully added: movieId=',movie_id,', starId=',star_id,', genreId=',genre_id) AS status_message;

END $$
DELIMITER ;