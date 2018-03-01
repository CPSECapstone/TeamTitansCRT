package webHandler;

import javax.xml.transform.Result;
import java.sql.*;
import java.util.ArrayList;

public class DBUtil {

    Connection conn;
    private String databaseFile;
    /**
     * Connect to a database
     *
     * @param databaseFile the database file name
     */

    public DBUtil(String databaseFile)
    {
        this.conn = connectSqlite(databaseFile);
    }

    public static Connection connectSqlite(String databaseFile)
    {
        Connection conn = null;
        try {

            conn = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);
            //conn = DriverManager.getConnection("jdbc:sqlite:/Users/devin/chinook.db");
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
        }

        catch(SQLException e)
        {
            // if the error message is "out of memory",
            // it probably means no database file is found
            System.err.println(e.getMessage());
        }
        finally
        {
            try
            {
                if(conn != null)
                    conn.close();
            }
            catch(SQLException e)
            {
                // connection close failed.
                System.err.println(e);
            }
        }
        return conn;
    }

    public Connection getConnection()
    {
        return conn;
    }

    public void closeConnection() throws SQLException {
        if (conn != null)
        {
            conn.close();
        }
    }

    public static void createNewTable(String databaseFile) throws SQLException
    {

        String sql = "CREATE TABLE IF NOT EXISTS captures(\n"
                +  " id INTEGER PRIMARY KEY,\n"
                + " rds TEXT,\n"
                + " s3 TEXT,\n"
                + " startTime TEXT,\n"
                + " endTime TEXT,\n"
                + " status TEXT,\n"
                + " fileSizeLimit INTEGER,\n"
                + " transactionLimit INTEGER,\n"
                + " dbFileSize INTEGER,\n"
                + " numDbTransactions INTEGER\n"
                +");";

        Connection conn = DriverManager.getConnection(databaseFile);
        Statement stmt = conn.createStatement();

        stmt.execute(sql);
        conn.close();
        stmt.close();
    }


    public void saveCapture (Capture capture) throws SQLException
    {
        //inserts values, if value is there then it's replaced
        String sql = "INSERT OR REPLACE INTO captures(id, rds, s3, startTime, endTime, status, " +
                "fileSizeLimit, transactionLimit, dbFileSize, numDbTransactions) " +
                "VALUES (?,?,?,?,?,?,?,?,?)";

        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setLong(1, Long.parseLong(capture.getId()));
        pstmt.setString(2, capture.getRds());
        pstmt.setString(3, capture.getS3());
        pstmt.setTimestamp(4, new Timestamp(capture.getStartTime().getTime()));
        pstmt.setTimestamp(5, new Timestamp(capture.getEndTime().getTime()));
        pstmt.setString(6, capture.getStatus());
        pstmt.setInt(7, capture.getFileSizeLimit());
        pstmt.setInt(8, capture.getDbFileSize());
        pstmt.setInt(9, capture.getNumDBTransactions());
        pstmt.executeUpdate();

        pstmt.close();

        if (capture.getId() == null) //TODO: last row id implementation
        {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid");
            rs.next();
            long id = rs.getLong(1);
            capture.setId(Long.toString(id));
            pstmt.close();
        }
    }

    public ArrayList<Capture> loadAllCaptures() throws SQLException
    {
        ResultSet rs;
        ArrayList<Capture> captures = new ArrayList<>();

        String sql = "SELECT * FROM captures";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.execute();
        rs = pstmt.getResultSet();

        while (rs.next())
        {
            Capture capture = new Capture();
            capture.setId(Long.toString(rs.getLong(1)));
            capture.setRds(rs.getString(2));
            capture.setS3(rs.getString(3));
            capture.setStartTime(rs.getDate(4));
            capture.setEndTime(rs.getDate(5));
            capture.setStatus(rs.getString(6));
            capture.setFileSizeLimit(rs.getInt(7));
            capture.setDbFileSize(rs.getInt(8));
            capture.setNumDBTransactions(rs.getInt(9));

            captures.add(capture);
        }

        return captures;
    }

    public Capture loadCapture (String id) throws SQLException
    {
        Capture capture = new Capture();
        ResultSet rs;

        String sql = "SELECT * FROM captures WHERE id = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setLong(1, Long.parseLong(id));
        pstmt.execute();

        rs = pstmt.getResultSet();

        if (!rs.next())
        {
            rs.close();
            pstmt.close();
            return null;
        }

        capture.setId(Long.toString(rs.getLong(1)));
        capture.setRds(rs.getString(2));
        capture.setS3(rs.getString(3));
        capture.setStartTime(rs.getDate(4));
        capture.setEndTime(rs.getDate(5));
        capture.setStatus(rs.getString(6));
        capture.setFileSizeLimit(rs.getInt(7));
        capture.setDbFileSize(rs.getInt(8));
        capture.setNumDBTransactions(rs.getInt(9));

        return capture;
    }

    /*public static void main (String[] args)
    {
        connectSqlite("/Users/devin/test.db");
    }*/
}

