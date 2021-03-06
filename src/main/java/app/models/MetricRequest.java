package app.models;

import java.util.Date;

public class MetricRequest {

    private String rds;
    private String rdsRegion;
    private Date startTime;
    private Date endTime;
    private String[] metrics;
   
    public MetricRequest() {

    }

    public MetricRequest(String rds, String rdsRegion, Date startTime, Date endTime, String... metrics) {
        this.rds = rds;
        this.rdsRegion = rdsRegion;
        this.startTime = startTime;
        this.endTime = endTime;
        this.metrics = metrics;
    }
    
    public String getRDS() {
        return rds;
    }
    
    public void setRDS (String rds) {
        this.rds = rds;
    }

    public String getRdsRegion() { return rdsRegion; }

    public void setRdsRegion(String rdsRegion) { this.rdsRegion = rdsRegion; }

    public Date getStartTime() {
        return startTime;
    }
    
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }
    
    public Date getEndTime() {
        return endTime;
    }
    
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
    
    public String[] getMetrics() {
        return metrics;
    }
    
    public void setMetrics(String... metrics) {
        this.metrics = metrics;
    }
}