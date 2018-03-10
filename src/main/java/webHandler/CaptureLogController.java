package webHandler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class CaptureLogController extends LogController
{
    public static final int HOURLY = 0;
    public static final int END = 1;

    public static final String GeneralLogFileName = "general/mysql-general-log";

    private String localFileName;
    private String sessionID;

    public CaptureLogController(Capture capture)
    {
        this.logFilter = new CaptureFilter((Session) capture);
        this.sessionID = capture.getId();
        this.localFileName = this.sessionID + WorkloadTag;
    }

    @Override
    public String getLogData(String resourceName, String fileName) {
        RDSManager rdsManager = new RDSManager();
        return rdsManager.downloadLog(resourceName, fileName);
    }

    public void processData(Session capture, int type)
    {
        String fileName = null;
        boolean isFinalWrite = true;

        if (type == HOURLY)
        {
            fileName = GeneralLogFileName + "." + getHourOfDayUTC();
            isFinalWrite = false;
        }
        else if (type == END)
        {
            fileName = GeneralLogFileName;
            isFinalWrite = true;
        }

        String logData = getLogData(capture.getRds(), fileName);
        if (logData == null)
        {
            return;
        }

        List<Statement> filteredStatementList = filterLogData(logData);
        List<String> filteredLogDataList = filteredStatementList.stream().
                map(stmt -> stmt.toString()).collect(Collectors.toList());
        String filteredLogData = String.join(",\n", filteredLogDataList);
        if (filteredLogData != null)
        {
            writeToFile(filteredLogData, isFinalWrite);
            updateSessionController();
        }
    }

    private int getHourOfDayUTC()
    {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    private void writeToFile(String logData, boolean isFinalWrite)
    {
        BufferedWriter out = null;

        boolean isFirstWrite = getFile() == null;

        if (isFirstWrite)
        {
            logData = "[\n" + logData;
        }
        if (isFinalWrite)
        {
            logData = logData + "\n]";
        }

        try
        {
            FileWriter fileWriter = new FileWriter(this.localFileName, true);
            out = new BufferedWriter(fileWriter);
            out.write(logData);
            out.close();

            updateSessionController();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public File getFile()
    {
        File file = new File(this.localFileName);
        return file.isFile() ? file : null;
    }

    public void deleteFile()
    {
        new File(localFileName).delete();
    }

    @Override
    public void updateSessionController() {
        // TODO: Look into needing this
        File file = getFile();
        if (file != null)
        {
            CaptureController.updateCaptureFileSize(sessionID, file.length());
        }
        CaptureController.updateCaptureTransactionCount(sessionID, logFilter.getTransactionCount());
    }

    @Override
    public void uploadAllFiles(Session capture)
    {
        uploadMetrics(capture);
        uploadFile(capture.getS3(), localFileName, getFile());
        deleteFile();
    }
}
