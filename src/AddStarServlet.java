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
@WebServlet(name = "AddStarServlet", urlPatterns = "/_dashboard/api/addstar")
public class AddStarServlet extends HttpServlet {
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String star_name = request.getParameter("star_name");
        String birth_year = request.getParameter("birth_year");

        System.out.println("Received star_name: " + star_name);
        System.out.println("Received birth_year: " + birth_year);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        JsonObject responseJsonObject = new JsonObject();
        if(star_name == null || star_name.isEmpty()){
            // star name is not optional, birth year is
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "Please enter a star name.");
            // Write JSON string to output
            out.write(responseJsonObject.toString());
            // Set response status to 200 (OK)
            out.close();
            response.setStatus(200);
            return;
        }


        try (Connection conn = dataSource.getConnection()) {
            String max_id_query = "SELECT max(substring(id, 3)) as max_id FROM stars";
            PreparedStatement max_id_statement = conn.prepareStatement(max_id_query);

            ResultSet max_id_result = max_id_statement.executeQuery();
            max_id_result.next();
            System.out.println("max_id:" + max_id_result.getString("max_id"));
            int max_id = Integer.parseInt(max_id_result.getString("max_id")) + 1;
            String new_id = "nm" + max_id;

            String insert_query = "INSERT INTO stars (id, name, birthYear) VALUES (?, ?, ?)";
            PreparedStatement insert_statement = conn.prepareStatement(insert_query);
            insert_statement.setString(1, new_id);
            insert_statement.setString(2, star_name);
            if(birth_year != null){
                insert_statement.setInt(3, Integer.parseInt(birth_year));
            } else {
                insert_statement.setString(3, null);
            }

            int rs = insert_statement.executeUpdate();

            max_id_result.close();
            max_id_statement.close();
            insert_statement.close();
            responseJsonObject.addProperty("status", "success");
            responseJsonObject.addProperty("message", "Successfully added star: " + new_id);
            out.write(responseJsonObject.toString());
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            //JsonObject jsonObject = new JsonObject();
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "Failed to add star.");
            out.write(responseJsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }


    }
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }


}