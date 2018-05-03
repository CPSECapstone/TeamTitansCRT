package app.servlets;

import app.controllers.CaptureController;
import app.controllers.CaptureLogController;
import app.managers.CaptureTimerManager;
import app.util.ErrorsUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import app.models.Capture;
import app.controllers.LogController;

import java.util.Collection;
import java.util.Date;

@RestController
public class CaptureServlet {

    @RequestMapping(value = "/capture/start", method = RequestMethod.POST)
    public ResponseEntity<String> captureStart(@RequestBody Capture capture) {
        if (capture.getId() == null || capture.getS3() == null || capture.getRds() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (capture.getId().contains(" ")) {
            return new ResponseEntity<>(ErrorsUtil.CaptureIDContainsSpaces(), HttpStatus.BAD_REQUEST);
        }

        if (CaptureController.isCaptureIdDuplicate(capture)) {
            return new ResponseEntity<>(ErrorsUtil.DuplicateCaptureIDError(), HttpStatus.BAD_REQUEST);
        }

        if (capture.getStartTime() == null) {
            capture.setStartTime(new Date());
        }

        if (capture.getStartTime().before(new Date())) {
            return new ResponseEntity<>(ErrorsUtil.StartTimeBeforeCurrentTimeError(), HttpStatus.BAD_REQUEST);
        }

        if (capture.getEndTime() != null && capture.getEndTime().before(capture.getStartTime())) {
            return new ResponseEntity<>(ErrorsUtil.EndTimeBeforeStartTimeError(), HttpStatus.BAD_REQUEST);
        }

        if (capture.getFileSizeLimit() < 0 || capture.getTransactionLimit() < 0) {
            return new ResponseEntity<>(ErrorsUtil.NegativeNumbersError(), HttpStatus.BAD_REQUEST);
        }

        LogController logController = new CaptureLogController(capture);
        CaptureTimerManager captureTimerManager = new CaptureTimerManager(capture.getId(), capture.getStartTime(),
                capture.getEndTime());

        CaptureController.addCapture(capture);
        CaptureController.addLogController(logController, capture.getId());
        CaptureController.addTimer(captureTimerManager, capture.getId());

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/capture/stop", method = RequestMethod.POST)
    public ResponseEntity<String> captureStop(@RequestBody Capture tempCapture) {

        String id = tempCapture.getId();
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

    @RequestMapping(value = "/capture/delete", method = RequestMethod.POST)
    public ResponseEntity<String> captureDelete(@RequestBody Capture capture) {
        if (capture.getId() == null || capture.getS3() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        CaptureController.deleteCapture(capture);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
