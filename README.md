# CS122B Project
### Author: Kelly Hu

## PROJECT 3
As of 2/16/25: Project 3 requirements completed.

Project 3 Demo Video: https://www.youtube.com/watch?v=70OwEigokEg

Notes: stored-procedure.sql is in folder 'db'. Only 2 commits after the demo video (readme update and inconsistent data reports).

### Files with Prepared Statements:
- AddStarServlet
- AddToCartServlet
- ConfirmationServlet
- DashboardLoginServlet
- LoginServlet
- PaymentServlet
- PlaceOrderServlet
- ResultsServlet
- ShoppingCartServlet
- SingleMovieServlet
- SingleStarServlet

Note:
- AddMovieServlet uses CallableStatement.
- VerifyEmployeePassword and VerifyPassword doesn't use user input so Statement suffices.
- GenresServlet doesn't use user input so Statement suffices.
- MovieListServlet doesn't use user input so Statement suffices.


### Parsing Time Optimization Strategies
1) Preloading data and storing into hashsets or hashmaps to reduce multiple queries for big tables like stars and movies.
2) Batch inserts for all insertions. (Note that INSERT IGNORE was used to ignore duplicate insertions for genres_in_movies and stars_in_movies)
   
Comparison: Naive approach for StarsParser took 4:36 mins. With 1) 2:39 mins. With 2) 2 seconds.

Inconsistent data reports from parsing can be found in folder 'ParsingFiles' (committed after updating this readme). The reports are transferred from the demo video's parsing.

Parsing System Output:
Inserted 6522 stars.
341 stars duplicate.
Inserted 12044 movies.
30 movies duplicate.
41 movies inconsistent.
Inserted 124 genres.
Inserted 9809 genres_in_movies.
Inserted 26763 stars_in_movies.
9202 movies not found.
15816 stars not found.


## PROJECT 2
As of 2/2/25: Project 2 requirements completed.

Project 2 Demo Video: https://www.youtube.com/watch?v=3-Bw_5iemmo

Substring matching design: All patterns used WHERE ... LIKE '%something%'. These patterns were just used for the SQL query for search bar's title, director, and star in the ResultsServlet.java.

## PROJECT 1
As of 1/19/25: Project 1 requirements completed.

Project 1 Demo Video: https://youtu.be/LJ_pqlnllLQ

NOTE TO GRADER: My create_table.sql is named as createtable.sql. And just a note about the visual, there is a menu for navigation because of the size of the window. If the window was bigger, the navbar looks like the example demo. All changes after the video's commit were just to fix the structure of the github and readme changes. (Changes done by @ 11:53PM 1/19/2025).
