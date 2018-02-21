package webHandler;

import com.amazonaws.services.s3.model.ObjectMetadata;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
public class CaptureController {

    // Number of minutes to milliseconds to wait before updating captures.
    private final int UPDATE_PERIOD = 1000 * 60 * 1;

    private HashMap<String, Capture> captures = new HashMap<>();

    @RequestMapping(value = "/capture/start", method = RequestMethod.POST)
    public ResponseEntity<String> captureStart(@RequestBody Capture capture) {

        if (capture.getId() == null || capture.getS3() == null || capture.getRds() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (capture.getStartTime() == null) {
            capture.setStartTime(new Date());
        }

        if (capture.getEndTime() != null) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    captureStop(capture);
                }
            }, capture.getEndTime());
        }

        if (capture.hasFileSizeLimit()) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    int fileSize = getFileSize(capture);
                    capture.setDbFileSize(fileSize);
                    if (fileSize > capture.getFileSizeLimit()) {
                        captureStop(capture);
                        cancel();
                    }
                }
            }, 0, UPDATE_PERIOD);
        }

        if (capture.hasTransactionLimit()) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    int numTransactions = getNumTransactions(capture);
                    capture.setNumDBTransactions(numTransactions);
                    if (numTransactions > capture.getTransactionLimit()) {
                        captureStop(capture);
                        cancel();
                    }
                }
            }, 0, UPDATE_PERIOD);
        }

        captures.put(capture.getId(), capture);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/capture/stop", method = RequestMethod.POST)
    public ResponseEntity<String> captureStop(@RequestBody Capture capture) {

        Capture targetCapture = captures.get(capture.getId());
        targetCapture.updateStatus();

        if (targetCapture == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (capture.getEndTime() == null) {
            targetCapture.setEndTime(new Date());
        } else {
            targetCapture.setEndTime(capture.getEndTime());
        }

        if (targetCapture.getStatus().equals("Finished")) {
            // Grab RDS workload
            RDSManager rdsManager = new RDSManager();
            String logData = rdsManager.downloadLog(targetCapture.getRds(),  "general/mysql-general.log");

            LogParser parser = new LogParser();

            String parsedLogData = parser.parseLogData(logData, capture.getFilterStatements(),
                    capture.getFilterUsers(), capture.getStartTime(), capture.getEndTime());

            InputStream stream = null;
            try
            {
                stream = new ByteArrayInputStream(parsedLogData.getBytes(StandardCharsets.UTF_8.name()));
            } catch (UnsupportedEncodingException enc) {
                enc.printStackTrace();
            }

            if (stream == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            CloudWatchManager cloudManager = new CloudWatchManager();
            String stats = cloudManager.getAllMetricStatisticsAsJson(targetCapture.getRds(), targetCapture.getStartTime(), targetCapture.getEndTime());
            InputStream statStream = new ByteArrayInputStream(stats.getBytes(StandardCharsets.UTF_8));

            // Store RDS workload in S3
            S3Manager s3Manager = new S3Manager();
            s3Manager.uploadFile(targetCapture.getS3(), targetCapture.getId() + "-Workload.log", stream, new ObjectMetadata());
            s3Manager.uploadFile(targetCapture.getS3(), targetCapture.getId() + "-Performance.log", statStream, new ObjectMetadata());

            //TODO: Add check for file upload
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/capture/status", method = RequestMethod.GET)
    public ResponseEntity<Collection<Capture>> captureStatus() {
        return new ResponseEntity<>(captures.values(), HttpStatus.OK);
    }

    private int getFileSize(Capture capture) {
        try {
            RDSManager rdsManager = new RDSManager();
            String logData = rdsManager.downloadLog(capture.getRds(), "general/mysql-general.log");
            String parsedLogData = new LogParser().parseLogData(logData, capture.getFilterStatements(),
                    capture.getFilterUsers(), capture.getStartTime(), capture.getEndTime());
            return parsedLogData.getBytes(StandardCharsets.UTF_8.name()).length;
        } catch(UnsupportedEncodingException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private int getNumTransactions(Capture capture) {
        try {
            RDSManager rdsManager = new RDSManager();
            String logData = rdsManager.downloadLog(capture.getRds(), "general/mysql-general.log");
            String parsedLogData = new LogParser().parseLogData(logData, capture.getFilterStatements(),
                    capture.getFilterUsers(), capture.getStartTime(), capture.getEndTime());
            return parsedLogData.length()  - parsedLogData.replace("\n", "").length();
        } catch(Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void updateCaptures() {
        for (Map.Entry<String, Capture> entry : captures.entrySet()) {
            entry.getValue().updateStatus();
        }
    }
}
