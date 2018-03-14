package webHandler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Date;

@RestController
public class ReplayServlet {

    public final String WorkloadTag = "-Workload.log";

    @RequestMapping(value = "/replay/start", method = RequestMethod.POST)
    public ResponseEntity<String> startReplay(@RequestBody Replay replay, String replayType) {
        if (replay.getStartTime() == null)
        {
            replay.setStartTime(new Date());
        }

        ReplayController.addReplay(replay);
        LogController logController = new ReplayLogController(replay);

        ReplayController.addLogController(logController, replay.getId());
        // TODO: Add the ability to schedule replays
        int type = replayType.equals("Fast Mode") ? ReplayLogController.FAST_MODE : ReplayLogController.TIME_SENSITIVE;
        logController.processData(replay, type);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/replay/stop", method = RequestMethod.POST)
    public ResponseEntity<String> stopReplay(@RequestBody Replay tempReplay)
    {
        // TODO: Support manually ending the replay
        Replay replay = ReplayController.getReplay(tempReplay.getId());
        replay.setEndTime(new Date());
        ReplayController.removeReplay(tempReplay.getId());
        ReplayController.removeLogController(tempReplay.getId());
        return new ResponseEntity<>(HttpStatus.OK);
    }




}
