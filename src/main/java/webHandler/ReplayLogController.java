package webHandler;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class ReplayLogController extends LogController {
    public static final int FAST_MODE = 0;
    public static final int TIME_SENSITIVE = 1;

    public ReplayLogController(Replay replay) {
        this.logFilter = new ReplayFilter(replay);
    }

    @Override
    public String getLogData(String resourceName, String fileName) {
        S3Manager s3Manager = new S3Manager();
        return s3Manager.getFileAsString(resourceName, fileName);
    }

    public void processData(Session session, int type) {
        Replay replay = (Replay) session;
        String logData = getLogData(replay.getS3(), replay.getLogFileName());

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

    private void replayFastMode(Replay replay, List<Statement> statements) {
        MySQLManager manager = new MySQLManager(replay.getDBUrl(),
                replay.getDatabase(),
                replay.getDBUsername(),
                replay.getDBPassword());

        try
        {
            for (Statement statement : statements) {
                if (!statement.getCommand().equals("Connect")) {
                    manager.query(statement.getQuery());
                }

            }
        }
        finally
        {
            manager.closeConnection();
        }

        ReplayServlet servlet = new ReplayServlet();
        servlet.stopReplay(replay);

    }

    // TODO: Fix time sensitive replay using SimpleDateFormat (look at CaptureFilter isWithinTimeInterval function)
    private void replayTimeSensitive(Replay replay, List<Statement> statements) {
        MySQLManager manager = new MySQLManager(replay.getDBUrl(),
                replay.getDatabase(),
                replay.getDBUsername(),
                replay.getDBPassword());

        long lastTime = Long.parseLong(statements.get(0).getTime());
        long currTime;
        try
        {
            for (Statement statement : statements) {
                currTime = Long.parseLong(statement.getTime());

                long toWait = currTime - lastTime;

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
        replay.setEndTime(new Date());
        ReplayServlet servlet = new ReplayServlet();
        servlet.stopReplay(replay);
    }

    public void uploadAllFiles(Session session)
    {
        uploadMetrics(session);
    }
}
