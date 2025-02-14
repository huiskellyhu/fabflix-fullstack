use moviedb;
Create index for movieId in the ratings table
CREATE INDEX idx_movieId_ratings ON ratings(movieId);

Create index for movieId in the genres_in_movies table
CREATE INDEX idx_movieId_genres_in_movies ON genres_in_movies(movieId);

Create index for genreId in the genres_in_movies table
CREATE INDEX idx_genreId_genres_in_movies ON genres_in_movies(genreId);

Create index for movieId in the stars_in_movies table
CREATE INDEX idx_movieId_stars_in_movies ON stars_in_movies(movieId);

Create index for starId in the stars_in_movies table
CREATE INDEX idx_starId_stars_in_movies ON stars_in_movies(starId);