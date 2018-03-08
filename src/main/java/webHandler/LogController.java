package webHandler;

import com.amazonaws.services.s3.model.ObjectMetadata;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class LogController {

    private CaptureFilter logFilter;

    private String fileName;
    private String captureId;

    public LogController(Capture capture) {
        logFilter = new CaptureFilter(capture);

        this.fileName = capture.getId() + "-Workload.log";
        this.captureId = capture.getId();
    }

    public void logData(Capture capture, String logData, boolean isFirstWrite, boolean isFinalWrite) {
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
            updateCaptureController();
            writeToFile(filteredLogData, isFirstWrite, isFinalWrite);
        }
    }

    private void writeToFile(String logData, boolean isFirstWrite, boolean isFinalWrite) {
        BufferedWriter out = null;

        if (isFirstWrite) {
            logData = "[\n" + logData;
        }

        if (isFinalWrite) {
            logData += "\n]";
        }

        try {
            FileWriter fileWriter = new FileWriter(this.fileName, true);
            out = new BufferedWriter(fileWriter);
            out.write(logData);
            out.close();

            updateCaptureController();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File getFile() {
        File file = new File(this.fileName);
        if (file.isFile()) {
            return file;
        }
        return null;
    }

    private void updateCaptureController()
    {
        File file = getFile();
        if (file != null) {
            CaptureController.getInstance().updateCaptureFileSize(this.captureId, file.length());
        }
    }

    public void updateLogController(Capture capture)
    {
        logFilter.setEndTime(capture.getEndTime());
        logFilter.setTransactionLimit(capture.getTransactionLimit());
    }

}
