package app.controllers;

import app.managers.RDSManager;
import app.models.Capture;
import app.models.Session;
import app.models.Statement;
import app.util.CaptureFilter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class CaptureLogController extends LogController
{
    public static final boolean HOURLY = false;
    public static final boolean END = true;

    public static final String GeneralLogFileName = "general/mysql-general.log";

    private String localFileName = "";
    private String sessionID;

    private final int FILESIZEBUFFER = 50;
    private long fileSizeLimit;
    private boolean limitFlag = false;

    public CaptureLogController(Capture capture)
    {
        this.logFilter = new CaptureFilter((Session) capture);
        this.sessionID = capture.getId();
        this.fileSizeLimit = capture.getFileSizeLimit();
        this.localFileName = this.sessionID + WorkloadTag;
    }

    @Override
    public String getLogData(String resourceName, String region, String fileName) {
        RDSManager rdsManager = new RDSManager(region);
        return rdsManager.downloadLog(resourceName, fileName);
    }

    public void processData(Session capture, boolean type)
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

        String logData = getLogData(capture.getRds(), capture.getRdsRegion(), fileName);
        if (logData == null)
        {
            return;
        }

        List<Statement> filteredStatementList = filterLogData(logData);
        List<String> filteredLogDataList = filteredStatementList.stream().
                map(stmt -> stmt.toString()).collect(Collectors.toList());
        //String filteredLogData = String.join(",\n", filteredLogDataList);
        if (!filteredLogDataList.isEmpty())
        {
            writeToFile(filteredLogDataList, isFinalWrite);
            //writeToFile(filteredLogData, isFinalWrite);
            if (!type)
            {
                updateSessionController();
            }
        }
    }

    private int getHourOfDayUTC()
    {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    private void writeToFile(List<String> filteredLogData, boolean isFinalWrite)
    {
        if (limitFlag) {
            return;
        }
        BufferedWriter out = null;

        boolean isFirstWrite = getFile() == null;

        try
        {
            FileWriter fileWriter = new FileWriter(this.localFileName,true);
            out = new BufferedWriter(fileWriter);
            if (isFirstWrite)
            {
                out.write("[\n");
            }
            File tempFile = getFile();
            long fileSize = tempFile.length() + FILESIZEBUFFER;

            for (int i = 0; i < filteredLogData.size(); i++)
            {
                String statement = filteredLogData.get(i);
                long tempFileSize = fileSize + statement.length() + 2; // +2 accounts for ",\n"
                if (fileSizeLimit > 0 && (tempFileSize / 1000.0) >= fileSizeLimit)
                {
                    limitFlag = true;
                    out.write("\n]");
                    out.flush();
                    updateSessionController(true);
                    CaptureController.stopCapture(sessionID);
                    return;
                }
                else if (i != 0)
                {
                    out.write(",\n");
                    fileSize += 2;
                }
                out.write(statement);
                fileSize += statement.length();
                out.flush();
            }
            if (isFinalWrite)
            {
                out.write("\n]");
                out.flush();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            //TODO: Fail flag and upload files only if "Finished"
        }
        finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }
/*
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
*/
    public File getFile()
    {
        File file = new File(this.localFileName);
        return file.isFile() ? file : null;
    }

    public void deleteFile()
    {
        new File(localFileName).delete();
    }

    private void updateSessionController(boolean overrideFlag)
    {
        File file = getFile();
        if (file != null)
        {
            if (overrideFlag)
            {
                CaptureController.updateCaptureFileSize(sessionID, fileSizeLimit * 1001);
            }
            else
            {
                CaptureController.updateCaptureFileSize(sessionID, file.length());
            }
        }
        CaptureController.updateCaptureTransactionCount(sessionID, logFilter.getTransactionCount());
    }


    @Override
    public void updateSessionController() {
        // TODO: Look into needing this
       updateSessionController(false);
    }

    @Override
    public void uploadAllFiles(Session capture)
    {
        if (capture.getStatus() != "Finished")  {
            return;
        }
        uploadMetrics(capture);
        if (getFile() == null)
        {
            createDummyFile();
        }
        uploadFile(capture.getS3(), capture.getS3Region(), localFileName, getFile());
        deleteFile();
    }

    private void createDummyFile()
    {
        BufferedWriter out = null;
        try
        {
            FileWriter fileWriter = new FileWriter(this.localFileName,true);
            out = new BufferedWriter(fileWriter);
            out.write("[\n\n]");
            out.close();
            fileWriter.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
