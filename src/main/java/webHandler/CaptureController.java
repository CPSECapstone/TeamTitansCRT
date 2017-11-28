package webHandler;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

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

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/capture/status", method = RequestMethod.GET)
    public ResponseEntity<Status> CaptureStatus(@PathVariable("capture") String capture) {
        return new ResponseEntity<>(new Status("Capturing", new StatusMetrics(0.0,1.0,2.0)), HttpStatus.OK);
    }
}
