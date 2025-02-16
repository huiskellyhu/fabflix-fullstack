package ParsingFiles.src.main.java;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

public class CastsParser extends DefaultHandler {

    private HashMap<Stars, Movies> sims_to_add;
    // reference: https://www.w3schools.com/java/java_hashmap.asp
    // to loop:
//    for (String i : sims_to_add.keySet()) {
//        System.out.println("key: " + i + " value: " + sims_to_add.get(i));
//    }
    private Integer stars_in_movies_added = 0;
    private Integer movies_notfound = 0;

    private Integer stars_notfound = 0;

    private String tempVal;

    //to maintain context
    private Movies tempMovie;
    private Stars tempStar;
    private String tempDirector;
    private String tempFid;

    private static PreparedStatement insertStarInMovies;
    private static Connection conn;

    // for preloadingdata
    // all_movies: KEY=title_year_director, VALUE=movieId
    //private static HashMap<String, String> all_movies = new HashMap<>();

    // all_stars: KEY=starname, VALUE=starId
    private static HashMap<String, String> all_stars = new HashMap<>();

    public CastsParser(){
        sims_to_add = new HashMap<>();
        String loginUser = "mytestuser";
        String loginPasswd = "testpassword";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
            conn.setAutoCommit(false);
            insertStarInMovies = conn.prepareStatement("INSERT IGNORE INTO stars_in_movies (starId, movieId) VALUES (?, ?)");


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
            sp.parse("ParsingFiles/casts124.xml", this);

        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    private void preloadData() throws SQLException{
//        Statement statement = conn.createStatement();
//        ResultSet rs = statement.executeQuery("SELECT id, title, year, director FROM movies_backup");
//        while(rs.next()){
//            String key = rs.getString("title") + "_" + rs.getInt("year") + "_" + rs.getString("director");
//            all_movies.put(key,rs.getString("id"));
//        }
//        rs.close();
//        statement.close();
        // TO PRELOADDATA FOR MOVIES...NEED FID FOUND IN MoviesParser. turn public and add fid
        // testing
        //System.out.println(MoviesParser.all_movies_fids.values());
        // testing

        Statement star_statement = conn.createStatement();
        ResultSet star_rs = star_statement.executeQuery("SELECT id, name FROM stars");
        while(star_rs.next()){
            String key = star_rs.getString("name");
            all_stars.put(key, star_rs.getString("id"));
        }
        star_rs.close();
        star_statement.close();
    }

    /**
     * Iterate through the list and print
     * the contents
     */
    private void printData() {

        System.out.println("Inserted " + stars_in_movies_added + " stars_in_movies.");
        System.out.println(movies_notfound + " movies not found.");
        System.out.println(stars_notfound + " stars not found.");

//        Iterator<Movies> it = movies.iterator();
//        while (it.hasNext()) {
//            System.out.println(it.next().toString());
//        }
    }

    private void batchInsert(){
        int batch_size = 400;
        int batch_count = 0;

        // pass tempStar into <> sims_to_add
        // set fields of tempStar (setId)
//        if(tempStar != null){
//            tempStar.setId(all_stars.get(tempStar.getName()));
//        }
//        // pass tempMovie into <> sims_to_add
//        //  set fields of tempMovie
//        //      - look for FID first, then look for title + director if no FID
//        //      then go through the containsKey() for all_movies_fids
//        if(tempMovie != null){
//            String current_fid_key = tempMovie.getFid() + "_" + tempMovie.getTitle() + "_" + tempMovie.getDirector();
//        }

        try{
            for (Stars star : sims_to_add.keySet()) {
                // sims_to_add.get(star) to get the movie
                boolean star_ready = false;
                boolean movie_ready = false;
                if (all_stars.containsKey(star.getName())) {
                    // star found
                    star.setId(all_stars.get(star.getName()));
                    star_ready = true;
                } else {
                    // Star not found, write to file
                    PrintWriter writer = new PrintWriter(new FileWriter("StarNotFound.txt", true));
                    writer.println();
                    writer.close();
                    stars_notfound++;
                }

                Movies curr_movie = sims_to_add.get(star);
                String current_fid_key = curr_movie.getFid() + "_" + curr_movie.getTitle() + "_" + curr_movie.getDirector();
                //System.out.println("Current fid: " + current_fid_key);

                if (MoviesParser.all_movies_fids.containsKey(current_fid_key)) {
                    // movie found

                    //System.out.println("Movie found: " + MoviesParser.all_movies_fids.containsKey(current_fid_key) );
                    //System.out.println("Got");
                    curr_movie.setId(MoviesParser.all_movies_fids.get(current_fid_key));
                    movie_ready = true;
                } else {
                    // movie not found, write to file
                    PrintWriter writer = new PrintWriter(new FileWriter("MovieNotFound.txt", true));
                    writer.println();
                    writer.close();
                    movies_notfound++;

                }

                if (star_ready && movie_ready) {
                    insertStarInMovies.setString(1, star.getId());
                    insertStarInMovies.setString(2, curr_movie.getId());
                    insertStarInMovies.addBatch();
//                    if(star.getId().length() > 9){
//                        System.out.println("Inserting: " + star.getId() + " " + curr_movie.getId());
//                    }
                    batch_count++;
                    stars_in_movies_added++;

                    if(batch_count >= batch_size){
                        insertStarInMovies.executeBatch();
                        conn.commit();
                        batch_count = 0;
                        //System.out.println("Executed batch");
                    }
                }

            }
            // extras in the batch after last loop
            if (batch_count > 0) {
                insertStarInMovies.executeBatch();
                conn.commit();
                //System.out.println("Executed final batch");
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
    //Event Handlers
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        tempVal = "";
        if (qName.equalsIgnoreCase("m")) {
            //create a new instance of movie
            tempMovie = new Movies();
        } else if (qName.equalsIgnoreCase("a")) {
            //create a new instance of star
            tempStar = new Stars();
        }

    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("dirfilms")) {
            tempDirector = null;
        } else if (qName.equalsIgnoreCase("is")) {
            tempDirector = tempVal;
        } else if (qName.equalsIgnoreCase("m")) {
            // this set ends, add to sims_to_add
            if(tempStar != null && tempMovie != null){
                if(tempDirector != null){
                    tempMovie.setDirector(tempDirector);
                }
                if(tempFid != null) {
                    tempMovie.setFid(tempFid);
                }
                sims_to_add.put(tempStar, tempMovie);
            }
        } else if (qName.equalsIgnoreCase("a")) {
            if(tempStar != null) {
                tempStar.setName(tempVal);
            }
        } else if (qName.equalsIgnoreCase("f")) {
            if(tempMovie != null){
                tempMovie.setFid(tempVal);
            }
        } else if (qName.equalsIgnoreCase("t")) {
            if(tempMovie != null){
                tempMovie.setTitle(tempVal);
            }


        }


    }

    public static void main(String[] args) throws SQLException {
        CastsParser spe = new CastsParser();
        spe.run();
    }

}