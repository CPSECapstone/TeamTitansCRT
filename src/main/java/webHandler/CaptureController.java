package webHandler;

import com.amazonaws.services.s3.model.ObjectMetadata;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;

public class CaptureController {

    private final String GeneralLogFileName = "general/mysql-general.log";

    private static CaptureController instance = null; // singleton instance

    private HashMap<String, Capture> captures;
    private HashMap<String, LogController> logControllers;
    private HashMap<String, TimerManager> timers;

    // Initializes the hash maps. Private constructor due to singleton class
    private CaptureController()
    {
        this.captures = new HashMap<>();
        this.logControllers = new HashMap<>();
        this.timers = new HashMap<>();
    }

    // return singleton instance
    public static CaptureController getInstance()
    {
        if (instance == null)
        {
            instance = new CaptureController();
        }
        return instance;
    }

    /*
   @return Returns true if successfully uploads, false otherwise
    */
    private boolean uploadLogToS3(Capture capture) {
        String fileName = capture.getId() + "-Workload.log";
        File workloadFile = new File(fileName);
        FileInputStream stream = null;

        try {
            stream = new FileInputStream(workloadFile);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            return false;
        }

        S3Manager s3Manager = new S3Manager();
        s3Manager.uploadFile(capture.getS3(), fileName, workloadFile);
        //s3Manager.uploadFile(capture.getS3(), fileName, stream, new ObjectMetadata());
        return true;
    }

    /*
@return Returns true if successfully uploads, false otherwise
*/
    private boolean uploadMetricsToS3(Capture capture) {
        CloudWatchManager cloudManager = new CloudWatchManager();
        String stats = cloudManager.getAllMetricStatisticsAsJson(capture.getRds(), capture.getStartTime(), capture.getEndTime());
        InputStream statStream = new ByteArrayInputStream(stats.getBytes(StandardCharsets.UTF_8));

        // Store RDS workload in S3
        S3Manager s3Manager = new S3Manager();
        s3Manager.uploadFile(capture.getS3(), capture.getId() + "-Performance.log", statStream, new ObjectMetadata());
        return true;
    }

    private void deleteFile(String id)
    {
        new File(id + "-Workload.log").delete();
    }


    public boolean uploadLogsAndMetricsToS3(Capture capture)
    {
        if (uploadLogToS3(capture) && uploadMetricsToS3(capture))
        {
            deleteFile(capture.getId());
            return true;
        }
        return false;
    }

    private void updateCapture(Capture updatedCapture) {
        Capture capture = getCapture(updatedCapture.getId());
        if (updatedCapture.getStartTime() != null)
        {
            capture.setStartTime(updatedCapture.getStartTime());
        }
        if (updatedCapture.getEndTime() != null)
        {
            capture.setEndTime(updatedCapture.getEndTime());
        }
        if (updatedCapture.getTransactionLimit() != 0)
        {
            capture.setTransactionLimit(updatedCapture.getTransactionLimit());
        }
        if (updatedCapture.getFileSizeLimit() != 0)
        {
            capture.setFileSizeLimit(updatedCapture.getFileSizeLimit());
        }
        capture.updateStatus();

        DBUtil.getInstance().saveCapture(capture);
    }

    private void updateLogController(Capture capture) {
        LogController logController = getLogController(capture.getId());
        logController.updateLogController(capture);
    }

    private void updateTimerController(Capture capture) {
        TimerManager timerManager = getTimer(capture.getId());
        timerManager.updateTimeManager(capture.getStartTime(), capture.getEndTime());
    }

    public void updateAll(Capture capture)
    {
        updateCapture(capture);
        updateLogController(capture);
        updateTimerController(capture);
    }

    public void updateCaptureFileSize(String id, long fileSize) {
        if (captures.containsKey(id)) {
            Capture capture = captures.get(id);
            capture.setDbFileSize(fileSize);
            capture.updateStatus();
            if (capture.hasReachedFileSizeLimit()) {
                stopCapture(id);
            }
        }
    }

    public void endCaptureResources(String id)
    {
        LogController logController = getLogController(id);
        TimerManager timerManager = getTimer(id);
        Capture capture = getCapture(id);

        if (logController != null && capture != null)
        {
            String logData = downloadLog(capture.getRds(), GeneralLogFileName);
            logController.logData(capture, logData, true);
            logControllers.remove(id);
        }
        if (timerManager != null)
        {
            timerManager.end();
            timers.remove(id);
        }
        captures.remove(id);
    }

    public void stopCapture(String id)
    {
        CaptureServlet servlet = new CaptureServlet();
        servlet.captureStop(id);
    }

    public void updateCaptureTransactionCount(String id, int count) {
        if (captures.containsKey(id)) {
            Capture capture = captures.get(id);
            capture.setTransactionCount(count);
            capture.updateStatus();
            if (capture.hasReachedTransactonLimit()) {
                stopCapture(id);
            }
        }
    }

    public String downloadLog(String rds, String logFileName)
    {
        RDSManager rdsManager = new RDSManager();
        return rdsManager.downloadLog(rds, logFileName);

    }

    public void writeHourlyLogFile(String id, int hour) {
        Capture capture = getCapture(id);
        LogController logController = getLogController(id);
        RDSManager rdsManager = new RDSManager();

        if (capture == null)
        {
            return;
        }

        String logFile = GeneralLogFileName + "." + hour;
        String logData = downloadLog(capture.getRds(), logFile);

        logController.logData(capture, logData, false);
    }

    public void addTimer(TimerManager timer, String id) {
        timers.put(id, timer);
    }

    private TimerManager getTimer(String id) {
        if (doesTimersTableContain(id)) {
            return timers.get(id);
        }
        return null;
    }

    public void addLogController(LogController controller, String id) {
        logControllers.put(id, controller);
    }

    private LogController getLogController(String id) {
        if (doesLogControllersTableContain(id)) {
            return logControllers.get(id);
        }
        return null;
    }

    public void addCapture(Capture capture) {
        captures.put(capture.getId(), capture);
    }

    public Capture getCapture(String id) {
        if (doesCapturesTableContain(id)) {
            return captures.get(id);
        }
        return null;
    }

    public boolean doesCapturesTableContain(String id) {
        if (captures.containsKey(id)) {
            return true;
        }

        return false;
    }

    public boolean doesLogControllersTableContain(String id) {
        if (logControllers.containsKey(id)) {
            return true;
        }

        return false;
    }

    public boolean doesTimersTableContain(String id) {
        if (timers.containsKey(id)) {
            return true;
        }

        return false;
    }

    public Collection<Capture> getAllCaptureValues()
    {
        return captures.values();
    }
}
