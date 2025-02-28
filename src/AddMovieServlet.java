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
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "AddMovieServlet", urlPatterns = "/_dashboard/api/addmovie")
public class AddMovieServlet extends HttpServlet {
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

    public String getServletInfo() {
        return "Servlet connects to MySQL database and displays result of a SELECT";
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String title = request.getParameter("title"); // required
        String year = request.getParameter("year"); // required
        String director = request.getParameter("director"); // required
        String star_name = request.getParameter("star_name"); // required
        String birth_year = request.getParameter("birth_year"); // not required
        String genre_name = request.getParameter("genre_name"); // required

        System.out.println("birth_year: " + birth_year);
        if (birth_year != null && birth_year.equals("")) {
            birth_year = null;
        }
        System.out.println("genre_name: " + genre_name);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        JsonObject responseJsonObject = new JsonObject();

        // don't need to validate fields, just made them required in the form
        // if going to /api/addmovie then need to add this

        try (Connection conn = dataSource.getConnection()) {
            // call stored procedure
            String call_proc = "CALL add_movie(?,?,?,?,?,?)";

            CallableStatement call_proc_statement = conn.prepareCall(call_proc);
            call_proc_statement.setString(1, title);
            call_proc_statement.setInt(2, Integer.parseInt(year));
            call_proc_statement.setString(3, director);
            call_proc_statement.setString(4, star_name);
            if(birth_year != null){
                System.out.println("birth_year2: " + birth_year);
                call_proc_statement.setInt(5, Integer.parseInt(birth_year));
            } else {
                call_proc_statement.setString(5, null);
            }
            call_proc_statement.setString(6, genre_name);

            boolean temp = call_proc_statement.execute();

            if (temp){
                ResultSet rs = call_proc_statement.getResultSet();
                if (rs.next()){
                    String resultMessage = rs.getString("status_message");
                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", resultMessage);
                }
            } else {
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Error.");
            }

            out.write(responseJsonObject.toString());
            response.setStatus(200);


        } catch (Exception e) {
            // Write error message JSON object to output
            //JsonObject jsonObject = new JsonObject();
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "Failed to add movie.");
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