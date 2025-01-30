import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String genreId = request.getParameter("genre");
        String prefixId = request.getParameter("prefix");
        String title = request.getParameter("title");
        String year = request.getParameter("year");
        String director = request.getParameter("director");
        String star = request.getParameter("star");

        // The log message can be found in localhost log
        request.getServletContext().log("getting genreId: " + genreId);
        request.getServletContext().log("getting prefixId: " + prefixId);
        request.getServletContext().log("getting title: " + title);
        request.getServletContext().log("getting year: " + year);
        request.getServletContext().log("getting director: " + director);
        request.getServletContext().log("getting star: " + star);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            String query = "SELECT m.id, m.title, m.year, m.director, " +
                    "GROUP_CONCAT(DISTINCT CONCAT(g.id, ':', g.name) ORDER BY g.name) AS genres, " +
                    "GROUP_CONCAT(DISTINCT CONCAT(s.id, ':', s.name) ORDER BY (SELECT COUNT(*) FROM stars_in_movies as sim2 WHERE sim2.starId = s.id) DESC) AS stars, " +
                    "r.rating " +
                    "FROM movies AS m " +
                    "LEFT JOIN ratings AS r ON r.movieId = m.id " +
                    "LEFT JOIN genres_in_movies AS gim ON gim.movieId = m.id " +
                    "LEFT JOIN genres AS g ON g.id = gim.genreId " +
                    "LEFT JOIN stars_in_movies AS sim ON sim.movieId = m.id " +
                    "LEFT JOIN stars AS s ON s.id = sim.starId ";
            if (genreId != null) {
                // GENRE QUERY
                query += "WHERE m.id IN (" +
                        "    SELECT movieId " +
                        "    FROM genres_in_movies " +
                        "    WHERE genreId = ?) ";

            } else if (prefixId != null) {
                // PREFIX QUERY
                if (prefixId.equals("*")) {
                    // non-alphanumeric chars
                    query += "WHERE m.title REGEXP '[^a-zA-Z0-9]' ";
                }
                else {
                    query += "WHERE m.title LIKE ? ";
                }
            } else {
                query += "WHERE 1=1 "; // to use AND

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
                    query += "AND m.id IN ( " +
                             "SELECT sim.movieId FROM stars_in_movies as sim " +
                             "JOIN stars as s ON sim.starId = s.id WHERE sim.starId = s.id AND s.name LIKE ?) ";
                }
            }
            query += "GROUP BY m.id " +
                    "ORDER BY m.title ASC " +
                    "LIMIT 20;";
            // Construct a query with parameter represented by "?"
//            String query = "SELECT * from stars as s, stars_in_movies as sim, movies as m " +
//                    "where m.id = sim.movieId and sim.starId = s.id and s.id = ?";

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

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);
                jsonObject.addProperty("movie_genres", movie_genres);
                jsonObject.addProperty("movie_stars", movie_stars);
                jsonObject.addProperty("movie_rating", movie_rating);


                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

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
