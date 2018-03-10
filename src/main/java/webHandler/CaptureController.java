package webHandler;

import com.amazonaws.services.s3.model.ObjectMetadata;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CaptureController {

    private final static Map<String, Capture> captures = new ConcurrentHashMap<>();
    private final static Map<String, LogController> logControllers = new ConcurrentHashMap<>();
    private final static Map<String, TimerManager> timers = new ConcurrentHashMap<>();

    private static void updateCapture(Capture updatedCapture) {
        Capture capture = getCapture(updatedCapture.getId());
        if (updatedCapture.getStartTime() != null)
        {
            capture.setStartTime(updatedCapture.getStartTime());
        }
        if (updatedCapture.getEndTime() != null)
        {
            capture.setEndTime(updatedCapture.getEndTime());
        }
        if (updatedCapture.getTransactionLimit() != 0)
        {
            capture.setTransactionLimit(updatedCapture.getTransactionLimit());
        }
        if (updatedCapture.getFileSizeLimit() != 0)
        {
            capture.setFileSizeLimit(updatedCapture.getFileSizeLimit());
        }
        capture.updateStatus();
    }

    private static void updateLogController(Capture capture) {
        LogController logController = getLogController(capture.getId());
        logController.updateLogFilter((Session) capture);
    }

    private static void updateTimerController(Capture capture) {
        TimerManager timerManager = getTimer(capture.getId());
        timerManager.updateTimeManager(capture.getStartTime(), capture.getEndTime());
    }

    public static void updateAll(Capture capture)
    {
        updateCapture(capture);
        updateLogController(capture);
        updateTimerController(capture);
    }

    public static void updateCaptureFileSize(String id, long fileSize) {
        if (captures.containsKey(id)) {
            Capture capture = captures.get(id);
            capture.setDbFileSize(fileSize);
            capture.updateStatus();
            if (capture.hasReachedFileSizeLimit()) {
                stopCapture(id);
            }
        }
    }

    public static void updateCaptureTransactionCount(String id, int count) {
        if (captures.containsKey(id)) {
            Capture capture = captures.get(id);
            capture.setTransactionCount(count);
            capture.updateStatus();
            if (capture.hasReachedTransactonLimit()) {
                stopCapture(id);
            }
        }
    }

    public static void endCaptureResources(String id)
    {
        LogController logController = getLogController(id);
        TimerManager timerManager = getTimer(id);
        Capture capture = getCapture(id);

        if (logController != null && capture != null)
        {
            String logData = logController.getLogData(capture.getRds(), CaptureLogController.GeneralLogFileName);
            logController.processData((Session) capture, CaptureLogController.END);
            logControllers.remove(id);
        }
        if (timerManager != null)
        {
            timerManager.end();
            timers.remove(id);
        }
        captures.remove(id);
    }

    public static void stopCapture(String id)
    {
        CaptureServlet servlet = new CaptureServlet();
        servlet.captureStop(id);
    }


    public static void addTimer(TimerManager timer, String id) {
        timers.put(id, timer);
    }

    private static TimerManager getTimer(String id) {
        if (doesTimersTableContain(id)) {
            return timers.get(id);
        }
        return null;
    }

    public static void addLogController(LogController controller, String id) {
        logControllers.put(id, controller);
    }

    private static LogController getLogController(String id) {
        if (doesLogControllersTableContain(id)) {
            return logControllers.get(id);
        }
        return null;
    }

    public static void addCapture(Capture capture) {
        captures.put(capture.getId(), capture);
    }

    public static Capture getCapture(String id) {
        if (doesCapturesTableContain(id)) {
            return captures.get(id);
        }
        return null;
    }

    public static boolean doesCapturesTableContain(String id) {
        if (captures.containsKey(id)) {
            return true;
        }

        return false;
    }

    public static boolean doesLogControllersTableContain(String id) {
        if (logControllers.containsKey(id)) {
            return true;
        }

        return false;
    }

    public static boolean doesTimersTableContain(String id) {
        if (timers.containsKey(id)) {
            return true;
        }

        return false;
    }

    public static void hourlyCaptureLogUpdate(String captureID)
    {
        if (doesLogControllersTableContain(captureID))
        {
            LogController logController = logControllers.get(captureID);
            Capture capture = captures.get(captureID);
            logController.processData((Session) capture, CaptureLogController.HOURLY);
        }
    }

    public static Collection<Capture> getAllCaptureValues()
    {
        return captures.values();
    }

    public static void uploadAllFiles(Capture capture)
    {
        LogController logController = logControllers.get(capture.getId());
        logController.uploadAllFiles((Session) capture);
    }
}
