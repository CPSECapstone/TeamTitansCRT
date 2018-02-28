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

    @RequestMapping(value = "/capture/start", method = RequestMethod.POST)
    public ResponseEntity<String> startReplay(@RequestBody Capture capture, String replayType) {
        S3Manager s3Manager = new S3Manager();
        InputStream statementsStream;
        ArrayList<Statement> statements;

        String filename = capture.getId() + WorkloadTag;
        statementsStream = s3Manager.getFile(capture.getS3(), filename);

        statements = LogFilter.getStatements(statementsStream);

        switch (replayType) {
            case "Time Sensitive":
                replayTimeSensitive(statements);
                break;
            case "Fast Mode":
                replayFastMode(statements);
                break;
            default:
                System.out.println("Error --- Unknown replay type.");
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/capture/stop", method = RequestMethod.POST)
    public ResponseEntity<String> stopReplay(@RequestBody Capture capture) {
        return new ResponseEntity<>(HttpStatus.OK);
    }


    private void replayTimeSensitive(ArrayList<Statement> statements) {

    }

    private void replayFastMode(ArrayList<Statement> statements) {

    }
}
