ALTER TABLE movies
ADD COLUMN price INT NOT NULL DEFAULT 0;

UPDATE movies
SET price = FLOOR(1+RAND() * 100);
-- only want $1-$100, no decimals