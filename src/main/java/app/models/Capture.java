package app.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Capture implements Session {
    private final int NO_LIMIT = 0;

    private String id;
    private String rds;
    private String s3;
    private Date startTime;
    private Date endTime;
    private String status;

    private int fileSizeLimit = NO_LIMIT;
    private int transactionLimit = NO_LIMIT;

    private long dbFileSize = 0;
    private int transactionCount = 0;

    private List<String> filterStatements;
    private List<String> filterUsers;


    public Capture() {

    }

    public Capture(String id, String rds, String s3) {
        this.id = id;
        this.rds = rds;
        this.s3 = s3;
        this.startTime = new Date();
        this.endTime = null;
        updateStatus();
    }

    public Capture(String id, String rds, String s3, int fileSizeLimit, int transactionLimit) {
        this.id = id;
        this.rds = rds;
        this.s3 = s3;
        this.startTime = new Date();
        this.endTime = null;
        setFileSizeLimit(fileSizeLimit);
        setTransactionLimit(transactionLimit);
        updateStatus();
    }

    public Capture(String id, String rds, String s3, Date startTime, Date endTime) {
        this.id = id;
        this.rds = rds;
        this.s3 = s3;
        this.startTime = startTime;
        this.endTime = endTime;
        updateStatus();
    }

    public Capture(String id, String rds, String s3, Date startTime, Date endTime, int fileSizeLimit, int transactionLimit) {
        this.id = id;
        this.rds = rds;
        this.s3 = s3;
        this.startTime = startTime;
        this.endTime = endTime;
        setFileSizeLimit(fileSizeLimit);
        setTransactionLimit(transactionLimit);
        updateStatus();
    }

    public void updateStatus() {
        Date currTime = new Date();

        if("Failed".equals(status)) {
            return;
        }

        if (startTime != null && currTime.compareTo(startTime) >= 0) {
            if ((endTime != null && currTime.compareTo(endTime) >= 0)
                    || (fileSizeLimit != NO_LIMIT && dbFileSize > fileSizeLimit)
                    || (transactionLimit != NO_LIMIT && transactionCount > transactionLimit)) {
                this.status = "Finished";
            } else {
                this.status = "Running";
            }
        } else {
            this.status = "Queued";
        }
    }

    public boolean hasReachedFileSizeLimit() {

        return this.fileSizeLimit == NO_LIMIT ? false : this.dbFileSize >= this.fileSizeLimit;
    }

    public boolean hasReachedTransactonLimit() {
        return this.transactionLimit == NO_LIMIT ? false : this.transactionCount >= this.transactionLimit;
    }

    public String getId() { return id; }

    public void setId(String name) { this.id = name; }

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

    public void setFileSizeLimit(int size) {this.fileSizeLimit = size <= 0 ? NO_LIMIT : size; }

    public int getTransactionLimit() {return this.transactionLimit; }

    public void setTransactionLimit(int size) {this.transactionLimit = size <= 0 ? NO_LIMIT : size; }

    public long getDbFileSize() { return this.dbFileSize; }

    public void setDbFileSize(long size) {this.dbFileSize = size; }

    public int getTransactionCount() {return this.transactionCount; }

    public void setTransactionCount(int num) {this.transactionCount = num; }

    public boolean hasTransactionLimit() {return this.getTransactionLimit() > NO_LIMIT; }

    public boolean hasFileSizeLimit() {return this.getFileSizeLimit() > NO_LIMIT; }

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
}
