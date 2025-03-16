package movies;

import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import login.User;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "movies.PaymentServlet", urlPatterns = "/api/payment")
public class PaymentServlet extends HttpServlet {
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

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String first_name = request.getParameter("first_name");
        String last_name = request.getParameter("last_name");
        String card_num = request.getParameter("card_num");
        String expiration_date = request.getParameter("expiration_date");

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {

            // See if username and password are in database
            String query = "SELECT * FROM creditcards WHERE id = ?";

            PreparedStatement statement = conn.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, card_num);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonObject responseJsonObject = new JsonObject();

            // Iterate through each row of rs
            if (rs.next()) {
                // credit card num found

                // make sure that session's customer id matches the credit card id of the current customer id
                HttpSession session = request.getSession();
                User current_user = (User) session.getAttribute("user");
                Integer customer_id = current_user.getId();

                String correct_card_query = "SELECT ccId FROM customers WHERE id = ?";
                PreparedStatement cc_statement = conn.prepareStatement(correct_card_query);
                cc_statement.setString(1, customer_id.toString());
                ResultSet rs_cc = cc_statement.executeQuery();
                if (rs_cc.next()) {
                    if(rs_cc.getString("ccId").equals(card_num)) {
                        // customer id and cc id match
                        if (rs.getString("firstName").equals(first_name)) {
                            if (rs.getString("lastName").equals(last_name)) {
                                if (rs.getString("expiration").equals(expiration_date)) {
                                    responseJsonObject.addProperty("status", "success");
                                    responseJsonObject.addProperty("message", "success");
                                } else {
                                    responseJsonObject.addProperty("status", "fail");
                                    responseJsonObject.addProperty("message", "Expiration date is incorrect.");
                                }
                            } else {
                                responseJsonObject.addProperty("status", "fail");
                                responseJsonObject.addProperty("message", "Last Name is incorrect.");
                            }

                        } else {
                            // payment fail (first_name is incorrect)
                            responseJsonObject.addProperty("status", "fail");
                            responseJsonObject.addProperty("message", "First Name is incorrect.");
                        }
                    } else {
                        // customer id and cc id do not match
                        responseJsonObject.addProperty("status", "fail");
                        responseJsonObject.addProperty("message", "Credit Card does not match Customer.");
                    }
                } else {
                    // no result found from cc_query
                }

            } else {
                // Payment fail (card_num not found)
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Card number not found.");
                // Log to localhost log
                request.getServletContext().log("Payment failed");
            }
            rs.close();
            statement.close();


            // Write JSON string to output
            out.write(responseJsonObject.toString());
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
    }
}
