import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "ResultsServlet", urlPatterns = "/api/results")
public class ResultsServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
            System.out.println("result servlet established");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();

        response.setContentType("application/json"); // Response mime type
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        // Retrieve parameter id from url request.
        String genreId = request.getParameter("genre");
        String prefixId = request.getParameter("prefix");
        String title = request.getParameter("title");
        String year = request.getParameter("year");
        String director = request.getParameter("director");
        String star = request.getParameter("star");
        String sort = request.getParameter("sort");
        String limit = request.getParameter("limit");
        String page = request.getParameter("page");
        String returning = request.getParameter("returning");



        // The log message can be found in localhost log
        request.getServletContext().log("getting genreId: " + genreId);
        request.getServletContext().log("getting prefixId: " + prefixId);
        request.getServletContext().log("getting title: " + title);
        request.getServletContext().log("getting year: " + year);
        request.getServletContext().log("getting director: " + director);
        request.getServletContext().log("getting star: " + star);
        request.getServletContext().log("getting sort: " + sort);
        request.getServletContext().log("getting limit: " + limit);
        request.getServletContext().log("getting page: " + page);
        request.getServletContext().log("getting returning: " + returning);

//        System.out.println("Received Sort: " + sort);
//        System.out.println("Received Limit: " + limit);
//        System.out.println("Received Page: " + page);
//        System.out.println("Received returning: " + returning);
        System.out.println("Received genreId: " + genreId);

        // setting default values
        if (sort == null){
            sort = "title_asc_rating_asc";
        }
        if (limit == null){
            limit = "25";
        }


        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        if(returning != null && returning.equals("1")){
            // already convert to string
            String jsonArray = session.getAttribute("results").toString();

            // Write JSON string to output
            out.write(jsonArray);
            // Set response status to 200 (OK)
            response.setStatus(200);
            System.out.println("got result from cache");
            return;
        }

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            // OLD QUERY BELOW THIS ---
            // old query really fast for small results, new query more good in general but slow for small results
            // IMPROVEMENTS MADE FOR P3:
            //      SELECT FOR GENRES FIRST INSTEAD OF NESTED SUBQUERIES
            //      MAKE A DYNAMIC COLUMN FOR movie_count FOR EACH STAR INSTEAD OF NESTED SUBQUERY FOR ORDERING
            //              (a lot of overhead when querying for stars i think)
            // test in mysql workbench
            // used some tips from this https://www.geeksforgeeks.org/best-practices-for-sql-query-optimizations/


            String query = "SELECT m.id, m.title, m.year, m.director, g2.genres, s2.stars, r.rating, COUNT(*) OVER() AS total_results FROM movies AS m " +
                            "LEFT JOIN ratings AS r ON r.movieId = m.id " +
                            "INNER JOIN ( " +
                            "      SELECT gm.movieId, " +
                            "      GROUP_CONCAT(DISTINCT CONCAT(g.id, ':', g.name) ORDER BY g.name SEPARATOR ', ') AS genres " +
                            "      FROM genres_in_movies gm " +
                            "      INNER JOIN genres g ON g.id = gm.genreId " +
                            "      GROUP BY gm.movieId " +
                            " ) AS g2 ON g2.movieId = m.id " +
                            "INNER JOIN ( " +
                            "       SELECT sm.movieId, " +
                            "       GROUP_CONCAT(DISTINCT CONCAT(s.id, ':', s.name) ORDER BY s.movie_count DESC, s.name ASC SEPARATOR ', ') AS stars FROM stars_in_movies sm  " +
                            "INNER JOIN ( " +
                            "       SELECT s.id, s.name, COUNT(sim.movieId) AS movie_count FROM stars s " +
                            "       INNER JOIN stars_in_movies sim ON s.id = sim.starId " +
                            "       GROUP BY s.id " +
                            " ) AS s ON s.id = sm.starId " +
                            "GROUP BY sm.movieId " +
                            " ) AS s2 ON s2.movieId = m.id " +
                            "INNER JOIN genres_in_movies gim ON gim.movieId = m.id " +
                            "WHERE 1=1 ";

//            String query = "SELECT m.id, m.title, m.year, m.director, " +
//                    "GROUP_CONCAT(DISTINCT CONCAT(g.id, ':', g.name) ORDER BY g.name) AS genres, " +
//                    "GROUP_CONCAT(DISTINCT CONCAT(s.id, ':', s.name) ORDER BY (SELECT COUNT(*) FROM stars_in_movies as sim2 WHERE sim2.starId = s.id) DESC, s.name ASC) AS stars, " +
//                    "r.rating, " +
//                    "COUNT(*) OVER() AS total_results " +
//                    "FROM movies AS m " +
//                    "LEFT JOIN ratings AS r ON r.movieId = m.id " +
//                    "INNER JOIN genres_in_movies AS gim ON gim.movieId = m.id " +
//                    "INNER JOIN genres AS g ON g.id = gim.genreId " +
//                    "INNER JOIN stars_in_movies AS sim ON sim.movieId = m.id " +
//                    "INNER JOIN stars AS s ON s.id = sim.starId ";

            if (genreId != null) {
                // GENRE QUERY
//                query += "WHERE m.id IN (" +
//                        "    SELECT movieId " +
//                        "    FROM genres_in_movies " +
//                        "    WHERE genreId = ?) ";
                query += "AND gim.genreId = ?";
//                query += "AND g2.genres LIKE ? ";

            } else if (prefixId != null) {
                // PREFIX QUERY
                if (prefixId.equals("*")) {
                    // non-alphanumeric chars
                    query += "AND m.title REGEXP '[^a-zA-Z0-9]' ";
                }
                else {
                    query += "AND m.title LIKE ? ";
                }
            } else {
//                query += "WHERE 1=1 "; // to use AND

                if (title != null && !title.isEmpty()) {
                    query += "AND m.title LIKE ? ";
                }
                if (year != null && !year.isEmpty()) {
                    query += "AND m.year = ? ";
                }
                if (director != null && !director.isEmpty()) {
                    query += "AND m.director LIKE ? ";
                }
                if (star != null && !star.isEmpty()) {
//                    query += "AND m.id IN ( " +
//                             "SELECT sim.movieId FROM stars_in_movies as sim " +
//                             "JOIN stars as s ON sim.starId = s.id WHERE sim.starId = s.id AND s.name LIKE ?) ";
                    query += "AND s2.stars LIKE ? ";
                }
            }


            query += "GROUP BY m.id ";

            if(sort == null || sort.equals("title_asc_rating_asc")) {
                query += "ORDER BY m.title ASC, COALESCE(r.rating, 0) ASC ";
            } else if (sort.equals("title_asc_rating_desc")) {
                query += "ORDER BY m.title ASC, COALESCE(r.rating, 0) DESC ";
            } else if (sort.equals("title_desc_rating_asc")) {
                query += "ORDER BY m.title DESC, COALESCE(r.rating, 0) ASC ";
            } else if (sort.equals("title_desc_rating_desc")) {
                query += "ORDER BY m.title DESC, COALESCE(r.rating, 0) DESC ";
            } else if (sort.equals("rating_asc_title_asc")) {
                query += "ORDER BY COALESCE(r.rating, 0) ASC, m.title ASC ";
            } else if (sort.equals("rating_asc_title_desc")) {
                query += "ORDER BY COALESCE(r.rating, 0) ASC, m.title DESC ";
            } else if (sort.equals("rating_desc_title_asc")) {
                query += "ORDER BY COALESCE(r.rating, 0) DESC, m.title ASC ";
            } else if (sort.equals("rating_desc_title_desc")) {
                query += "ORDER BY COALESCE(r.rating, 0) DESC, m.title DESC ";
            }

            if(limit == null) {
                query += "LIMIT 25 ";
            } else {
                query += "LIMIT " + limit + " ";
            }

            // pagination
            if(page == null) {
                query += "OFFSET 0;";
            } else {
                int limit_num = Integer.parseInt(limit);
                int offset_num = Integer.parseInt(page);
                int total_offset = (offset_num-1) * limit_num;
                query += "OFFSET " + total_offset + ";";
            }


            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            if (genreId != null) {
                statement.setString(1, genreId);
            } else if (prefixId != null) {
                if (!prefixId.equals("*")) {
                    statement.setString(1, prefixId + "%");
                }
            } else {
                int paramIndex = 1;
                if (title != null && !title.isEmpty()) {
                    statement.setString(paramIndex++, "%" + title + "%");
                }
                if (year != null && !year.isEmpty()) {
                    statement.setInt(paramIndex++, Integer.parseInt(year));
                }
                if (director != null && !director.isEmpty()) {
                    statement.setString(paramIndex++, "%" + director + "%");
                }
                if (star != null && !star.isEmpty()) {
                    statement.setString(paramIndex++, "%" + star + "%");
                }
            }

            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                String movie_id = rs.getString("id");
                String movie_title = rs.getString("title");
                String movie_year = rs.getString("year");
                String movie_director = rs.getString("director");
                String movie_genres = rs.getString("genres");
                String movie_stars = rs.getString("stars");
                String movie_rating = rs.getString("rating");
                String total_results = rs.getString("total_results");

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);
                jsonObject.addProperty("movie_genres", movie_genres);
                jsonObject.addProperty("movie_stars", movie_stars);
                jsonObject.addProperty("movie_rating", movie_rating);
                jsonObject.addProperty("total_results", total_results);

                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();
            // save JSON to session (convert back to string later)
            session.setAttribute("results", jsonArray);

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }

}
