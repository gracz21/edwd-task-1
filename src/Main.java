import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * @author Kamil Walkowiak
 */
public class Main {
    private static Map<String, String> tablesMap = new HashMap<>();

    private static BufferedReader uniqueTracks;
    private static BufferedReader tripletsSample;
    private static Connection con;


    public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException {
        uniqueTracks = new BufferedReader(
                new InputStreamReader(new FileInputStream("./res/unique_tracks.txt"), "ISO-8859-1"));
        tripletsSample = new BufferedReader(
                new InputStreamReader(new FileInputStream("./res/triplets_sample_20p.txt")));

        tablesMap.put("OLD_SONGS", Queries.oldSongsCreateQuery);
        tablesMap.put("OLD_PLAYBACKS", Queries.oldPlaybacksCreateQuery);
        tablesMap.put("SONGS", Queries.songsCreateQuery);
        tablesMap.put("ARTISTS", Queries.artistsCreateQuery);
        tablesMap.put("PLAYBACKS", Queries.playbacksCreateQuery);
        tablesMap.put("DATES", Queries.datesCreateQuery);
        tablesMap.put("TIMES", Queries.timesCreateQuery);
        tablesMap.put("USERS", Queries.usersCreateQuery);

        con = null;
        Class.forName("org.sqlite.JDBC");
        con = DriverManager.getConnection("jdbc:sqlite:test.db");
        con.setAutoCommit(false);
        System.out.println("Opened database successfully");

        setupSchemas();

        loadToOldSchema();
        //loadToNewSchema();

        con.close();
    }

    private static void setupSchemas() throws SQLException {
        DatabaseMetaData meta = con.getMetaData();
        ResultSet res;

        for(String tableName: tablesMap.keySet()) {
            res = meta.getTables(null, null, tableName, new String[] {"TABLE"});
            if(!res.next()) {
                Statement stmt = con.createStatement();
                stmt.execute(tablesMap.get(tableName));
                stmt.close();
            }
            res.close();
        }
    }

    private static void loadToOldSchema() throws IOException, SQLException {
        final String insertIntoOldSongsQuery = "INSERT OR IGNORE INTO OLD_SONGS VALUES (?, ?, ?)";
        final String insertIntoOldPlaybacksQuery = "INSERT INTO OLD_PLAYBACKS(USER_ID, SONG_ID, TIMESTAMP) VALUES (?, ?, ?)";

        PreparedStatement ps;
        String line;
        String[] splittedLine;

        long startTime = System.nanoTime();
        ps = con.prepareStatement(insertIntoOldSongsQuery);
        while((line = uniqueTracks.readLine()) != null) {
            splittedLine = line.split("<SEP>");

            if(splittedLine.length != 4) {
                continue;
            }

            ps.setString(1, splittedLine[1]);
            ps.setString(2, splittedLine[2]);
            ps.setString(3, splittedLine[3]);
            ps.addBatch();
        }
        ps.executeBatch();
        ps.close();
        con.commit();
        System.out.println("Finished old songs");

        int i = 0;
        ps = con.prepareStatement(insertIntoOldPlaybacksQuery);
        while((line = tripletsSample.readLine()) != null) {
            splittedLine = line.split("<SEP>");

            ps.setString(1, splittedLine[0]);
            ps.setString(2, splittedLine[1]);
            ps.setString(3, splittedLine[2]);
            ps.addBatch();
            i++;

            if(i >= 1000000) {
                ps.executeBatch();
                con.commit();
                ps.clearBatch();
                i = 0;
            }
        }
        ps.executeBatch();
        con.commit();
        ps.clearBatch();
        ps.close();

        long elapsedTime = System.nanoTime() - startTime;
        System.out.println("Finished old schema in " + elapsedTime/1000000000 + " s");
    }

    private static void loadToNewSchema() throws SQLException, IOException {
        final String insertIntoSongs = "INSERT OR IGNORE INTO SONGS(OLD_ID, TITLE) VALUES (?, ?)";
        final String insertIntoArtists = "INSERT OR IGNORE INTO ARTISTS(NAME) VALUES (?)";
        final String insertIntoDates = "INSERT OR IGNORE INTO DATES(YEAR, MONTH, DAY) VALUES (?, ?, ?)";
        final String insertIntoTimes = "INSERT OR IGNORE INTO TIMES(HOUR, MINUTE) VALUES (?, ?)";
        final String insertIntoUsers = "INSERT OR IGNORE INTO USERS(OLD_ID) VALUES(?)";
        final String insertIntoPlaybacks = "INSERT INTO PLAYBACKS(SONG_ID, ARTIST_ID, DATE_ID, TIME_ID, USER_ID) " +
                "VALUES (?, ?, ?, ?, ?)";

        Map<String, Integer> songsOldIdToIdMap = new HashMap<>();
        Map<String, Integer> songsOldIdToArtistIdMap = new HashMap<>();
        Map<String, Integer> artistNameToIdMap = new HashMap<>();
        Map<String, Integer> dateToIdMap = new HashMap<>();
        Map<String, Integer> timeToIdMap = new HashMap<>();
        Map<String, Integer> usersOldIdToIdMap = new HashMap<>();

        int songIdCounter = 1;
        int artistIdCounter = 1;
        int dateIdCounter = 1;
        int timeIdCounter = 1;
        int userIdCounter = 1;

        PreparedStatement[] ps = new PreparedStatement[4];
        String line;
        String[] splittedLine;

        long startTime = System.nanoTime();
        ps[0] = con.prepareStatement(insertIntoSongs);
        ps[1] = con.prepareStatement(insertIntoArtists);
        while((line = uniqueTracks.readLine()) != null) {
            splittedLine = line.split("<SEP>");

            if(splittedLine.length != 4) {
                continue;
            }

            if(!artistNameToIdMap.containsKey(splittedLine[2])) {
                artistNameToIdMap.put(splittedLine[2], artistIdCounter);
                artistIdCounter++;
                ps[1].setString(1, splittedLine[2]);
                ps[1].addBatch();
            }

            if(!songsOldIdToIdMap.containsKey(splittedLine[1])) {
                songsOldIdToIdMap.put(splittedLine[1], songIdCounter);
                songsOldIdToArtistIdMap.put(splittedLine[1], artistNameToIdMap.get(splittedLine[2]));
                songIdCounter++;
                ps[0].setString(1, splittedLine[1]);
                ps[0].setString(2, splittedLine[3]);
                ps[0].addBatch();
            }
        }
        for(int i = 0; i < 2; i++) {
            ps[i].executeBatch();
            ps[i].close();
            con.commit();
        }
        System.out.println("Finished first file");

        ps[0] = con.prepareStatement(insertIntoDates);
        ps[1] = con.prepareStatement(insertIntoTimes);
        ps[2] = con.prepareStatement(insertIntoUsers);
        ps[3] = con.prepareStatement(insertIntoPlaybacks);
        Calendar cal = Calendar.getInstance();
        int numberOfTransactionsInBatch = 0;
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat format2 = new SimpleDateFormat("HH:mm");
        while((line = tripletsSample.readLine()) != null) {
            splittedLine = line.split("<SEP>");

            if(songsOldIdToArtistIdMap.containsKey(splittedLine[1])) {
                cal.setTimeInMillis(Long.parseLong(splittedLine[2])*1000);

                if(!dateToIdMap.containsKey(format1.format(cal.getTime()))) {
                    dateToIdMap.put(format1.format(cal.getTime()), dateIdCounter);
                    dateIdCounter++;
                    ps[0].setInt(1, cal.get(Calendar.YEAR));
                    ps[0].setInt(2, cal.get(Calendar.MONTH));
                    ps[0].setInt(3, cal.get(Calendar.DAY_OF_MONTH));
                    ps[0].addBatch();
                }

                if(!timeToIdMap.containsKey(format2.format(cal.getTime()))) {
                    timeToIdMap.put(format2.format(cal.getTime()), timeIdCounter);
                    timeIdCounter++;
                    ps[1].setInt(1, cal.get(Calendar.HOUR));
                    ps[1].setInt(2, cal.get(Calendar.MINUTE));
                    ps[1].addBatch();
                }

                if(!usersOldIdToIdMap.containsKey(splittedLine[0])) {
                    usersOldIdToIdMap.put(splittedLine[0], userIdCounter);
                    userIdCounter++;
                    ps[2].setString(1, splittedLine[0]);
                    ps[2].addBatch();
                }

                ps[3].setInt(1, songsOldIdToIdMap.get(splittedLine[1]));
                ps[3].setInt(2, songsOldIdToArtistIdMap.get(splittedLine[1]));
                ps[3].setInt(3, dateToIdMap.get(format1.format(cal.getTime())));
                ps[3].setInt(4, timeToIdMap.get(format2.format(cal.getTime())));
                ps[3].setInt(5, usersOldIdToIdMap.get(splittedLine[0]));
                ps[3].addBatch();

                numberOfTransactionsInBatch++;
            }

            if(numberOfTransactionsInBatch >= 1000000) {
                for(PreparedStatement p : ps) {
                    p.executeBatch();
                    con.commit();
                    p.clearBatch();
                }
                numberOfTransactionsInBatch = 0;
            }
        }
        for(PreparedStatement p : ps) {
            p.executeBatch();
            con.commit();
            p.clearBatch();
            p.close();
        }
        long elapsedTime = System.nanoTime() - startTime;
        System.out.println("Finished new schema in " + elapsedTime/1000000000 + " s");
    }
}
