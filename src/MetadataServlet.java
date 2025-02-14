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
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;


@WebServlet(name = "MetadataServlet", urlPatterns = "/_dashboard/api/dashboardmetadata")
public class MetadataServlet extends HttpServlet {
    // Create a dataSource which registered in web.xml
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        System.out.println("trying to initialize metadataservlet");
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
            System.out.println("MetadataServlet initialized!");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("getting metadata");
        response.setContentType("application/json"); // Response mime type
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");


        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            // reference: https://www.baeldung.com/jdbc-database-metadata
            // https://docs.oracle.com/javase/8/docs/api/java/sql/DatabaseMetaData.html
            DatabaseMetaData data = conn.getMetaData();

            // get all table names with show table

            // get metadata for each table name

            ResultSet tables = data.getTables("moviedb", null, "%", new String[]{"TABLE"});
            ArrayList<String> tableList = new ArrayList<>();

            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                tableList.add(tableName);
            }
            //tableList.add("sys_config");

            JsonArray jsonArray = new JsonArray();

            for (String tableName : tableList) {
                ResultSet rs = data.getColumns("moviedb", null, tableName, "%");

                while (rs.next()) {
                    String table = rs.getString("TABLE_NAME");
                    String column = rs.getString("COLUMN_NAME");
                    String type = rs.getString("TYPE_NAME");
                    String size = rs.getString("COLUMN_SIZE");

                    // Create a JsonObject based on the data we retrieve from rs
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("table", table);
                    jsonObject.addProperty("column", column);
                    jsonObject.addProperty("type", type);
                    jsonObject.addProperty("size", size);
                    System.out.println(table + "  " + column + " "  + type + " " + size);
                    jsonArray.add(jsonObject);
                }
                rs.close();
            }
            tables.close();

            // get sys_config
            ResultSet sys_config_only = data.getColumns(null, null, "sys_config", "%");
            while (sys_config_only.next()){
                String table = sys_config_only.getString("TABLE_NAME");
                String column = sys_config_only.getString("COLUMN_NAME");
                String type = sys_config_only.getString("TYPE_NAME");
                String size = sys_config_only.getString("COLUMN_SIZE");

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("table", table);
                jsonObject.addProperty("column", column);
                jsonObject.addProperty("type", type);
                jsonObject.addProperty("size", size);
                //System.out.println(table + "  " + column + " "  + type + " " + size);
                jsonArray.add(jsonObject);
            }
            sys_config_only.close();

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
    }

}