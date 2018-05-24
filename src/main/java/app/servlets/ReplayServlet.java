package app.servlets;

import app.controllers.CaptureController;
import app.controllers.LogController;
import app.controllers.ReplayLogController;
import app.util.ErrorsUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import app.models.Replay;
import app.controllers.ReplayController;
import app.managers.ReplayTimerManager;

import java.util.Collection;
import java.util.Date;

@RestController
public class ReplayServlet {
    
    @RequestMapping(value = "/replay/start", method = RequestMethod.POST)
    public ResponseEntity<String> startReplay(@RequestBody Replay replay) {

        if (ReplayController.isReplayIdDuplicate(replay)) {
            return new ResponseEntity<>(ErrorsUtil.DuplicateReplayIDError(), HttpStatus.BAD_REQUEST);
        }

        if (!replay.getId().matches("[A-Za-z0-9]+")) {
            return new ResponseEntity<>(ErrorsUtil.idContainsNonAlphaNumeric(), HttpStatus.BAD_REQUEST);
        }

        if (replay.getStartTime() == null) {
            replay.setStartTime(new Date());
        }

        if (replay.getStartTime().before(new Date())) {
            return new ResponseEntity<>(ErrorsUtil.StartTimeBeforeCurrentTimeError(), HttpStatus.BAD_REQUEST);
        }

        if (replay.getEndTime() != null && replay.getEndTime().before(replay.getStartTime())) {
            return new ResponseEntity<>(ErrorsUtil.EndTimeBeforeStartTimeError(), HttpStatus.BAD_REQUEST);
        }

        if (replay.getTransactionLimit() < 0) {
            return new ResponseEntity<>(ErrorsUtil.NegativeNumbersError(), HttpStatus.BAD_REQUEST);
        }


        if (replay.getStartTime() == null)
        {
            replay.setStartTime(new Date());
        }

        ReplayController.addReplay(replay);
        LogController logController = new ReplayLogController(replay);
        ReplayController.addLogController(logController, replay.getId());
        ReplayTimerManager timerManager = new ReplayTimerManager(replay.getId(), replay.getStartTime());
        ReplayController.addTimer(timerManager, replay.getId());

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/replay/stop", method = RequestMethod.POST)
    public ResponseEntity<String> stopReplay(@RequestBody Replay tempReplay)
    {
        Replay replay = ReplayController.getReplay(tempReplay.getId());
        replay.setEndTime(new Date());
        ReplayController.removeAll(tempReplay.getId());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/replay/delete", method = RequestMethod.POST)
    public ResponseEntity<String> deleteReplay(@RequestBody Replay replay) {
        if (replay.getId() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        ReplayController.deleteReplay(replay);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/replay/status", method = RequestMethod.GET)
    public ResponseEntity<Collection<Replay>> replayStatus() {
        return new ResponseEntity<>(ReplayController.getAllReplayValues(), HttpStatus.OK);
    }

    // Add the ability to support editing scheduled replays

}
