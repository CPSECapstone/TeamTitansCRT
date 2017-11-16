package webHandler;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RestController
public class CaptureController {

    @RequestMapping(value = "/capture/{capture}", method = RequestMethod.POST)
    public ResponseEntity<String> CaptureCommand(@PathVariable("capture") String capture, @RequestBody String command) {
        return new ResponseEntity<String>(capture + " - " + command, HttpStatus.OK);
    }

    @RequestMapping(value = "/capture/{capture}/status", method = RequestMethod.GET)
    public ResponseEntity<Status> CaptureStatus(@PathVariable("capture") String capture) {
        return new ResponseEntity<Status>(new Status("Capturing", new StatusMetrics(0.0,1.0,2.0)), HttpStatus.OK);
    }
}
