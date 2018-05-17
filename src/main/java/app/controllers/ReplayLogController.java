package app.controllers;

import app.managers.MySQLManager;
import app.models.Capture;
import app.models.Replay;
import app.models.Session;
import app.models.Statement;
import app.servlets.ReplayServlet;
import app.util.DBUtil;
import app.util.ReplayFilter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ReplayLogController extends LogController {
    public static final boolean FAST_MODE = false;
    public static final boolean TIME_SENSITIVE = true;

    public ReplayLogController(Replay replay) {
        this.logFilter = new ReplayFilter(replay);
    }

    @Override
    public String getLogData(String resourceName, String region, String fileName) {
        return getMetricsFromS3(resourceName, region, fileName);
    }

    public void processData(Session session, boolean type) {
        Replay replay = (Replay) session;
        Capture associatedCapture = DBUtil.getInstance().loadCapture(replay.getCaptureId());
        String logData = getLogData(associatedCapture.getS3(), associatedCapture.getS3Region(), replay.getCaptureLogFileName());

        List<Statement> filteredStatementList = filterLogData(logData);

        if (type == FAST_MODE) {
            replayFastMode(replay, filteredStatementList);
        } else if (type == TIME_SENSITIVE) {
            replayTimeSensitive(replay, filteredStatementList);
        }
        uploadAllFiles(session);
    }

    @Override
    public void updateSessionController() {

    }

    private void makeStopRequest(Replay replay)
    {
        if (isRunning(replay.getStatus()))
        {
            ReplayServlet servlet = new ReplayServlet();
            servlet.stopReplay(replay);
        }
    }

    private boolean isRunning(String status)
    {
        return status.equals("Running");
    }

    private void replayFastMode(Replay replay, List<Statement> statements) {
        try {
            Thread.sleep(180000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (statements.size() == 0)
        {
            makeStopRequest(replay);
            return;
        }
        MySQLManager manager = new MySQLManager(replay.getDBUrl(),
                replay.getDatabase(),
                replay.getDBUsername(),
                replay.getDBPassword());

        try
        {
            for (Statement statement : statements) {
                if (!replay.getStatus().equals("Running"))
                {
                    break;
                }
                if (!statement.getCommand().equals("Connect")) {
                    manager.query(statement.getQuery());
                }

            }
        }
        finally
        {
            manager.closeConnection();
        }
        makeStopRequest(replay);
    }

    private Date parseToDate(String dateString, String timeString)
    {
        String pattern = "yyMMdd HH:mm:ss";
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        Date date = null;
        try
        {
            date = formatter.parse(dateString + " " + timeString);
        }
        catch (ParseException pe)
        {
            pe.printStackTrace();
        }
        return date;
    }

    // TODO: Fix time sensitive replay using SimpleDateFormat (look at CaptureFilter isWithinTimeInterval function)
    private void replayTimeSensitive(Replay replay, List<Statement> statements) {
        if (statements.size() == 0)
        {
            makeStopRequest(replay);
            return;
        }
        MySQLManager manager = new MySQLManager(replay.getDBUrl(),
                replay.getDatabase(),
                replay.getDBUsername(),
                replay.getDBPassword());

        Date lastTime = parseToDate(statements.get(0).getDate(), statements.get(0).getTime());
        Date currTime;
        try
        {
            for (Statement statement : statements) {
                if (!replay.getStatus().equals("Running"))
                {
                    break;
                }
                currTime = parseToDate(statement.getDate(), statement.getTime());

                if (lastTime == null || currTime == null)
                {
                    lastTime = currTime;
                    continue;
                }

                long toWait = currTime.getTime() - lastTime.getTime();

                try {
                    Thread.sleep(toWait);
                    if (!statement.getCommand().equals("Connect"))
                    {
                        manager.query(statement.getQuery());
                    }
                }
                catch (InterruptedException ex) {
                    ex.printStackTrace();
                }

                lastTime = currTime;
            }
        }
        finally
        {
            manager.closeConnection();
        }
        makeStopRequest(replay);
    }

    public void uploadAllFiles(Session session)
    {
        uploadMetrics(session);
    }
}
