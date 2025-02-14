USE moviedb;

DROP procedure IF EXISTS add_movie;
-- Change DELIMITER to $$
DELIMITER $$

CREATE PROCEDURE add_movie (
    IN movie_title VARCHAR(100),
    IN movie_year INT,
    IN movie_director VARCHAR(100),
    IN star_name VARCHAR(100),
    IN genre_name VARCHAR(32)
-- FLOOR(1+RAND() * 100) for price?
)
BEGIN
-- dupe movie

-- new star

-- new genre