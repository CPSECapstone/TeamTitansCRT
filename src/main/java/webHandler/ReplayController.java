package webHandler;

import com.amazonaws.services.s3.model.ObjectMetadata;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;

public class ReplayController {
    private HashMap<String, Replay> replays;

    private static ReplayController instance = null;

    public static ReplayController getInstance()
    {
        if (instance == null)
        {
            instance = new ReplayController();
        }
        return instance;
    }

    public void addReplay(Replay replay)
    {
        replays.put(replay.getId(), replay);
    }

    public void removeReplay(String id)
    {
        replays.remove(id);
    }

    public Collection<Replay> getAllReplays()
    {
        return replays.values();
    }

    /*
        @return Returns true if successfully uploads, false otherwise
    */
    private boolean uploadMetricsToS3(Replay replay) {
        CloudWatchManager cloudManager = new CloudWatchManager();
        String stats = cloudManager.getAllMetricStatisticsAsJson(replay.getRds(), replay.getStartTime(), replay.getEndTime());
        InputStream statStream = new ByteArrayInputStream(stats.getBytes(StandardCharsets.UTF_8));

        // Store RDS workload in S3
        S3Manager s3Manager = new S3Manager();
        s3Manager.uploadFile(replay.getS3(), replay.getId() + "-Performance.log", statStream, new ObjectMetadata());
        return true;
    }
}
