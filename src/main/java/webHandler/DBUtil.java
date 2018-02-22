package webHandler;

import javax.xml.transform.Result;
import java.sql.*;

public class DBUtil {

    Connection conn;
    /**
     * Connect to a sample database
     *
     * @param databaseFile the database file name
     */
    public static Connection connectSqlite(String databaseFile)
    {
        Connection conn = null;
        try {

            conn = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);
            //conn = DriverManager.getConnection("jdbc:sqlite:/Users/devin/chinook.db");
            Statement stmt = conn.createStatement();

            // Enable WAL-mode transactions for concurrent writing.
            // See: https://www.sqlite.org/wal.html
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

    public static void createNewTable(String databaseFile) throws SQLException
    {

        String sql = "CREATE TABLE IF NOT EXISTS captures (\n"
                +  " id INTEGER PRIMARY KEY, \n"
                + " rds TEXT, \n"
                + " s3 TEXT, \n"
                + " startTime TEXT, \n"
                + " endTime TEXT, \n"
                + " status TEXT, \n"
                + " fileSizeLimit INTEGER, \n"
                + " transactionLimit INTEGER, \n"
                + " dbFileSize INTEGER, \n"
                + " numDbTransactions INTEGER \n"
                +");";

        Connection conn = DriverManager.getConnection(databaseFile);
        Statement stmt = conn.createStatement();

        stmt.execute(sql);
    }

    public void closeConnection() throws SQLException {
        if (conn != null)
        {
            conn.close();
        }
    }

    public void saveCapture (Capture capture) throws SQLException
    {
        String sql = "INSERT INTO captures(id) VALUES (?,?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setLong(1, Long.parseLong(capture.getId()));
        pstmt.executeUpdate();
    }

    public ResultSet loadCapture (String id) throws SQLException
    {
        String sql = "SELECT * FROM captures WHERE id = " + Long.parseLong(id);
        Statement stmt = conn.createStatement();
        ResultSet results = stmt.executeQuery(sql);


        return results;
    }

    /*public static void main (String[] args)
    {
        connectSqlite("/Users/devin/test.db");
    }*/
}
