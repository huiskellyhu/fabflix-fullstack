package movies;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import common.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import common.User;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "movies.ConfirmationServlet", urlPatterns = "/api/confirmation")
public class ConfirmationServlet extends HttpServlet {
    // Create a dataSource which registered in web.xml
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    public String getServletInfo() {
        return "Servlet connects to MySQL database and displays result of a SELECT";
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // get current customer
        HttpSession session = request.getSession();
//        User current_user = (User) session.getAttribute("user");
//        Integer customer_id = current_user.getId();
        String token = JwtUtil.getCookieValue(request, "jwtToken");
        Claims claims = JwtUtil.validateToken(token);
        Integer customer_id = Integer.valueOf(claims.getSubject());

        response.setContentType("application/json");

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // only get current customer's most recent n sales (session.getAttribute("recent_sales_num")
        int num_recent = (Integer) session.getAttribute("recent_sales_num");

        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT s.id, s.movieId, m.title, m.price, s.quantity " +
                    "FROM sales AS s " +
                    "JOIN movies AS m ON s.movieId = m.id " +
                    "WHERE customerId = ? " +
                    "ORDER BY s.id DESC LIMIT ?";

            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, customer_id);
            statement.setInt(2, num_recent);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                String sales_id = rs.getString("id");
                String movie_id = rs.getString("movieId");
                String movie_title = rs.getString("title");
                String sales_quantity = rs.getString("quantity");
                String movie_price = rs.getString("price");

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("sales_id", sales_id);
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("sales_quantity", sales_quantity);
                jsonObject.addProperty("movie_price", movie_price);

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

    }

}