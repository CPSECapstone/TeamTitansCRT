package webHandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBUtil {

    public static Connection connectSqlite(String databaseFile)
    {
        Connection conn = null;
        try {

            conn = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);
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
}
