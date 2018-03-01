package webHandler;

import java.io.InputStream;

import java.util.ArrayList;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public class ReplayController {

    public final String WorkloadTag = "-Workload.log";

    @RequestMapping(value = "/replay/start", method = RequestMethod.POST)
    public ResponseEntity<String> startReplay(@RequestBody Replay replay, String replayType) {
        S3Manager s3Manager = new S3Manager();
        InputStream statementsStream;
        ArrayList<Statement> statements;

        String filename = replay.getId() + WorkloadTag;
        statementsStream = s3Manager.getFile(replay.getS3(), filename);

        statements = LogFilter.getStatements(statementsStream);

        switch (replayType) {
            case "Time Sensitive":
                replayTimeSensitive(replay, statements);
                break;
            case "Fast Mode":
                replayFastMode(replay, statements);
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

    private void replayTimeSensitive(Replay replay, ArrayList<Statement> statements) {
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
                manager.query(statement.getQuery());
            }
            catch (InterruptedException ex) {
                ex.printStackTrace();
            }

            lastTime = currTime;
        }

        manager.closeConnection();
    }

    private void replayFastMode(Replay replay, ArrayList<Statement> statements) {
        MySQLManager manager = new MySQLManager(replay.getDBUrl(),
                replay.getDatabase(),
                replay.getDBUsername(),
                replay.getDBPassword());

        for (Statement statement : statements) {
            manager.query(statement.getQuery());
        }

        manager.closeConnection();
    }
}
