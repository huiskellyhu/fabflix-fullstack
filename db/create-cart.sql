CREATE TABLE cart_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    movie_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (movie_id) REFERENCES movies(id)
);

