package app.managers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class DatabaseManager {
    private String dbms;
    private String dbURL;
    private String database;
    private String username;
    private String password;
    private String className;

    private Connection conn = null;

    public DatabaseManager(String dbms, String className, String dbURL, String database, String username,
                            String password)
    {
        this.dbms = dbms;
        this.dbURL = dbURL;
        this.database = database;
        this.username = username;
        this.password = password;
        this.className = className;
    }

    private Connection createConnection()
    {
        try
        {
            Class.forName(this.className);

            conn = DriverManager.getConnection("jdbc:" + this.dbms + "://" +
                    this.dbURL + "/" + this.database, this.username, this.password);

        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
            conn = null;
        }
        catch (SQLException se)
        {
            se.printStackTrace();
            conn = null;
        }
        return conn;
    }

    private Connection getConnection()
    {
        return conn == null ? createConnection() : conn;
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
        catch (SQLException se)
        {
            se.printStackTrace();
        }
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

    public boolean checkConnection() {
        try {
            return getConnection() != null;
        } finally {
            closeConnection();
        }
    }
}
