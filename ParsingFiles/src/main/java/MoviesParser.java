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

public class MoviesParser extends DefaultHandler {

    List<Movies> movies;
    private Integer movies_added = 0;
    private Integer movies_dupes = 0;
    private Integer movies_inc = 0;

    List<String> genres;
    private Integer genres_added = 0;
    private Integer genres_in_movies_added = 0;

    private String tempVal;

    //to maintain context
    private Movies tempMovie;
    private String tempGenre;
    private Integer currentId;

    private static PreparedStatement insertMovie, insertGenre, insertGenreInMovies;
    private static Connection conn;
    private static HashMap<String, Movies> all_movies;
    public static HashMap<String, String> all_movies_fids;

    // private static HashSet<String> all_genres = new HashSet<>();
    private static HashMap<String, Integer> all_genres = new HashMap<>();
        // use all_genres.containsValue(somevalue) to check for genres
    private Integer currentGenreId;

    // private static HashSet<String> all_stars = new HashSet<>(); // USE LATER

    public MoviesParser(){
        movies = new ArrayList<Movies>();
        genres = new ArrayList<String>();
        all_movies = new HashMap<>();
        all_movies_fids = new HashMap<>();

        String loginUser = "mytestuser";
        String loginPasswd = "testpassword";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
            conn.setAutoCommit(false);
            insertMovie = conn.prepareStatement("INSERT INTO movies (id, title,  year, director, price) VALUES (?, ?, ?, ?, FLOOR(1+RAND() * 100))");
            insertGenre = conn.prepareStatement("INSERT INTO genres (name) VALUES (?)");
            insertGenreInMovies = conn.prepareStatement("INSERT IGNORE INTO genres_in_movies (genreId, movieId) VALUES (?, ?)");

            // get current max id
            Statement statement = conn.createStatement();
            ResultSet maxRs = statement.executeQuery("SELECT max(substring(id, 3)) FROM movies");
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
            sp.parse("mains243.xml", this);

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
        ResultSet rs = statement.executeQuery("SELECT id, title, year, director FROM movies");
        while(rs.next()){
            String key = rs.getString("title") + "_" + rs.getInt("year") + "_" + rs.getString("director");
            Movies temp = new Movies();
            temp.setId(rs.getString("id"));
            temp.setTitle(rs.getString("title"));
            temp.setYear(rs.getInt("year"));
            temp.setDirector(rs.getString("director"));

            all_movies.put(key, temp);

            // FID_title_director (FID is -1 for preloaded.)
            String fid_key = "-1" + "_" + rs.getString("title") + "_" + rs.getString("director");
            all_movies_fids.put(fid_key, rs.getString("id"));
        }
        rs.close();
        statement.close();

        Statement genre_statement = conn.createStatement();
        ResultSet genre_rs = genre_statement.executeQuery("SELECT id, name FROM genres");
        while(genre_rs.next()){
            all_genres.put(
                    genre_rs.getString("name"), genre_rs.getInt("id")
            );
        }
        currentGenreId = all_genres.size() + 1;
        genre_rs.close();
        genre_statement.close();
    }

    /**
     * Iterate through the list and print
     * the contents
     */
    private void printData() {

        System.out.println("Inserted " + movies_added + " movies.");
        System.out.println(movies_dupes + " movies duplicate.");
        System.out.println(movies_inc + " movies inconsistent.");
        System.out.println("Inserted " + genres_added + " genres.");
        System.out.println("Inserted " + genres_in_movies_added + " genres_in_movies.");

//        Iterator<Movies> it = movies.iterator();
//        while (it.hasNext()) {
//            System.out.println(it.next().toString());
//        }
    }

    private void batchInsert(){
        int batch_size = 700;
        int batch_count = 0;

        try{
            for (Movies movie : movies){
                String current_key = movie.getTitle() + "_" + movie.getYear() + "_" + movie.getDirector();

                 if (current_key.contains("-1")){
                     // Inconsistency found, write to the file
                     PrintWriter writer = new PrintWriter(new FileWriter("MovieInconsistents.txt", true));
                     writer.println("Inconsistent " + movie.toString());
                     writer.close();
                     movies_inc++;
                 }
                 else if (all_movies.containsKey(current_key)){
                    // Duplicate found, write it to the file
                    PrintWriter writer = new PrintWriter(new FileWriter("MovieDuplicates.txt", true));
                    writer.println("Duplicate " + movie.toString());
                    writer.close();
                    movies_dupes++;
                } else {
                    // No duplicate, insert the new movie into the database
                    String newMovieId = "tt"+ String.format("%07d", currentId);
                    movie.setId(newMovieId);

                    // FID_title_director

                     String fid_key = movie.getFid() + "_" + movie.getTitle() + "_" + movie.getDirector();

                     all_movies_fids.put(fid_key, newMovieId);
                    all_movies.put(current_key, movie);
                    currentId++;

                    insertMovie.setString(1, newMovieId);
                    insertMovie.setString(2, movie.getTitle());
                    insertMovie.setInt(3, movie.getYear());
                    insertMovie.setString(4, movie.getDirector());

                    insertMovie.addBatch();
                    batch_count++;



                    movies_added++;

                    // link current movie's genre in genres_in_movies
                     // loop through the current movie's genre
                     // if genre not exists, insertGenre and genres_added++
                     //
                     // then link gim and movieid, insertGenreInMovie
                    for(String current_genre : movie.getGenres()){
                        if(!all_genres.containsKey(current_genre)){
                            // Genre not found, need to add
                            insertGenre.setString(1, current_genre);
                            insertGenre.executeUpdate();
                            conn.commit();
                            all_genres.put(current_genre, currentGenreId++);
                            genres_added++;
                        }
                        // need to get genreid and movieid
                        insertGenreInMovies.setInt(1, all_genres.get(current_genre));
                        insertGenreInMovies.setString(2, movie.getId());
                        insertGenreInMovies.addBatch();
                        genres_in_movies_added++;
                    }

                     if(batch_count >= batch_size){
                         insertMovie.executeBatch();
                         insertGenreInMovies.executeBatch();
                         conn.commit();
                         batch_count = 0;
                     }
                }

            }
            // extras in the batch after last loop
            if (batch_count > 0) {
                insertMovie.executeBatch();
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
        if (qName.equalsIgnoreCase("film")) {
            //create a new instance of movie
            tempMovie = new Movies();
        }
//        } else if (qName.equalsIgnoreCase("cat")) {
//            //create a new instance of movie
//            tempGenre = new Genres();
//        }

    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("film")) {
            //add it to the list
            movies.add(tempMovie);

        } else if (qName.equalsIgnoreCase("fid")) {
            tempMovie.setFid(tempVal);
        } else if (qName.equalsIgnoreCase("t")) {
            if(tempVal.isEmpty()){
                tempMovie.setTitle("-1");
            } else {
                tempMovie.setTitle(tempVal);
            }
        } else if (qName.equalsIgnoreCase("year")) {
            try {
                tempMovie.setYear(Integer.parseInt(tempVal));
            } catch (Exception e) {
                tempMovie.setYear(-1);
            }
        } else if (qName.equalsIgnoreCase("dirn")) {
            if(tempVal.isEmpty()){
                tempMovie.setDirector("-1");
            } else {
                tempMovie.setDirector(tempVal);
            }
        } else if (qName.equalsIgnoreCase("cat")) {
            //tempMovie.setGenre(tempVal);
            if(tempVal != null){
                tempGenre = tempVal.toLowerCase();
                tempMovie.addGenre(tempGenre);

                genres.add(tempGenre);
            }


        }


    }

    public static void main(String[] args) throws SQLException {
        MoviesParser spe = new MoviesParser();
        spe.run();
    }

}