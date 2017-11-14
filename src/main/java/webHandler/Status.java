package webHandler;

public class Status {
    public String status;
    public StatusMetrics metrics;

    public Status(String status, StatusMetrics metrics) {
        this.status = status;
        this.metrics = metrics;
    }
}
