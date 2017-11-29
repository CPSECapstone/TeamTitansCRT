package webHandler;

import java.util.Date;

public class Capture {
    private String id;
    private String rds;
    private String s3;
    private Date startTime;
    private Date endTime;
    private String status;

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

    public Capture(String id, String rds, String s3, Date startTime, Date endTime) {
        this.id = id;
        this.rds = rds;
        this.s3 = s3;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = "Running";
    }

    public void updateStatus() {
        Date currTime = new Date();
        if (startTime != null && startTime.compareTo(currTime) >= 0) {
            if (endTime != null && endTime.compareTo(currTime) <= 0) {
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
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
