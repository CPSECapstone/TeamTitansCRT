package app.servlets;

import app.controllers.LogController;
import app.controllers.ReplayLogController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import app.models.Replay;
import app.controllers.ReplayController;
import app.managers.ReplayTimerManager;

import java.util.Date;

@RestController
public class ReplayServlet {
    
    @RequestMapping(value = "/replay/start", method = RequestMethod.POST)
    public ResponseEntity<String> startReplay(@RequestBody Replay replay) {
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

    @RequestMapping(value = "/replay/delete", method = RequestMethod.GET)
    public ResponseEntity<String> deleteReplay(Replay replay) {
        if (replay.getId() == null || replay.getS3() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        ReplayController.deleteReplay(replay);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    // Add the ability to support editing scheduled replays

}
