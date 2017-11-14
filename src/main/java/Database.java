import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DriverManager;

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

    public Connection getConnection() throws SQLException
    {
        try
        {
            Class.forName("com.mysql.jdbc.Driver");
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }

        Connection conn = DriverManager.getConnection("jdbc:" + this.dbms + "://" +
                this.dbURL + "/" + this.database, this.username, this.password);
        return conn;
    }
}
