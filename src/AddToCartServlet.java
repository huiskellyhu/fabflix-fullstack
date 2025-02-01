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
@WebServlet(name = "AddToCartServlet", urlPatterns = "/api/addtocart")
public class AddToCartServlet extends HttpServlet {
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
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        User current_user = (User) session.getAttribute("user");
        Integer customer_id = current_user.getId();

        String movie_id = request.getParameter("movie_id");


        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            // check if user + movie is already in the cart_items table
            String query = "SELECT id, quantity FROM cart_items WHERE customer_id = ? AND movie_id = ?";
            PreparedStatement statement = conn.prepareStatement(query);

            statement.setInt(1, customer_id);
            statement.setString(2, movie_id);

            ResultSet rs = statement.executeQuery();

            // if yes -> update quantity
            if (rs.next()){
                String update_query = "UPDATE cart_items SET quantity = ? WHERE id = ?";
                PreparedStatement update_statement = conn.prepareStatement(update_query);

                update_statement.setInt(1, rs.getInt("quantity") + 1);
                update_statement.setInt(2, rs.getInt("id"));
                update_statement.executeUpdate();
                update_statement.close();
            } else {
                // else -> add new row to cart_items table
                String insert_query = "INSERT INTO cart_items (customer_id, movie_id, quantity) VALUES (?, ?, ?)";
                PreparedStatement insert_statement = conn.prepareStatement(insert_query);
                insert_statement.setInt(1, customer_id);
                insert_statement.setString(2, movie_id);
                insert_statement.setInt(3, 1);
                insert_statement.executeUpdate();
                insert_statement.close();
            }

            rs.close();
            statement.close();
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