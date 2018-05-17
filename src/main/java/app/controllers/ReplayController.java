package app.controllers;


import app.managers.ReplayTimerManager;
import app.util.DBUtil;
import app.models.Replay;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReplayController {
    private final static Map<String, Replay> replays = new ConcurrentHashMap<>();
    private final static Map<String, LogController> logControllers = new ConcurrentHashMap<>();
    private final static Map<String, ReplayTimerManager> timers = new ConcurrentHashMap<>();

    public static void addReplay(Replay replay)
    {
        replays.put(replay.getId(), replay);
        DBUtil.getInstance().saveReplay(replay);
    }

    public static void removeReplay(String id)
    {
        if (replays.containsKey(id))
        {
            Replay replay = replays.remove(id);
            DBUtil.getInstance().saveReplay(replay);
        }
    }

    public static void addLogController(LogController logController, String id)
    {
        logControllers.put(id, logController);
    }

    public static void removeLogController(String id)
    {
        if (logControllers.containsKey(id))
        {
            logControllers.remove(id);
        }
    }

    public static Collection<Replay> getAllReplayValues()
    {
        return replays.values();
    }

    public static void startReplay(String id)
    {
        if (logControllers.containsKey(id) && replays.containsKey(id))
        {
            LogController logController = logControllers.get(id);
            Replay replay = replays.get(id);
            replay.setStatus("Running");
            DBUtil.getInstance().saveReplay(replay);
            boolean type = replay.getReplayType().equals("Fast Mode") ? ReplayLogController.FAST_MODE :
                    ReplayLogController.TIME_SENSITIVE;
            logController.processData(replay, type);
        }
    }

    public static void removeAll(String id)
    {
        removeReplay(id);
        removeLogController(id);
        removeTimer(id);
    }

    public static void addTimer(ReplayTimerManager timerManager, String id)
    {
        timers.put(id, timerManager);
    }

    public static void removeTimer(String id)
    {
        if (timers.containsKey(id))
        {
            ReplayTimerManager timerManager = timers.remove(id);
            timerManager.end();
        }
    }
    public static boolean isReplayIdDuplicate(Replay replay) {
        return DBUtil.getInstance().checkReplayNameDuplication(replay.getId());
    }

    public static Replay getReplay(String id) {
        return replays.get(id);
    }

    public static boolean deleteReplay(Replay replay) {
        replays.remove(replay.getId());
        logControllers.remove(replay.getId());
        timers.remove(replay.getId());
        DBUtil.getInstance().deleteReplay(replay.getId());

        return true;
    }

}
