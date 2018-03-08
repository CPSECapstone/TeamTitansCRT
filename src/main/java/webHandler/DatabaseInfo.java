package webHandler;

public class DatabaseInfo {

    private String dbUrl;
    private String databse;
    private String username;
    private String password;

    public DatabaseInfo(String dbUrl, String databse, String username, String password) {
        this.dbUrl = dbUrl;
        this.databse = databse;
        this.username = username;
        this.password = password; // Should this be encrypted?
    }

    public String getDbUrl() {
        return this.dbUrl;
    }

    public String getDatabse() {
        return this.databse;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

}
