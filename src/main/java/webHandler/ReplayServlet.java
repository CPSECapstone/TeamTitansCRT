package webHandler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Date;

@RestController
public class ReplayServlet {
    
    @RequestMapping(value = "/replay/start", method = RequestMethod.POST)
    public ResponseEntity<String> startReplay(@RequestBody Replay replay) {
        if (replay.getStartTime() == null)
        {
            replay.setStartTime(new Date());
        }
        System.out.println(replay.getStartTime());
        System.out.println(replay.getId());

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

    // Add the ability to support editing scheduled replays

}
