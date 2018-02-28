package webHandler;

import com.amazonaws.services.s3.model.ObjectMetadata;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class LogController {

    private final int UPDATE_PERIOD_HOUR = 1000 * 60 * 60;
    private final String GeneralLogFileName = "general/mysql-general.log";

    private Capture capture;

    private RDSManager rdsManager;
    private S3Manager s3Manager;
    private LogFilter logFilter;

    private boolean isRunning = false;
    private boolean isFirstWrite = true;

    private String fileName;

    public LogController(Capture capture) {
        this.capture = capture;

        rdsManager = new RDSManager();
        s3Manager = new S3Manager();
        logFilter = new CaptureFilter(capture.getStartTime(), capture.getEndTime(), capture.getTransactionLimit(),
                capture.getFilterStatements(), capture.getFilterUsers());

        fileName = capture.getId() + "-Workload.log";
    }

    public void run() {
        if (isRunning) {
            return;
        }

        isRunning = true;

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        int currMinute = calendar.get(Calendar.MINUTE);
        int delay = UPDATE_PERIOD_HOUR - (1000 * 60 * currMinute);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                int hour = calendar.get(Calendar.HOUR_OF_DAY);

                if (isRunning) {
                    logData(GeneralLogFileName + "." + hour);
                } else {
                    cancel();
                }
                calendar.add(Calendar.HOUR, 1);
            }
        }, delay, UPDATE_PERIOD_HOUR);
    }

    public void end() {
        this.isRunning = false;
        logData(GeneralLogFileName);
        uploadToS3();
    }

    private void logData(String logFile) {
        String logData = rdsManager.downloadLog(capture.getRds(), logFile);

        if (logData ==  null) {
            return;
        }

        List<Statement> filteredStatementList = logFilter.filterLogData(logData);

        List<String> filteredLogDataList = filteredStatementList.stream().
                map(stmt -> stmt.toString()).collect(Collectors.toList());
        String filteredLogData = String.join(",\n", filteredLogDataList);
        InputStream stream = null;
        try {
            stream = new ByteArrayInputStream(filteredLogData.getBytes(StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException enc) {
            enc.printStackTrace();
        }

        if (stream != null) {
            writeToFile(filteredLogData);
        }
    }

    private void writeToFile(String logData) {
        BufferedWriter out = null;

        if (!isRunning) {
            logData += "\n]";
        } else if (isFirstWrite) {
            logData = "[\n" + logData;
            isFirstWrite = false;
        }

        try {
            FileWriter fileWriter = new FileWriter(this.fileName, true);
            out = new BufferedWriter(fileWriter);
            out.write(logData);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void uploadToS3() {
        try {
            File file = new File(this.fileName);
            s3Manager.uploadFile(capture.getS3(), fileName, new FileInputStream(file), new ObjectMetadata());
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
