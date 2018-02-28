public class MySQLManager extends DatabaseManager {

    public MySQLManager(String dbURL, String database, String username, String password)
    {
        super("mysql","com.mysql.jdbc.Driver", dbURL, database, username, password);
    }
}
