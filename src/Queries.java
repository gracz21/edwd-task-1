/**
 * @author Kamil Walkowiak
 */
class Queries {
    static String oldSongsCreateQuery = "CREATE TABLE OLD_SONGS " +
            "(ID STRING PRIMARY KEY NOT NULL," +
            "ARTIST STRING," +
            "TITLE STRING)";
    static String oldPlaybacksCreateQuery = "CREATE TABLE OLD_PLAYBACKS " +
            "(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
            "USER_ID STRING NOT NULL," +
            "SONG_ID STRING NOT NULL," +
            "TIMESTAMP INTEGER," +
            "FOREIGN KEY (SONG_ID) REFERENCES OLD_SONGS (ID))";

    static String songsCreateQuery = "CREATE TABLE SONGS " +
            "(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
            "OLD_ID STRING," +
            "TITLE STRING);" +
            "CREATE UNIQUE INDEX SONGS_OLD_ID_IDX " +
            "ON SONGS (OLD_ID);";
    static String artistsCreateQuery = "CREATE TABLE ARTISTS " +
            "(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
            "NAME STRING);" +
            "CREATE UNIQUE INDEX ARTISTS_NAME_IDX " +
            "ON ARTISTS (NAME);";
    static String playbacksCreateQuery = "CREATE TABLE PLAYBACKS " +
            "(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
            "SONG_ID INTEGER NOT NULL," +
            "ARTIST_ID INTEGER NOT NULL," +
            "DATE_ID INTEGER NOT NULL," +
            "TIME_ID INTEGER NOT NULL," +
            "USER_ID INTEGER NOT NULL," +
            "FOREIGN KEY (SONG_ID) REFERENCES SONGS (ID)," +
            "FOREIGN KEY (ARTIST_ID) REFERENCES ARTISTS (ID)," +
            "FOREIGN KEY (DATE_ID) REFERENCES DATES (ID)," +
            "FOREIGN KEY (TIME_ID) REFERENCES TIME (ID)," +
            "FOREIGN KEY (USER_ID) REFERENCES USERS (ID))";
    static String datesCreateQuery = "CREATE TABLE DATES " +
            "(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
            "YEAR INTEGER," +
            "MONTH INTEGER," +
            "DAY INTEGER);" +
            "CREATE UNIQUE INDEX UNIQUE_DATES_IDX " +
            "ON DATES (YEAR, MONTH, DAY);";
    static String timesCreateQuery = "CREATE TABLE TIMES " +
            "(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
            "HOUR INTEGER," +
            "MINUTE INTEGER);" +
            "CREATE UNIQUE INDEX UNIQUE_TIMES_IDX " +
            "ON TIMES (HOUR, MINUTE);";
    static String usersCreateQuery = "CREATE TABLE USERS " +
            "(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
            "OLD_ID STRING);" +
            "CREATE UNIQUE INDEX USERS_OLD_ID_IDX " +
            "ON USERS (OLD_ID);";
}
