package movies;

import com.google.gson.JsonObject;
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

@WebServlet(name = "movies.PlaceOrderServlet", urlPatterns = "/api/placeorder")
public class PlaceOrderServlet extends HttpServlet {
    // Create a dataSource which registered in web.xml
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/master");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    public String getServletInfo() {
        return "Servlet connects to MySQL database and displays result of a SELECT";
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // get current customer
        HttpSession session = request.getSession();
        User current_user = (User) session.getAttribute("user");
        Integer customer_id = current_user.getId();

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();


        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            // add to sales and remove from cart_items
            // sales table: id, customerId, movieId, saleDate, quantity
            // cart_items: remove all rows where customerId = customer_id
            String insert_sales_query = "INSERT INTO sales (customerId, movieId, saleDate, quantity) " +
                    "SELECT customer_id, movie_id, CURDATE(), quantity FROM cart_items WHERE customer_id = ?";

            PreparedStatement sales_statement = conn.prepareStatement(insert_sales_query);
            sales_statement.setInt(1, customer_id);

            int rs_sales = sales_statement.executeUpdate();
            System.out.println("added to sales");

            // also save sales to session for confirmation page
            int num_items = 0;
            String get_items_query = "SELECT COUNT(*) FROM cart_items WHERE customer_id = ?";
            PreparedStatement get_items_statement = conn.prepareStatement(get_items_query);
            get_items_statement.setInt(1, customer_id);
            ResultSet rs = get_items_statement.executeQuery();
            if (rs.next()){
                num_items = rs.getInt(1);
            }
            session.setAttribute("recent_sales_num", num_items);
            System.out.println("saved recent sales_num: " + num_items);


            // removing from cart_items
            String delete_query = "DELETE FROM cart_items WHERE customer_id = ?";
            PreparedStatement delete_statement = conn.prepareStatement(delete_query);
            delete_statement.setInt(1, customer_id);
            delete_statement.executeUpdate();
            System.out.println("deleted from cart_items");


            conn.commit();

            sales_statement.close();
            delete_statement.close();
            response.setStatus(200);
            conn.setAutoCommit(true);
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