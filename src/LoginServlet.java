import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
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

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {

            // See if username and password are in database
            String query = "SELECT password FROM customers WHERE email = ?";

            PreparedStatement statement = conn.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, username);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonObject responseJsonObject = new JsonObject();

            // Iterate through each row of rs
            if (rs.next()) {
                // Login success (username found):

                // set this user into the session
                request.getSession().setAttribute("user", new User(username));

                if (rs.getString("password").equals(password)) {
                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "success");
                } else {
                    // Login fail (password is incorrect)
                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", "password is incorrect");
                }

            } else {
                // Login fail (username not found)
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "username not found");
                // Log to localhost log
                request.getServletContext().log("Login failed");
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

        /* This example only allows username/password to be test/test
        /  in the real project, you should talk to the database to verify username/password
        */
//        JsonObject responseJsonObject = new JsonObject();
//        if (username.equals("anteater") && password.equals("123456")) {
//            // Login success:
//
//            // set this user into the session
//            request.getSession().setAttribute("user", new User(username));
//
//            responseJsonObject.addProperty("status", "success");
//            responseJsonObject.addProperty("message", "success");
//
//        } else {
//            // Login fail
//            responseJsonObject.addProperty("status", "fail");
//            // Log to localhost log
//            request.getServletContext().log("Login failed");
//            // sample error messages. in practice, it is not a good idea to tell user which one is incorrect/not exist.
//            if (!username.equals("anteater")) {
//                responseJsonObject.addProperty("message", "user " + username + " doesn't exist");
//            } else {
//                responseJsonObject.addProperty("message", "incorrect password");
//            }
//        }
//        response.getWriter().write(responseJsonObject.toString());
    }
}
