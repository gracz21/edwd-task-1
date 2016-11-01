/**
 * @author Kamil Walkowiak
 */
class Queries {
    //Old schema queries
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

    //New schema queries
    static String songsCreateQuery = "CREATE TABLE SONGS " +
            "(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
            "OLD_ID STRING NOT NULL," +
            "TITLE STRING)";
    static String artistsCreateQuery = "CREATE TABLE ARTISTS " +
            "(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
            "NAME STRING NOT NULL)";
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
            "YEAR INTEGER NOT NULL," +
            "MONTH INTEGER NOT NULL," +
            "DAY INTEGER NOT NULL)";
    static String timesCreateQuery = "CREATE TABLE TIMES " +
            "(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
            "HOUR INTEGER NOT NULL," +
            "MINUTE INTEGER NOT NULL)";
    static String usersCreateQuery = "CREATE TABLE USERS " +
            "(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
            "OLD_ID STRING NOT NULL)";

    //New schema indexes queries
    static String indexOnSongIdFkCreateQuery = "CREATE INDEX SONG_ID_FK ON PLAYBACKS (SONG_ID)";
    static String indexOnArtistIdFkCreateQuery = "CREATE INDEX ARTIST_ID_FK ON PLAYBACKS (ARTIST_ID)";
    static String indexOnDateIdFkCreateQuery = "CREATE INDEX DATE_ID_FK ON PLAYBACKS (DATE_ID)";
    static String indexOnTimeIdFkCreateQuery = "CREATE INDEX TIME_ID_FK ON PLAYBACKS (TIME_ID)";
    static String indexOnUserIdFkCreateQuery = "CREATE INDEX USER_ID_FK ON PLAYBACKS (USER_ID)";
}
