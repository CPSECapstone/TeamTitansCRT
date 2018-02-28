package webHandler;

import java.util.Date;
import java.util.List;

public class MetricRequest {

    private String id;
    private Date startTime;
    private Date endTime;
    private String[] metrics;
   
    public MetricRequest() {

    }

    public MetricRequest(String id, Date startTime, Date endTime, String... metrics) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.metrics = metrics;
    }
    
    public String getID() {
        return id;
    }
    
    public void setID (String id) {
        this.id = id;
    }

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