package ParsingFiles.src.main.java;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

public class StarsParser extends DefaultHandler {

    List<Stars> stars;
    private Integer stars_added = 0;
    private Integer stars_dupes = 0;

    private String tempVal;

    //to maintain context
    private Stars tempStar;
    private Integer currentId;

    private static PreparedStatement insertStar, checkDupe, currentId_query;
    private static Connection conn;
    private static HashSet<String> all_stars = new HashSet<>();

    public StarsParser(){
        stars = new ArrayList<Stars>();

        String loginUser = "mytestuser";
        String loginPasswd = "testpassword";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
            conn.setAutoCommit(false);
            insertStar = conn.prepareStatement("INSERT INTO stars (id, name,  birthYear) VALUES (?, ?, ?)");
            // checkDupe = conn.prepareStatement("SELECT * FROM stars WHERE name = ? AND birthYear = ?");

            // get current max id
            Statement statement = conn.createStatement();
            ResultSet maxRs = statement.executeQuery("SELECT max(substring(id, 3)) FROM stars");
            if (maxRs.next()) {
                currentId = maxRs.getInt(1) + 1;
            }
            maxRs.close();
            statement.close();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void run() throws SQLException {
        preloadData();
        parseDocument();
        batchInsert();
        printData();
    }

    private void parseDocument() {

        //get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {

            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse("ParsingFiles/actors63.xml", this);

        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    private void preloadData() throws SQLException{
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery("SELECT name, COALESCE(birthYear, -1) as birthYear FROM stars");
        while(rs.next()){
            String key = rs.getString("name") + "_" + rs.getInt("birthYear");
            all_stars.add(key);
        }
        rs.close();
        statement.close();
    }

    /**
     * Iterate through the list and print
     * the contents
     */
    private void printData() {

        // System.out.println("No of Stars '" + stars.size() + "'.");
        System.out.println("Inserted " + stars_added + " stars.");
        System.out.println(stars_dupes + " stars duplicate.");

//        Iterator<Stars> it = stars.iterator();
//        while (it.hasNext()) {
//            System.out.println(it.next().toString());
//        }
    }

    private void batchInsert(){
        int batch_size = 700;
        int batch_count = 0;

        try{
            for (Stars star : stars){
                String current_key = star.getName() + "_" + star.getDOB();

                if (all_stars.contains(current_key)){
                    // Duplicate found, write it to the file
                    PrintWriter writer = new PrintWriter(new FileWriter("StarDuplicates.txt", true));
                    writer.println("Duplicate " + star.toString());
                    writer.close();
                    stars_dupes++;
                } else {
                    // No duplicate, insert the new star into the database
                    all_stars.add(current_key);
                    String newStarId = "nm"+currentId;
                    currentId++;

                    insertStar.setString(1, newStarId);
                    insertStar.setString(2, star.getName());
                    if(star.getDOB() == -1){
                        insertStar.setString(3, null);
                    } else {
                        insertStar.setInt(3, star.getDOB());
                    }
                    insertStar.addBatch();
                    batch_count++;

                    if(batch_count >= batch_size){
                        insertStar.executeBatch();
                        conn.commit();
                        batch_count = 0;
                    }
                    stars_added++;

                }

            }
            // extras in the batch after last loop
            if (batch_count > 0) {
                insertStar.executeBatch();
                conn.commit();
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
    //Event Handlers
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        tempVal = "";
        if (qName.equalsIgnoreCase("actor")) {
            //create a new instance of employee
            tempStar = new Stars();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.equalsIgnoreCase("actor")) {
            //add it to the list
            stars.add(tempStar);
//            try {
//                // Check for duplicates in the database
////                checkDupe.setString(1, tempStar.getName());
////                if(tempStar.getDOB() == -1){
////                    checkDupe.setString(2, null);
////                } else {
////                    checkDupe.setInt(2, tempStar.getDOB());
////                }
////                ResultSet rs = checkDupe.executeQuery();
//                String current_key = tempStar.getName() + "_" + tempStar.getDOB();
//                if (all_stars.contains(current_key)){
//                    // Duplicate found, write it to the file
//                    PrintWriter writer = new PrintWriter(new FileWriter("StarDuplicates.txt", true));
//                    writer.println("Duplicate " + tempStar.toString());
//                    writer.close();
//                } else {
//                    // No duplicate, insert the new star into the database
//                    // Generate new starId by getting max current starId + 1
//                    Statement stmt = conn.createStatement();
//                    ResultSet maxRs = stmt.executeQuery("SELECT CONCAT('nm', (SELECT max(substring(id, 3)) FROM stars_backup) + 1)");
//                    String newStarId = "";
//                    if (maxRs.next()) {
//                        newStarId = maxRs.getString(1);
//                    }
//
//                    insertStar.setString(1, newStarId);
//                    insertStar.setString(2, tempStar.getName());
//                    if(tempStar.getDOB() == -1){
//                        insertStar.setString(3, null);
//                    } else {
//                        insertStar.setInt(3, tempStar.getDOB());
//                    }
//                    insertStar.executeUpdate();
//                    stmt.close();
//                    conn.commit();
//                }
//            } catch (SQLException | IOException e) {
//                e.printStackTrace();
//
//            }
        } else if (qName.equalsIgnoreCase("stagename")) {
            tempStar.setName(tempVal);
        } else if (qName.equalsIgnoreCase("dob")) {
            try {
                tempStar.setDOB(Integer.parseInt(tempVal));
            } catch (Exception e) {
                tempStar.setDOB(-1);
            }
        }

    }

    public static void main(String[] args) throws SQLException {
        StarsParser spe = new StarsParser();
        spe.run();
    }

}