package webHandler;

import java.util.Date;
import java.util.List;

public class MetricRequest {

    private String id;
    private Date startTime;
    private Date endTime;
    private String metric;

   
    public MetricRequest() {

    }

    public MetricRequest(String id, Date startTime, Date endTime, String metric) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.metric = metric;
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
    
    public String getMetric() {
        return metric;
    }
    
    public void setMetric(String metric) {
        this.metric = metric;
    }
}