package webHandler;

import java.io.InputStream;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public class ReplayServlet {

    public final String WorkloadTag = "-Workload.log";

    @RequestMapping(value = "/replay/start", method = RequestMethod.POST)
    public ResponseEntity<String> startReplay(@RequestBody Replay replay, String replayType) {
        LogController replayController = new ReplayLogController(replay);

        if (replay.getStartTime() == null)
        {
            replay.setStartTime(new Date());
        }
        // TODO: Add the ability to schedule replays
        int type = replayType.equals("Fast Mode") ? ReplayLogController.FAST_MODE : ReplayLogController.TIME_SENSITIVE;
        replayController.processData((Session) replay, type);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/replay/stop", method = RequestMethod.POST)
    public ResponseEntity<String> stopReplay(@RequestBody Replay replay)
    {
        // TODO: Support manually ending the replay
        ReplayController.removeReplay(replay.getId());
        return new ResponseEntity<>(HttpStatus.OK);
    }




}
