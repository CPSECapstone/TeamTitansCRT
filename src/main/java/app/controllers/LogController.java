package app.controllers;

import com.amazonaws.services.s3.model.ObjectMetadata;
import app.managers.CloudWatchManager;
import app.util.LogFilter;
import app.managers.S3Manager;
import app.models.Session;
import app.models.Statement;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

public abstract class LogController
{
    public static final String PerformanceTag = "-Performance.log";
    public static final String WorkloadTag = "-Workload.log";

    protected LogFilter logFilter;

    public abstract String getLogData(String resourceName, String region, String fileName);

    public static String getMetricsFromS3(String s3Bucket, String region, String fileName)
    {
        S3Manager s3Manager = new S3Manager(region);
        return s3Manager.getFileAsString(s3Bucket, fileName);
    }

    public List<Statement> filterLogData(String logData)
    {
        return logFilter.filterLogData(logData);
    }

    public abstract void processData(Session session, boolean type);

    public abstract void updateSessionController();

    public void updateLogFilter(Session session) {
        logFilter.update(session);
    }

    public abstract void uploadAllFiles(Session session);

    public static String getMetricsFromCloudWatch(String rds, String region, Date startTime, Date endTime)
    {
        CloudWatchManager cloudManager = new CloudWatchManager(region);
        return cloudManager.getAllMetricStatisticsAsJson(rds, startTime, endTime);
    }

    public void uploadMetrics(Session session)
    {
        String stats = getMetricsFromCloudWatch(session.getRds(), session.getRdsRegion(), session.getStartTime(), session.getEndTime());
        InputStream statStream = new ByteArrayInputStream(stats.getBytes(StandardCharsets.UTF_8));
        uploadInputStream(session.getS3(), session.getRdsRegion(),session.getId() + PerformanceTag, statStream);
    }

    public void uploadInputStream(String s3, String region, String fileName, InputStream stream)
    {
        S3Manager s3Manager = new S3Manager(region);
        s3Manager.uploadFile(s3, fileName, stream, new ObjectMetadata());
    }

    public void uploadFile(String s3, String region, String fileName, File file)
    {
        S3Manager s3Manager = new S3Manager(region);
        s3Manager.uploadFile(s3, fileName, file);
    }
}
