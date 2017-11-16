package webHandler;

public class Status {
    public String status;
    public StatusMetrics metrics;

    public Status(String status, StatusMetrics metrics) {
        this.status = status;
        this.metrics = metrics;
    }

    public String getStatus() {
        return status;
    }

    public StatusMetrics getMetrics() {
        return metrics;
    }
}
