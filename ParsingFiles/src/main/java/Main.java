package ParsingFiles.src.main.java;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {
        StarsParser spe = new StarsParser();
        spe.run();

        MoviesParser mp = new MoviesParser();
        mp.run();

        CastsParser sc = new CastsParser();
        sc.run();

    }
}
