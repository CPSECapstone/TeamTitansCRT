package webHandler;

import com.amazonaws.services.s3.model.ObjectMetadata;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
public class CaptureServlet {

    @RequestMapping(value = "/capture/start", method = RequestMethod.POST)
    public ResponseEntity<String> captureStart(@RequestBody Capture capture) {
        if (capture.getId() == null || capture.getS3() == null || capture.getRds() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (capture.getStartTime() == null) {
            capture.setStartTime(new Date());
        }

        LogController logController = new CaptureLogController(capture);
        TimerManager timerManager = new TimerManager(capture.getId(), capture.getStartTime(), capture.getEndTime());

        CaptureController.addCapture(capture);
        CaptureController.addLogController(logController, capture.getId());
        CaptureController.addTimer(timerManager, capture.getId());

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/capture/stop", method = RequestMethod.POST)
    public ResponseEntity<String> captureStop(@RequestBody String id) {

        // Send bad request on unknown capture ID
        if (!CaptureController.doesCapturesTableContain(id)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Capture capture = CaptureController.getCapture(id);
        capture.setStatus("Finished");
        capture.setEndTime(new Date());

        CaptureController.endCapture(id);
        CaptureController.uploadAllFiles(capture);
        CaptureController.endCaptureResources(id);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/capture/update", method = RequestMethod.POST)
    public ResponseEntity<String> captureUpdate(@RequestBody Capture capture) {
        if (!CaptureController.doesCapturesTableContain(capture.getId())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        CaptureController.updateAll(capture);

        return new ResponseEntity<>(HttpStatus.OK);
    }


    @RequestMapping(value = "/capture/status", method = RequestMethod.GET)
    public ResponseEntity<Collection<Capture>> captureStatus() {
        return new ResponseEntity<>(CaptureController.getAllCaptureValues(), HttpStatus.OK);
    }
}
