package webHandler;

import com.amazonaws.services.s3.model.ObjectMetadata;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
public class CaptureController {

    private final String GeneralLogFileName = "general/mysql-general.log";

    // Singleton
    public static CaptureController captureController = null;

    private HashMap<String, Capture> captures;
    private HashMap<String, LogController> logControllers;
    private HashMap<String, TimerManager> timers;

    private CaptureController() {
        captures = new HashMap<>();
        logControllers = new HashMap<>();
        timers = new HashMap<>();
    }

    public static CaptureController getInstance() {
        if (captureController == null) {
            captureController = new CaptureController();
        }
        return captureController;
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
        s3Manager.uploadFile(capture.getS3(), fileName, stream, new ObjectMetadata());
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

    private void updateCapture(Capture updatedCapture) {
        Capture capture = captures.get(updatedCapture.getId());
        capture.setStartTime(updatedCapture.getStartTime());
        capture.setEndTime(updatedCapture.getEndTime());
        capture.setTransactionLimit(updatedCapture.getTransactionLimit());
        capture.setFileSizeLimit(updatedCapture.getFileSizeLimit());
        capture.updateStatus();

        DBUtil.getInstance().saveCapture(capture);
    }

    private void updateLogController(Capture capture) {
        LogController logController = logControllers.get(capture.getId());
        logController.updateLogController(capture);
    }

    private void updateTimerController(Capture capture) {
        TimerManager timerManager = timers.get(capture.getId());
        timerManager.updateTimeManager(capture.getStartTime(), capture.getEndTime());
    }

    public void updateCaptureFileSize(String id, long fileSize) {
        if (captures.containsKey(id)) {
            Capture capture = captures.get(id);
            capture.setDbFileSize(fileSize);
            capture.updateStatus();
            if (capture.hasFileSizeLimit()) {
                captureStop(id);
            }
        }
    }

    public void updateCaptureTransactionCount(String id, int count) {
        if (captures.containsKey(id)) {
            Capture capture = captures.get(id);
            capture.setTransactionCount(count);
            capture.updateStatus();
            if (capture.hasReachedTransactonLimit()) {
                captureStop(id);
            }
        }
    }

    public void writeHourlyLogFile(String id, int hour) {
        Capture capture = captures.get(id);
        LogController logController = logControllers.get(id);
        RDSManager rdsManager = new RDSManager();

        String logFile = GeneralLogFileName + "." + hour;
        String logData = rdsManager.downloadLog(capture.getRds(),  logFile);

        logController.logData(capture, logData, false, false);
    }

    @RequestMapping(value = "/capture/start", method = RequestMethod.POST)
    public ResponseEntity<String> captureStart(@RequestBody Capture capture) {
        LogController logController = new LogController(capture);
        TimerManager timerManager = new TimerManager(capture.getId(), capture.getStartTime(), capture.getEndTime());

        if (capture.getId() == null || capture.getS3() == null || capture.getRds() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (capture.getStartTime() == null) {
            capture.setStartTime(new Date());
        }

        captures.put(capture.getId(), capture);
        logControllers.put(capture.getId(), logController);
        timers.put(capture.getId(), timerManager);

        if (!DBUtil.getInstance().saveCapture(capture)) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/capture/stop", method = RequestMethod.POST)
    public ResponseEntity<String> captureStop(@RequestBody String captureID) {

        // Send bad request on unknown capture ID
        if (!captures.containsKey(captureID)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Capture capture = captures.get(captureID);
        capture.setStatus("Finished");
        capture.setEndTime(new Date());

        uploadLogToS3(capture);
        uploadMetricsToS3(capture);

        if (!DBUtil.getInstance().saveCapture(capture)) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/capture/update", method = RequestMethod.POST)
    public ResponseEntity<String> captureUpdate(@RequestBody Capture capture) {
        if (!captures.containsKey(capture.getId())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        updateCapture(capture);
        updateLogController(capture);
        updateTimerController(capture);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/capture/status", method = RequestMethod.GET)
    public ResponseEntity<Collection<Capture>> captureStatus() {
        return new ResponseEntity<>(captures.values(), HttpStatus.OK);
    }
}
