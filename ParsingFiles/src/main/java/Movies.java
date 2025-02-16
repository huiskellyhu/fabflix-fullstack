package ParsingFiles.src.main.java;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Movies {
    private String id;

    private String fid;

    private String title;

    private int year;

    private String director;

    Set<String> genres;

    public Movies(){
        genres = new HashSet<String>();
        title = "-1";
        year = -1;
        director = "-1";
        fid = "-1";
    }

    public Movies(String id, String title, int year, String director) {
        this.id = id;
        this.title = title;
        this.year  = year;
        this.director = director;

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFid() {
        return fid;
    }

    public void setFid(String fid) {
        this.fid = fid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public Set<String> getGenres() {
        return genres;
    }

    public void addGenre(String genre){
        this.genres.add(genre);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Movie Details - ");
        sb.append("Id:" + getFid());
        sb.append(", ");
        sb.append("Title:" + getTitle());
        sb.append(", ");
        sb.append("Year:" + getYear());
        sb.append(", ");
        sb.append("Director:" + getDirector());

        return sb.toString();
    }

}
