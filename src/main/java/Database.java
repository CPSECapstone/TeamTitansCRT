import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Database {
    String dbms;
    String dbURL;
    String database;
    String username;
    String password;

    public Database(String dbURL, String database, String username, String password)
    {
        this.dbms = "mysql";
        this.dbURL = dbURL;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    public Connection getConnection()
    {
        Connection conn = null;
        try
        {
            Class.forName("com.mysql.jdbc.Driver");

            conn = DriverManager.getConnection("jdbc:" + this.dbms + "://" +
                    this.dbURL + "/" + this.database, this.username, this.password);

        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (SQLException se)
        {
            se.printStackTrace();
        }
        return conn;
    }

    public boolean query(String sqlQuery)
    {
        Connection conn = getConnection();
        if (conn == null)
        {
            return false;
        }
        try
        {
            Statement stmt = conn.createStatement();
            stmt.execute(sqlQuery);
        }
        catch (SQLException se) {
            se.printStackTrace();
            return false;
        }
        return true;
    }

}
