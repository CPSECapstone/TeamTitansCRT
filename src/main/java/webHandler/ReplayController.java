package webHandler;


import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReplayController {
    private final static Map<String, Replay> replays = new ConcurrentHashMap<>();
    private final static Map<String, LogController> logControllers = new ConcurrentHashMap<>();

    public static void addReplay(Replay replay)
    {
        replays.put(replay.getId(), replay);
    }

    public static void removeReplay(String id)
    {
        replays.remove(id);
    }

    public static void addLogController(LogController logController, String id)
    {
        logControllers.put(id, logController);
    }

    public static void removeLogController(String id)
    {
        logControllers.remove(id);
    }

    public static Collection<Replay> getAllReplays()
    {
        return replays.values();
    }


    public static Replay getReplay(String id) {
        return replays.get(id);
    }

}
