package app.controllers;

import app.managers.CaptureTimerManager;
import app.models.Capture;
import app.models.Session;
import app.servlets.CaptureServlet;
import app.util.DBUtil;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CaptureController {

    private final static Map<String, Capture> captures = new ConcurrentHashMap<>();
    private final static Map<String, LogController> logControllers = new ConcurrentHashMap<>();
    private final static Map<String, CaptureTimerManager> timers = new ConcurrentHashMap<>();

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

        DBUtil.getInstance().saveCapture(capture);
    }

    private static void updateLogController(Capture capture) {
        LogController logController = getLogController(capture.getId());
        logController.updateLogFilter((Session) capture);
    }

    private static void updateTimerController(Capture capture) {
        CaptureTimerManager captureTimerManager = getTimer(capture.getId());
        captureTimerManager.updateTimeManager(capture.getStartTime(), capture.getEndTime());
    }

    /*
        Is called from CaptureServlet
     */
    public static void updateAll(Capture capture)
    {
        updateCapture(capture);
        updateLogController(capture);
        updateTimerController(capture);
    }

    /*
        Is called from capture CaptureLogController
     */
    public static void updateCaptureFileSize(String id, long fileSize) {
        if (captures.containsKey(id)) {
            Capture capture = captures.get(id);
            capture.setDbFileSize(fileSize);
            capture.updateStatus();
            if (capture.hasReachedFileSizeLimit() && !capture.getStatus().equals("Finished")) {
                stopCapture(id);
            }
        }
    }


    /*
        Is called from CaptureLogController
     */
    public static void updateCaptureTransactionCount(String id, int count) {
        if (captures.containsKey(id)) {
            Capture capture = captures.get(id);
            capture.setTransactionCount(count);
            capture.updateStatus();
            if (capture.hasReachedTransactonLimit() && !capture.getStatus().equals("Finished")) {
                stopCapture(id);
            }
        }
    }

    public static void endCaptureResources(String id)
    {
        LogController logController = logControllers.remove(id);
        CaptureTimerManager captureTimerManager = timers.remove(id);
        Capture capture = captures.remove(id);
        if (captureTimerManager != null)
        {
            captureTimerManager.end();
        }
    }

    public static void endCapture(String id)
    {
        LogController logController = getLogController(id);
        Capture capture = getCapture(id);
        if (logController != null && capture != null)
        {
            logController.processData(capture, CaptureLogController.END);
        }

        DBUtil.getInstance().saveCapture(capture);
    }

    public static void startCapture(String id)
    {
        if (captures.containsKey(id))
        {
            Capture capture = captures.get(id);
            capture.setStatus("Running");
        }
    }

    public static void stopCapture(String id)
    {
        CaptureServlet servlet = new CaptureServlet();
        Capture capture = captures.get(id);
        servlet.captureStop(capture);
    }


    public static void addTimer(CaptureTimerManager timer, String id) {
        timers.put(id, timer);
    }

    private static CaptureTimerManager getTimer(String id) {
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

        DBUtil.getInstance().saveCapture(capture);
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
        if (doesLogControllersTableContain(capture.getId()))
        {
            LogController logController = logControllers.get(capture.getId());
            logController.uploadAllFiles(capture);
        }
    }

    public static boolean isCaptureIdDuplicate(Capture capture) {
        return DBUtil.getInstance().checkCaptureNameDuplication(capture.getId());
    }
}
