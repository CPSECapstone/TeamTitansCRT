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

        LogController logController = new LogController(capture);
        TimerManager timerManager = new TimerManager(capture.getId(), capture.getStartTime(), capture.getEndTime());
        CaptureController instance = CaptureController.getInstance();

        instance.addCapture(capture);
        instance.addLogController(logController, capture.getId());
        instance.addTimer(timerManager, capture.getId());

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/capture/stop", method = RequestMethod.POST)
    public ResponseEntity<String> captureStop(@RequestBody String id) {

        CaptureController instance = CaptureController.getInstance();

        // Send bad request on unknown capture ID
        if (!instance.doesCapturesTableContain(id)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Capture capture = instance.getCapture(id);
        capture.setStatus("Finished");
        capture.setEndTime(new Date());

        instance.endCaptureResources(id);

        instance.uploadLogsAndMetricsToS3(capture);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/capture/update", method = RequestMethod.POST)
    public ResponseEntity<String> captureUpdate(@RequestBody Capture capture) {
        CaptureController instance = CaptureController.getInstance();
        if (!instance.doesCapturesTableContain(capture.getId())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        instance.updateAll(capture);

        return new ResponseEntity<>(HttpStatus.OK);
    }


    @RequestMapping(value = "/capture/status", method = RequestMethod.GET)
    public ResponseEntity<Collection<Capture>> captureStatus() {
        return new ResponseEntity<>(CaptureController.getInstance().getAllCaptureValues(), HttpStatus.OK);
    }
}
