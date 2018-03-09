package webHandler;

import java.io.InputStream;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public class ReplayServlet {

    public final String WorkloadTag = "-Workload.log";

    @RequestMapping(value = "/replay/start", method = RequestMethod.POST)
    public ResponseEntity<String> startReplay(@RequestBody Replay replay, String replayType) {
        S3Manager s3Manager = new S3Manager();

        String filename = replay.getId() + WorkloadTag;
        String logData = s3Manager.getFileString(replay.getS3(), filename);

        ReplayController.getInstance().addReplay(replay);

        if (replay.getStartTime() == null)
        {
            replay.setStartTime(new Date());
        }

        LogFilter replayFilter = new ReplayFilter(replay.getFilterStatements(), replay.getFilterUsers());
        List<Statement> filteredStatementsList = replayFilter.filterLogData(logData);
        // TODO: Don't forget to handle statements with time and date being --:--:-- and ------ respectively accordingly

        switch (replayType) {
            case "Time Sensitive":
                replayTimeSensitive(replay, filteredStatementsList);
                break;
            case "Fast Mode":
                replayFastMode(replay, filteredStatementsList);
                break;
            default:
                System.out.println("Error --- Unknown replay type.");
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/replay/stop", method = RequestMethod.POST)
    public ResponseEntity<String> stopReplay(@RequestBody Replay replay)
    {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    // TODO: Fix time sensitive replay using SimpleDateFormat (look at CaptureFilter isWithinTimeInterval function)
    private void replayTimeSensitive(Replay replay, List<Statement> statements) {
        MySQLManager manager = new MySQLManager(replay.getDBUrl(),
                replay.getDatabase(),
                replay.getDBUsername(),
                replay.getDBPassword());

        long lastTime = Long.parseLong(statements.get(0).getTime());
        long currTime;

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

        manager.closeConnection();
        replay.setEndTime(new Date());
        ReplayController.getInstance().removeReplay(replay.getId());
    }

    private void replayFastMode(Replay replay, List<Statement> statements) {
        MySQLManager manager = new MySQLManager(replay.getDBUrl(),
                replay.getDatabase(),
                replay.getDBUsername(),
                replay.getDBPassword());

        for (Statement statement : statements) {
            if (!statement.getCommand().equals("Connect"))
            {
                manager.query(statement.getQuery());
            }

        }

        manager.closeConnection();
    }
}
