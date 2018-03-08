package webHandler;

import java.sql.*;
import java.util.ArrayList;

public class DBUtil {

    private static DBUtil instance = null;
    private Connection conn;

    public static DBUtil getInstance()
    {
        if (instance == null) {
            instance = new DBUtil("captureReplay.db");
        }

        return instance;
    }

    /**
     * Connect to a database
     *
     * @param databaseFile the database file name
     */
    private DBUtil(String databaseFile)
    {
        this.conn = connectSqlite(databaseFile);
        createNewCaptureTable(databaseFile);
        createNewReplayTable(databaseFile);
    }

    private static Connection connectSqlite(String databaseFile)
    {
        Connection conn = null;
        try {
            Class.forName("org.sqlite.JDBC");

            conn = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);
            Statement stmt = conn.createStatement();

            // Enable WAL-mode transactions for concurrent writing.
            stmt.execute("PRAGMA synchronous = NORMAL;");
            stmt.execute("PRAGMA journal_mode = WAL;");

            // Increase cache size to 128MB and allow 4 extra query threads for performance.
            stmt.execute("PRAGMA cache_size = -131072;");
            stmt.execute("PRAGMA threads = 4;");

            // Enable foreign key enforcement to protect against inserting invalid data.
            stmt.execute("PRAGMA foreign_keys = ON;");

            System.out.println("Connection to SQLite has been established.");

            stmt.close();
        } catch(ClassNotFoundException e) {
            System.err.println(e.getMessage());
        } catch(SQLException e)
        {
            // if the error message is "out of memory",
            // it probably means no database file is found
            System.err.println(e.getMessage());
        }
        finally
        {
//            try
//            {
//                if(conn != null)
//                    conn.close();
//            }
//            catch(SQLException e)
//            {
//                // connection close failed.
//                System.err.println(e);
//            }
        }
        return conn;
    }

    public Connection getConnection()
    {
        return conn;
    }

    public void closeConnection() throws SQLException
    {
        try
        {
            if (conn != null)
            {
                conn.close();
            }
        }

        catch (SQLException e)
        {
            System.err.println(e.getMessage());
        }
    }

    private void createNewCaptureTable(String databaseFile)
    {

        try
        {
            String sql = "CREATE TABLE IF NOT EXISTS captures(\n"
                    + " dbId INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                    + " id TEXT,\n"
                    + " rds TEXT,\n"
                    + " s3 TEXT,\n"
                    + " startTime TEXT,\n"
                    + " endTime TEXT,\n"
                    + " status TEXT,\n"
                    + " fileSizeLimit INTEGER,\n"
                    + " transactionLimit INTEGER,\n"
                    + " dbFileSize INTEGER,\n"
                    + " numDbTransactions INTEGER\n"
                    + ");";

            //Connection conn = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);
            Statement stmt = conn.createStatement();

            stmt.execute(sql);
            //conn.close();
            stmt.close();
        }

        catch (SQLException e)
        {
            System.err.println(e.getMessage());
        }
    }

    private void createNewReplayTable(String databaseFile)
    {

        try
        {
            String sql = "CREATE TABLE IF NOT EXISTS replays(\n"
                    + " dbId INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                    + " capture_id TEXT,\n"
                    + " id TEXT,\n"
                    + " rds TEXT,\n"
                    + " s3 TEXT,\n"
                    + " startTime TEXT,\n"
                    + " endTime TEXT,\n"
                    + " status TEXT,\n"
                    + " FOREIGN KEY(capture_id) REFERENCES captures(id)\n"
                    + ");";

            //Connection conn = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);
            Statement stmt = conn.createStatement();

            stmt.execute(sql);
            //conn.close();
            stmt.close();
        }

        catch (SQLException e)
        {
            System.err.println(e.getMessage());
        }
    }

    public boolean saveCapture (Capture capture)
    {
        //inserts values, if value is there then it's replaced
        String sql = "INSERT OR REPLACE INTO captures(id, rds, s3, startTime, endTime, status, " +
                "fileSizeLimit, transactionLimit, dbFileSize, numDbTransactions) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?)";

        try
        {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, capture.getId());
            pstmt.setString(2, capture.getRds());
            pstmt.setString(3, capture.getS3());
            pstmt.setTimestamp(4, new Timestamp(capture.getStartTime().getTime()));
            pstmt.setTimestamp(5, new Timestamp(capture.getEndTime().getTime()));
            pstmt.setString(6, capture.getStatus());
            pstmt.setInt(7, capture.getFileSizeLimit());
            pstmt.setInt(8, capture.getTransactionLimit());
            pstmt.setInt(9, capture.getDbFileSize());
            pstmt.setInt(10, capture.getNumDBTransactions());
            pstmt.executeUpdate();

            pstmt.close();


            if (capture.getId() == null) //TODO: last row id implementation
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid");
                rs.next();
                String id = rs.getString(1);
                capture.setId(id);
                pstmt.close();
            }

            return true;
        }
        catch (SQLException e){
            System.err.println(e.getMessage());
            return false;
        }
    }

    public Capture loadCapture (String id)
    {
        try
        {
            Capture capture = new Capture();
            ResultSet rs;

            String sql = "SELECT * FROM captures WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.execute();

            rs = pstmt.getResultSet();

            if (!rs.next()) {
                rs.close();
                pstmt.close();
                return null;
            }

            capture.setId(rs.getString(2));
            capture.setRds(rs.getString(3));
            capture.setS3(rs.getString(4));
            capture.setStartTime(rs.getDate(5));
            capture.setEndTime(rs.getDate(6));
            capture.setStatus(rs.getString(7));
            capture.setFileSizeLimit(rs.getInt(8));
            capture.setDbFileSize(rs.getInt(9));
            capture.setNumDBTransactions(rs.getInt(10));

            return capture;
        }

        catch (SQLException e)
        {
            System.err.println(e.getMessage());
            return null;
        }
    }

    public ArrayList<Capture> loadAllCaptures()
    {
        try
        {
            ResultSet rs;
            ArrayList<Capture> captures = new ArrayList<>();

            String sql = "SELECT * FROM captures";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.execute();
            rs = pstmt.getResultSet();

            while (rs.next()) {
                Capture capture = new Capture();
                capture.setId(rs.getString(2));
                capture.setRds(rs.getString(3));
                capture.setS3(rs.getString(4));
                capture.setStartTime(rs.getDate(5));
                capture.setEndTime(rs.getDate(6));
                capture.setStatus(rs.getString(7));
                capture.setFileSizeLimit(rs.getInt(8));
                capture.setDbFileSize(rs.getInt(9));
                capture.setNumDBTransactions(rs.getInt(10));

                captures.add(capture);
            }

            return captures;
        }

        catch (SQLException e)
        {
            System.err.println(e.getMessage());
            return null;
        }
    }

    /*
    public boolean saveReplay(Replay replay)
    {
        //inserts values, if value is there then it's replaced
        String sql = "INSERT OR REPLACE INTO replays(id, rds, s3, startTime, endTime, status) " +
                "VALUES (?,?,?,?,?,?)";

        try
        {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, replay.getId());
            pstmt.setString(2, replay.getRds());
            pstmt.setString(3, replay.getS3());
            pstmt.setTimestamp(4, new Timestamp(replay.getStartTime().getTime()));
            pstmt.setTimestamp(5, new Timestamp(replay.getEndTime().getTime()));
            pstmt.setString(6, replay.getStatus());
            pstmt.executeUpdate();

            pstmt.close();


            if (replay.getId() == null) //TODO: last row id implementation
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid");
                rs.next();
                String id = rs.getString(1);
                replay.setId(id);
                pstmt.close();
            }

            return true;
        }

        catch (SQLException e){
            System.err.println(e.getMessage());
            return false;
        }
    }

    public Replay loadReplay(String id)
    {
        try
        {
            Replay replay = new Replay();
            ResultSet rs;

            String sql = "SELECT * FROM replays WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.execute();

            rs = pstmt.getResultSet();

            if (!rs.next()) {
                rs.close();
                pstmt.close();
                return null;
            }

            replay.setId(rs.getString(1));
            replay.setRds(rs.getString(2));
            replay.setS3(rs.getString(3));
            replay.setStartTime(rs.getDate(4));
            replay.setEndTime(rs.getDate(5));
            replay.setStatus(rs.getString(6));

            return replay;
        }

        catch (SQLException e)
        {
            System.err.println(e.getMessage());
            return null;
        }
    }

    public ArrayList<Replay> loadAllReplays()
    {
        try
        {
            ResultSet rs;
            ArrayList<Replay> replays = new ArrayList<>();

            String sql = "SELECT * FROM replays";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.execute();
            rs = pstmt.getResultSet();

            while (rs.next()) {
                Replay replay = new Replay();
                replay.setId(rs.getString(1));
                replay.setRds(rs.getString(2));
                replay.setS3(rs.getString(3));
                replay.setStartTime(rs.getDate(4));
                replay.setEndTime(rs.getDate(5));
                replay.setStatus(rs.getString(6));

                replays.add(replay);
            }

            return replays;
        }

        catch (SQLException e)
        {
            System.err.println(e.getMessage());
            return null;
        }
    }*/

    /*public static void main (String[] args)
    {
        connectSqlite("/Users/devin/test.db");
    }*/
}

