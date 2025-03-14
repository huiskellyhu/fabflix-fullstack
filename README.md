# CS122B Project
### Author: Kelly Hu


## PROJECT 4
As of 3/2/25: Project 4 requirements completed.

Project 4 Demo Video: https://youtu.be/i-Rz48Qv59Y

Notes: Ports 8080 to the backend servers are closed to public. Demo video ONLY has pauses during AWS image creation and instance initialization. Blank desktop may appear when I'm switching to putty terminals.

- ### Connection Pooling
    - #### Include the filename/path of all code/configuration files in GitHub of using JDBC Connection Pooling.
      - [WebContent/META-INF/context.xml](WebContent/META-INF/context.xml)
      - [src/AddMovieServlet.java](src/AddMovieServlet.java)
      - [src/AddStarServlet.java](src/AddStarServlet.java)
      - [src/AddToCartServlet.java](src/AddToCartServlet.java)
      - [src/ConfirmationServlet.java](src/ConfirmationServlet.java)
      - [src/DashboardLoginServlet.java](src/DashboardLoginServlet.java)
      - [src/GenresServlet.java](src/GenresServlet.java)
      - [src/LoginServlet.java](src/LoginServlet.java)
      - [src/MetadataServlet.java](src/MetadataServlet.java)
      - [src/MovieListServlet.java](src/MovieListServlet.java)
      - [src/PaymentServlet.java](src/PaymentServlet.java)
      - [src/PlaceOrderServlet.java](src/PlaceOrderServlet.java)
      - [src/ResultsServlet.java](src/ResultsServlet.java)
      - [src/ShoppingCartServlet.java](src/ShoppingCartServlet.java)
      - [src/SingleMovieServlet.java](src/SingleMovieServlet.java)
      - [src/SingleStarServlet.java](src/SingleStarServlet.java)
    
     Note that password encrypting files aren't included (Didn't change them since they're in P3).

    - #### Explain how Connection Pooling is utilized in the Fabflix code.
      Connection pooling is used to manage database connections more efficiently by reusing a pool of pre-established connections instead of connecting and closing connections for each request. Given that this application uses a lot of queries like for searching, each query would open and close a connection for each request. With connection pooling, when a query is executed, a connection from the pool is used rather than creating a new one. Then, when that connection is "closed," it returns to the pool. This is configured in the context.xml file in the "url" field of each datasource.
  
    - #### Explain how Connection Pooling works with two backend SQL.
      With two backend SQL, connection pooling is consistent with each backend server. In our case, there is a master and a slave database. With a request's connection, it can connect to either the master or the slave, but the database will still remain consistent
  between the two servers. Queries will get distributed between the two and be more efficient (depending on master/slave, talked about below).

- ### Master/Slave
    - #### Include the filename/path of all code/configuration files in GitHub of routing queries to Master/Slave SQL.
      - [WebContent/META-INF/context.xml](WebContent/META-INF/context.xml)
      - [src/AddMovieServlet.java](src/AddMovieServlet.java)
      - [src/AddStarServlet.java](src/AddStarServlet.java)
      - [src/AddToCartServlet.java](src/AddToCartServlet.java)
      - [src/PlaceOrderServlet.java](src/PlaceOrderServlet.java)
     
        Note that password encrypting files aren't included (Didn't change them since they're in P3).
    - #### How read/write requests were routed to Master/Slave SQL?
      Read/write requests were routed to Master/Slave SQL by setting the datasource for servlets to master if they had to update the database in any way. These servlets are noted above. Reading from the database was allowed for either the master or slave, but writing was only for master.

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
