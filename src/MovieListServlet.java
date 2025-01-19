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
import java.sql.ResultSet;
import java.sql.Statement;





@WebServlet(name = "MovieListServlet", urlPatterns = "/api/movielist")
public class MovieListServlet extends HttpServlet {
    // Create a dataSource which registered in web.xml
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb?useUnicode=true&characterEncoding=UTF-8");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {

            // Declare our statement
            Statement statement = conn.createStatement();

            // FIRST ITERATION OF QUERY... should be slow. try to get first 20 movies first.
            // test in mysql workbench
//            String query = "SELECT m.id, m.title, m.year, m.director, " +
//                    "GROUP_CONCAT(g.name) AS genres, " +
//                    "GROUP_CONCAT(s.name) AS stars, " +
//                    "r.rating " +
//                    "FROM movies AS m " +
//                    "LEFT JOIN stars_in_movies AS sim on sim.movieId = m.id " +
//                    "LEFT JOIN stars AS s ON s.id = sim.starId " +
//
//                    "LEFT JOIN genres_in_movies AS gim ON gim.movieId = m.id " +
//                    "LEFT JOIN genres AS g ON g.id = gim.genreId " +
//
//                    "LEFT JOIN ratings AS r ON r.movieId = m.id " +
//
//                    "GROUP BY m.id " +
//                    "ORDER BY r.rating DESC " +
//                    "LIMIT 20;";

            String query = "WITH top_20_movies AS ( " +
                            "SELECT m.id, m.title, m.year, m.director, r.rating FROM movies m " +
                            "LEFT JOIN ratings AS r ON r.movieId = m.id " +
                            "ORDER BY r.rating DESC " +
                            "LIMIT 20 )" +

                            "SELECT t20.id, t20.title, t20.year, t20.director, " +
                            "GROUP_CONCAT(DISTINCT g.name) AS genres, " +
                            "GROUP_CONCAT(DISTINCT s.name ORDER BY sim.starId) AS stars, " +
                            "t20.rating " +
                            "FROM top_20_movies AS t20 " +
                            "LEFT JOIN genres_in_movies AS gim ON gim.movieId = t20.id " +
                            "LEFT JOIN genres AS g ON g.id = gim.genreId " +
                            "LEFT JOIN stars_in_movies AS sim ON sim.movieId = t20.id " +
                            "LEFT JOIN stars AS s ON s.id = sim.starId " +
                            "GROUP BY t20.id " +
                            "ORDER BY t20.rating DESC;";

            // Perform the query
            ResultSet rs = statement.executeQuery(query);

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

            // Log to localhost log
            request.getServletContext().log("getting " + jsonArray.size() + " results");

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {

            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }
}
