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

    HashMap<String, Capture> captures = new HashMap<>();

    @RequestMapping(value = "/capture/start", method = RequestMethod.POST)
    public ResponseEntity<String> CaptureCommand(@RequestBody Capture capture) {

        capture.setStartTime(new Date());
        capture.setStatus("Running");

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/capture/status", method = RequestMethod.GET)
    public ResponseEntity<Status> CaptureStatus(@PathVariable("capture") String capture) {
        return new ResponseEntity<>(new Status("Capturing", new StatusMetrics(0.0,1.0,2.0)), HttpStatus.OK);
    }
}
