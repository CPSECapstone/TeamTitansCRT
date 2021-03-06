package app.util;

import app.models.DatabaseInfo;
import org.springframework.util.StringUtils;
import app.models.Capture;
import app.models.Replay;

import java.sql.*;
import java.util.ArrayList;

public class DBUtil {

    private static String databaseFile = "captureReplay.db";
    private static DBUtil instance = new DBUtil(databaseFile);
    private static Connection conn;

    public static DBUtil getInstance()
    {
        try{
            if (conn.isClosed()) {
                conn = connectSqlite(databaseFile);
            }
        } catch (SQLException e) { }

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
        checkForFailedCaptures();
        checkForFailedReplays();
    }

    private static Connection connectSqlite(String databaseFile)
    {
        Connection conn = null;
        try {
            Class.forName("org.sqlite.JDBC");

            conn = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);
            java.sql.Statement stmt = conn.createStatement();

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

    public void closeConnection()
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

    public void setDatabaseFile(String file) {
        databaseFile = file;
        instance.closeConnection();
        instance = new DBUtil(databaseFile);
    }

    public void setDatabaseFileDefault() {
        databaseFile = "captureReplay.db";
        instance.closeConnection();
        instance = new DBUtil(databaseFile);
    }

    private void createNewCaptureTable(String databaseFile)
    {

        try
        {
            String sql = "CREATE TABLE IF NOT EXISTS captures(\n"
                    + " dbId INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                    + " id TEXT UNIQUE,\n"
                    + " rds TEXT,\n"
                    + " rdsRegion TEXT, \n"
                    + " s3 TEXT,\n"
                    + " s3Region TEXT, \n"
                    + " startTime TEXT,\n"
                    + " endTime TEXT,\n"
                    + " status TEXT,\n"
                    + " fileSizeLimit INTEGER,\n"
                    + " transactionLimit INTEGER,\n"
                    + " dbFileSize INTEGER,\n"
                    + " numDbTransactions INTEGER\n"
                    + ");";

            //Connection conn = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);
            java.sql.Statement stmt = conn.createStatement();

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
                    + " captureId INTEGER,\n"
                    + " id TEXT UNIQUE,\n"
                    + " rds TEXT,\n"
                    + " rdsRegion TEXT, \n"
                    + " s3 TEXT,\n"
                    + " s3Region TEXT, \n"
                    + " startTime TEXT,\n"
                    + " endTime TEXT,\n"
                    + " status TEXT,\n"
                    + " type TEXT,\n"
                    + " FOREIGN KEY(captureId) REFERENCES captures(dbId)\n"
                    + ");";

            //Connection conn = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);
            java.sql.Statement stmt = conn.createStatement();

            stmt.execute(sql);
            //conn.close();
            stmt.close();
        }

        catch (SQLException e)
        {
            System.err.println(e.getMessage());
        }
    }

    private void checkForFailedCaptures() {
        try
        {
            ResultSet rs;
            String sql = "SELECT * FROM captures WHERE status in ('Running', 'Queued') AND startTime < CURRENT_TIMESTAMP";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.execute();
            rs = pstmt.getResultSet();

            while (rs.next()) {
                String updateSql = "UPDATE captures SET status = 'Failed' WHERE dbId = ?";

                PreparedStatement updatePstmt = conn.prepareStatement(updateSql);
                updatePstmt.setInt(1, rs.getInt(1));
                updatePstmt.executeUpdate();

                updatePstmt.close();
            }

            pstmt.close();
        }

        catch (SQLException e)
        {
            System.err.println(e.getMessage());
        }
    }

    private void checkForFailedReplays() {
        try
        {
            ResultSet rs;
            String sql = "SELECT * FROM replays WHERE status in ('Running', 'Queued') AND startTime < CURRENT_TIMESTAMP";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.execute();
            rs = pstmt.getResultSet();

            while (rs.next()) {
                String updateSql = "UPDATE replays SET status = 'Failed' WHERE dbId = ?";

                PreparedStatement updatePstmt = conn.prepareStatement(updateSql);
                updatePstmt.setInt(1, rs.getInt(1));
                updatePstmt.executeUpdate();

                updatePstmt.close();
            }

            pstmt.close();
        }

        catch (SQLException e)
        {
            System.err.println(e.getMessage());
        }
    }

    public boolean checkCaptureNameDuplication(String name) {
        String sql = "SELECT id FROM captures WHERE id = ?";
        return checkNameDuplication(sql, name);
    }

    public boolean checkReplayNameDuplication(String name) {
        String sql = "SELECT id FROM replays WHERE id = ?";
        return checkNameDuplication(sql, name);
    }

    private boolean  checkNameDuplication(String sql, String name) {
        try
        {
            ResultSet rs;

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.execute();

            rs = pstmt.getResultSet();

            if (!rs.next()) {
                rs.close();
                pstmt.close();
                return false;
            }
            pstmt.close();
            return true;
        }
        catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    public boolean saveCapture (Capture capture)
    {
        //inserts values, if value is there then it's replaced
        String sql = "INSERT OR REPLACE INTO captures(id, rds, rdsRegion, s3, s3Region, startTime, endTime, status, " +
                "fileSizeLimit, transactionLimit, dbFileSize, numDbTransactions) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?, ?, ?)";

        try
        {
            Timestamp startTime = capture.getStartTime() != null ? new Timestamp(capture.getStartTime().getTime()) : null;
            Timestamp endTime = capture.getEndTime() != null ? new Timestamp(capture.getEndTime().getTime()) : null;

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, capture.getId());
            pstmt.setString(2, capture.getRds());
            pstmt.setString(3, capture.getRdsRegion());
            pstmt.setString(4, capture.getS3());
            pstmt.setString(5, capture.getS3Region());
            pstmt.setTimestamp(6, startTime);
            pstmt.setTimestamp(7, endTime);
            pstmt.setString(8, capture.getStatus());
            pstmt.setInt(9, capture.getFileSizeLimit());
            pstmt.setInt(10, capture.getTransactionLimit());
            pstmt.setLong(11, capture.getDbFileSize());
            pstmt.setInt(12, capture.getTransactionCount());
            pstmt.executeUpdate();

            pstmt.close();


            if (capture.getId() == null) //TODO: last row id implementation
            {
                java.sql.Statement stmt = conn.createStatement();
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
            capture.setRdsRegion(rs.getString(4));
            capture.setS3(rs.getString(5));
            capture.setS3Region(rs.getString(6));
            capture.setStartTime(rs.getTimestamp(7));
            capture.setEndTime(rs.getTimestamp(8));
            capture.setStatus(rs.getString(9));
            capture.setFileSizeLimit(rs.getInt(10));
            capture.setDbFileSize(rs.getInt(11));
            capture.setTransactionCount(rs.getInt(12));

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
                capture.setRdsRegion(rs.getString(4));
                capture.setS3(rs.getString(5));
                capture.setS3Region(rs.getString(6));
                capture.setStartTime(rs.getTimestamp(7));
                capture.setEndTime(rs.getTimestamp(8));
                capture.setStatus(rs.getString(9));
                capture.setFileSizeLimit(rs.getInt(10));
                capture.setDbFileSize(rs.getInt(11));
                capture.setTransactionCount(rs.getInt(12));

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

    public ArrayList<Capture> loadCapturesWithStatus(String status)
    {
        status = StringUtils.capitalize(status);
        if (!(status.equals("Finished") || status.equals("Running") || status.equals("Queued") || status.equals("Failed"))) {
            return null;
        }

        try
        {
            ResultSet rs;
            ArrayList<Capture> captures = new ArrayList<>();

            String sql = "SELECT * FROM captures WHERE status = '" + status + "'";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.execute();
            rs = pstmt.getResultSet();

            while (rs.next()) {
                Capture capture = new Capture();
                capture.setId(rs.getString(2));
                capture.setRds(rs.getString(3));
                capture.setRdsRegion(rs.getString(4));
                capture.setS3(rs.getString(5));
                capture.setS3Region(rs.getString(6));
                capture.setStartTime(rs.getTimestamp(7));
                capture.setEndTime(rs.getTimestamp(8));
                capture.setStatus(rs.getString(9));
                capture.setFileSizeLimit(rs.getInt(10));
                capture.setDbFileSize(rs.getInt(11));
                capture.setTransactionCount(rs.getInt(12));

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

    public boolean saveReplay(Replay replay)
    {
        //inserts values, if value is there then it's replaced
        String sql = "INSERT OR REPLACE INTO replays(captureId, id, rds, rdsRegion, s3, s3Region, startTime, endTime, status, type) " +
                "VALUES (?,?,?,?,?,?,?,?, ?, ?)";

        try
        {
            Timestamp startTime = replay.getStartTime() != null ? new Timestamp(replay.getStartTime().getTime()) : null;
            Timestamp endTime = replay.getEndTime() != null ? new Timestamp(replay.getEndTime().getTime()) : null;

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, getCaptureID(replay.getCaptureId()));
            pstmt.setString(2, replay.getId());
            pstmt.setString(3, replay.getRds());
            pstmt.setString(4, replay.getRdsRegion());
            pstmt.setString(5, replay.getS3());
            pstmt.setString(6, replay.getS3Region());
            pstmt.setTimestamp(7, startTime);
            pstmt.setTimestamp(8, endTime);
            pstmt.setString(9, replay.getStatus());
            pstmt.setString(10, replay.getReplayType());
            pstmt.executeUpdate();

            pstmt.close();


            if (replay.getId() == null) //TODO: last row id implementation
            {
                java.sql.Statement stmt = conn.createStatement();
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

            replay.setCaptureId(getCaptureName(rs.getInt(2)));
            replay.setId(rs.getString(3));
            replay.setRds(rs.getString(4));
            replay.setRdsRegion(rs.getString(5));
            replay.setS3(rs.getString(6));
            replay.setS3Region(rs.getString(7));
            replay.setStartTime(rs.getDate(8));
            replay.setEndTime(rs.getDate(9));
            replay.setStatus(rs.getString(10));
            replay.setReplayType(rs.getString(11));

            return replay;
        }

        catch (SQLException e)
        {
            System.err.println(e.getMessage());
            return null;
        }
    }

    public ArrayList<Replay> loadReplaysOfCaptureID(String id)
    {
        try
        {
            ArrayList<Replay> replays = new ArrayList<>();
            ResultSet rs;

            String sql = "SELECT * FROM replays WHERE captureId = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, getCaptureID(id));
            pstmt.execute();

            rs = pstmt.getResultSet();

            while (rs.next()) {
                Replay replay = new Replay();
                replay.setCaptureId(getCaptureName(rs.getInt(2)));
                replay.setId(rs.getString(3));
                replay.setRds(rs.getString(4));
                replay.setRdsRegion(rs.getString(5));
                replay.setS3(rs.getString(6));
                replay.setS3Region(rs.getString(7));
                replay.setStartTime(rs.getDate(8));
                replay.setEndTime(rs.getDate(9));
                replay.setStatus(rs.getString(10));
                replay.setReplayType(rs.getString(11));

                replays.add(replay);
            }

            return replays;
        }

        catch (SQLException e)
        {
            System.out.println("1");
            System.err.println(e.getMessage());
            System.out.println("2");
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
                replay.setCaptureId(getCaptureName(rs.getInt(2)));
                replay.setId(rs.getString(3));
                replay.setRds(rs.getString(4));
                replay.setRdsRegion(rs.getString(5));
                replay.setS3(rs.getString(6));
                replay.setS3Region(rs.getString(7));
                replay.setStartTime(rs.getDate(8));
                replay.setEndTime(rs.getDate(9));
                replay.setStatus(rs.getString(10));
                replay.setReplayType(rs.getString(11));

                replays.add(replay);
            }

            return replays;
        }

        catch (SQLException e)
        {
            System.err.println(e.getMessage());
            return null;
        }
    }

    public ArrayList<Replay> loadReplaysWithStatus(String status)
    {
        status = StringUtils.capitalize(status);
        if (!(status.equals("Finished") || status.equals("Running") || status.equals("Queued") || status.equals("Failed"))) {
            return null;
        }

        try
        {
            ResultSet rs;
            ArrayList<Replay> replays = new ArrayList<>();

            //String sql = "SELECT * FROM captures WHERE status = '" + status + "'";
            String sql = "SELECT * FROM replays WHERE status = '" + status + "'";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.execute();
            rs = pstmt.getResultSet();

            while (rs.next()) {
                Replay replay = new Replay();
                replay.setCaptureId(getCaptureName(rs.getInt(2)));
                replay.setId(rs.getString(3));
                replay.setRds(rs.getString(4));
                replay.setRdsRegion(rs.getString(5));
                replay.setS3(rs.getString(6));
                replay.setS3Region(rs.getString(7));
                replay.setStartTime(rs.getDate(8));
                replay.setEndTime(rs.getDate(9));
                replay.setStatus(rs.getString(10));
                replay.setReplayType(rs.getString(11));
                replays.add(replay);
            }

            return replays;
        }

        catch (SQLException e)
        {
            System.err.println(e.getMessage());
            return null;
        }
    }

    public int getCaptureID(String captureName) {
        try
        {
            ResultSet rs;

            String sql = "SELECT dbId FROM captures WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, captureName);
            pstmt.execute();
            rs = pstmt.getResultSet();

            return rs.getInt(1);
        }

        catch (SQLException e)
        {
            System.err.println(e.getMessage());
            return -1;
        }
    }

    public String getCaptureName(int captureId) {
        try
        {
            ResultSet rs;

            String sql = "SELECT id FROM captures WHERE dbId = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, captureId);
            pstmt.execute();
            rs = pstmt.getResultSet();

            return rs.getString(1);
        }

        catch (SQLException e)
        {
            System.err.println(e.getMessage());
            return null;
        }
    }

    public boolean deleteCapture (String id)
    {
        try
        {
            ArrayList<Replay> replays = loadReplaysOfCaptureID(id);
            for (Replay replay : replays) {
                deleteReplay(replay.getId());                
            }
            String sql = "DELETE FROM captures WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.execute();

            return true;
        }

        catch (SQLException e)
        {
            System.err.println(e.getMessage());
            return false;
        }
    }

    public boolean deleteReplay (String id)
    {
        try
        {
            String sql = "DELETE FROM replays WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.execute();

            return true;
        }

        catch (SQLException e)
        {
            System.err.println(e.getMessage());
            return false;
        }
    }
}

