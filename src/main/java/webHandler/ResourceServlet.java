package webHandler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Servlet to handle all resource calls.
 */
@RestController
public class ResourceServlet {

    @RequestMapping(value = "/resource/rds", method = RequestMethod.GET)
    public ResponseEntity<Collection<String>> getRDSInstances() {

        RDSManager rdsManager = new RDSManager();
        return new ResponseEntity<>(rdsManager.getRDSInstances(), HttpStatus.OK);
    }

    @RequestMapping(value = "/resource/s3", method = RequestMethod.GET)
    public ResponseEntity<Collection<String>> getS3Buckets() {

        S3Manager s3Manager = new S3Manager();
        return new ResponseEntity<>(s3Manager.getS3Buckets(), HttpStatus.OK);
    }

    @RequestMapping(value = "/resource/history", method = RequestMethod.GET)
    public ResponseEntity<Collection<Capture>> getCaptureReplayHistory() {

        ArrayList<Capture> captures = DBUtil.getInstance().loadAllCaptures();

        if (captures == null) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(captures, HttpStatus.OK);
    }

    @RequestMapping(value = "/resource/captures", method = RequestMethod.GET)
    public ResponseEntity<Collection<Capture>> getAllCaptureHistory() {

        ArrayList<Capture> captures = DBUtil.getInstance().loadAllCaptures();

        if (captures == null) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(captures, HttpStatus.OK);
    }

    @RequestMapping(value = "/resource/captures/{status}", method = RequestMethod.GET)
    public ResponseEntity<Collection<Capture>> getCapturesWithStatus(@PathVariable String status) {
        ArrayList<Capture> captures = DBUtil.getInstance().loadCapturesWithStatus(status);

        if (captures == null) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(captures, HttpStatus.OK);
    }

    @RequestMapping(value = "/resource/replays", method = RequestMethod.GET)
    public ResponseEntity<Collection<Replay>> getAllReplayHistory() {

        ArrayList<Replay> replays = DBUtil.getInstance().loadAllReplays();

        if (replays == null) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(replays, HttpStatus.OK);
    }

    @RequestMapping(value = "/resource/replays/{status}", method = RequestMethod.GET)
    public ResponseEntity<Collection<Replay>> getReplaysWithStatus(@PathVariable String status) {
        ArrayList<Replay> replays = DBUtil.getInstance().loadReplaysWithStatus(status);

        if (replays == null) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(replays, HttpStatus.OK);
    }
}
