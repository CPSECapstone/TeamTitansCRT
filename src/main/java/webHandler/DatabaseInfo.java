package webHandler;

public class DatabaseInfo {

    private String dbUrl;
    private String database;
    private String username;
    private String password;

    public DatabaseInfo(String dbUrl, String database, String username, String password) {
        this.dbUrl = dbUrl;
        this.database = database;
        this.username = username;
        this.password = password; // Should this be encrypted?
    }

    public String getDbUrl() {
        return this.dbUrl;
    }

    public String getDatabase() {
        return this.database;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

}
