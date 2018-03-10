package webHandler;

import com.amazonaws.services.s3.model.ObjectMetadata;
import sun.misc.Perf;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public abstract class LogController
{
    protected final String PerformanceTag = "-Performance.log";
    protected final String WorkloadTag = "-Workload.log";

    protected LogFilter logFilter;

    public abstract String getLogData(String resourceName, String fileName);

    public List<Statement> filterLogData(String logData)
    {
        return logFilter.filterLogData(logData);
    }

    public abstract void processData(Session session, int type);

    public abstract void updateSessionController();

    public void updateLogFilter(Session session) {
        logFilter.update(session);
    }

    public abstract void uploadAllFiles(Session session);

    public void uploadMetrics(Session session)
    {
        CloudWatchManager cloudManager = new CloudWatchManager();
        String stats = cloudManager.getAllMetricStatisticsAsJson(session.getRds(), session.getStartTime(),
                session.getEndTime());
        InputStream statStream = new ByteArrayInputStream(stats.getBytes(StandardCharsets.UTF_8));
        uploadInputStream(session.getS3(), session.getId() + PerformanceTag, statStream);
    }

    public void uploadInputStream(String s3, String fileName, InputStream stream)
    {
        S3Manager s3Manager = new S3Manager();
        s3Manager.uploadFile(s3, fileName, stream, new ObjectMetadata());
    }

    public void uploadFile(String s3, String fileName, File file)
    {
        S3Manager s3Manager = new S3Manager();
        s3Manager.uploadFile(s3, fileName, file);
    }
}
