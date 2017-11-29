package webHandler;

import com.amazonaws.services.s3.model.ObjectMetadata;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

@RestController
public class CaptureController {

    private HashMap<String, Capture> captures = new HashMap<>();

    @RequestMapping(value = "/capture/start", method = RequestMethod.POST)
    public ResponseEntity<String> CaptureStart(@RequestBody Capture capture) {

        if (capture.getId() == null || capture.getS3() == null || capture.getRds() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (capture.getStartTime() == null) {
            capture.setStartTime(new Date());
        }

        captures.put(capture.getId(), capture);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/capture/stop", method = RequestMethod.POST)
    public ResponseEntity<String> CaptureStop(@RequestBody Capture capture) {

        Capture targetCapture = captures.get(capture.getId());

        if (targetCapture == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (capture.getEndTime() == null) {
            targetCapture.setEndTime(new Date());
        } else {
            targetCapture.setEndTime(capture.getEndTime());
        }

        if (targetCapture.getStatus().equals("Finished")) {
            String fileName = targetCapture.getId() + "-Workload.log";

            // Grab RDS workload
            RDSManager rdsManager = new RDSManager();
            InputStream stream = rdsManager.downloadLog(targetCapture.getRds(), fileName);

            if (stream == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            // Store RDS workload in S3
            S3Manager s3Manager = new S3Manager();
            s3Manager.uploadFile(targetCapture.getS3(), fileName, stream, new ObjectMetadata());

            //TODO: Add check for file upload
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/capture/status", method = RequestMethod.GET)
    public ResponseEntity<Collection<Capture>> CaptureStatus() {
        return new ResponseEntity<>(captures.values(), HttpStatus.OK);
    }
}
