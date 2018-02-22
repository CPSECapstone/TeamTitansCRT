package webHandler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Capture {
    private final int NO_LIMIT = 0;

    private String id;
    private String rds;
    private String s3;
    private Date startTime;
    private Date endTime;
    private String status;

    private int fileSizeLimit = NO_LIMIT;
    private int transactionLimit = NO_LIMIT;

    private int dbFileSize = 0;
    private int numDBTransactions = 0;

    private List<String> filterStatements;
    private List<String> filterUsers;

    private LogController logController;

    public Capture() {

    }

    public Capture(String id, String rds, String s3) {
        this.id = id;
        this.rds = rds;
        this.s3 = s3;
        this.startTime = new Date();
        this.endTime = null;
        this.status = "Running";
    }

    public Capture(String id, String rds, String s3, int fileSizeLimit, int transactionLimit) {
        this.id = id;
        this.rds = rds;
        this.s3 = s3;
        this.startTime = new Date();
        this.endTime = null;
        this.fileSizeLimit = fileSizeLimit;
        this.transactionLimit = transactionLimit;
        this.status = "Running";
    }

    public Capture(String id, String rds, String s3, Date startTime, Date endTime) {
        this.id = id;
        this.rds = rds;
        this.s3 = s3;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = "Running";
    }

    public Capture(String id, String rds, String s3, Date startTime, Date endTime, int fileSizeLimit, int transactionLimit) {
        this.id = id;
        this.rds = rds;
        this.s3 = s3;
        this.startTime = startTime;
        this.endTime = endTime;
        this.fileSizeLimit = fileSizeLimit;
        this.transactionLimit = transactionLimit;
        this.status = "Running";
    }

    public void updateStatus() {
        Date currTime = new Date();
        if (startTime != null && currTime.compareTo(startTime) >= 0) {
            if ((endTime != null && currTime.compareTo(endTime) >= 0)
                    || (fileSizeLimit != NO_LIMIT && dbFileSize > fileSizeLimit)
                    || (transactionLimit != NO_LIMIT && numDBTransactions > transactionLimit)) {
                this.status = "Finished";
            } else {
                this.status = "Running";
            }
        } else {
            this.status = "Queued";
        }
    }

    public void startCaptureLogs() {
        if (logController == null) {
            this.logController = new LogController(this);
            this.logController.run();
        }
    }

    public void endCaptureLogs() {
        this.logController.end();
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

    public int getFileSizeLimit() { return this.fileSizeLimit; }

    public void setFileSizeLimit(int size) {this.fileSizeLimit = size; }

    public int getTransactionLimit() {return this.transactionLimit; }

    public void setTransactionLimit(int size) {this.transactionLimit = size; }

    public int getDbFileSize() { return this.dbFileSize; }

    public void setDbFileSize(int size) {this.dbFileSize = size; }

    public int getNumDBTransactions() {return this.numDBTransactions; }

    public void setNumDBTransactions(int num) {this.numDBTransactions = num; }

    public boolean hasTransactionLimit() {return this.getTransactionLimit() > NO_LIMIT; }

    public boolean hasFileSizeLimit() {return this.getFileSizeLimit() > NO_LIMIT; }

    public List<String> getFilterStatements()
    {
        if (this.filterStatements == null)
        {
            return new ArrayList<String>();
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
            return new ArrayList<String>();
        }
        return this.filterUsers;
    }

    public void setFilterUsers(List<String> filterUsers)
    {
        this.filterUsers = filterUsers;
    }
}
