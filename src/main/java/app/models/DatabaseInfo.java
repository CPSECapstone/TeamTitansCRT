package app.models;

import app.managers.RDSManager;

public class DatabaseInfo {

    private String dbUrl;
    private String database;
    private String region;
    private String username;
    private String password;

    public DatabaseInfo() {
        
    }

    public DatabaseInfo(String dbUrl, String database, String region, String username, String password) {
        this.dbUrl = dbUrl;
        this.database = database;
        this.region = region;
        this.username = username;
        this.password = password; // Should this be encrypted?
    }

    public String getDbUrl() {
        return this.dbUrl;
    }

    public String getDatabase() {
        return this.database;
    }

    public String getRegion() { return this.region; }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public void setDatabase(String database) {
        this.database = database;
        updateDbUrl();
    }

    public void setRegion(String region) {
        this.region = region;
        updateDbUrl();
    }

    private void updateDbUrl() {
        if (getDbUrl() == null && getDatabase() != null && getRegion() != null) {
            RDSManager rdsManager = new RDSManager(getRegion());
            setDbUrl(rdsManager.getRDSInstanceUrl(getDatabase()));
        }
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
