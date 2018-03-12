package webHandler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Replay implements Session {

    private DatabaseInfo databaseInfo;

    private String id;
    private String rds;
    private String s3;
    private String status;
    private String logFileName;

    private Date startTime;
    private Date endTime;

    private List<String> filterStatements;
    private List<String> filterUsers;

    public Replay() {

    }

    public Replay(String id, String rds, String s3) {
        this.id = id;
        this.rds = rds;
        this.s3 = s3;
        this.logFileName = id + "-Workload.log";
        this.startTime = new Date();
        this.endTime = null;
        this.status = "Running";
    }

    public Replay(String id, String rds, String s3, Date startTime, Date endTime) {
        this.id = id;
        this.rds = rds;
        this.s3 = s3;
        this.logFileName = id + "-Workload.log";
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = "Running";
    }

    public void updateStatus() {
        Date currTime = new Date();
        if (startTime != null && currTime.compareTo(startTime) >= 0) {
            if ((endTime != null && currTime.compareTo(endTime) >= 0)) {
                this.status = "Finished";
            } else {
                this.status = "Running";
            }
        } else {
            this.status = "Queued";
        }
    }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getRds() {
        return rds;
    }

    public void setRds(String rds) {
        this.rds = rds;
    }

    public String getS3() {
        return s3;
    }

    public void setS3(String s3) {
        this.s3 = s3;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
        updateStatus();
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
        updateStatus();
    }

    public String getStatus() {
        updateStatus();
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getFilterStatements()
    {
        if (this.filterStatements == null)
        {
            this.filterStatements = new ArrayList<String>();
        }
        return this.filterStatements;
    }

    public void setFilterStatements(List<String> filterStatements)
    {
        this.filterStatements = filterStatements;
    }

    public List<String> getFilterUsers()
    {
        if (this.filterUsers == null)
        {
            this.filterUsers = new ArrayList<String>();
        }
        return this.filterUsers;
    }

    public void setFilterUsers(List<String> filterUsers)
    {
        this.filterUsers = filterUsers;
    }

    public String getDBUrl() {
        return this.databaseInfo.getDbUrl();
    }

    public String getDatabase() {
        return this.databaseInfo.getDatabase();
    }

    public String getDBUsername() {
        return this.databaseInfo.getUsername();
    }

    public String getDBPassword() {
        return this.databaseInfo.getPassword();
    }

    public void setDatabaseInfo(DatabaseInfo info) {
        this.databaseInfo = info;
    }

    public String getLogFileName() {
        return logFileName;
    }

    public int getTransactionLimit()
    {
        return 0;
    }
}
