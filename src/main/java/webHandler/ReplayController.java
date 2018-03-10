package webHandler;

import com.amazonaws.services.s3.model.ObjectMetadata;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReplayController {
    private final static Map<String, Replay> replays = new ConcurrentHashMap<>();


    public static void addReplay(Replay replay)
    {
        replays.put(replay.getId(), replay);
    }

    public static void removeReplay(String id)
    {
        replays.remove(id);
    }

    public static Collection<Replay> getAllReplays()
    {
        return replays.values();
    }


}
