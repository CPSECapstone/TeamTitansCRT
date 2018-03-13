package webHandler;

import java.util.*;

public class TimerManager {

    // Number of minutes to milliseconds to wait before updating captures.
    private final int UPDATE_PERIOD_MINUTE = 1000 * 60 * 1;
    private final int UPDATE_PERIOD_HOUR = UPDATE_PERIOD_MINUTE * 60;

    private Date startTime;
    private Date endTime;

    private Timer hourTimer;
    private Timer endTimer;

    private String captureID;

    public TimerManager(String id, Date startTime, Date endTime) {
        this.captureID = id;
        this.startTime = startTime;
        this.endTime = endTime;
        hourTimer = new Timer();
        endTimer = new Timer();
        startTimers();
    }

    private void startTimers() {
        // Timer to handle downloading logs and concatenating them to a file on the hour
        startHourTimer();
        startEndTimer();
    }

    private void startHourTimer()
    {
        hourTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                CaptureController.hourlyCaptureLogUpdate(captureID);
            }
        }, roundToNextWholeHour(startTime), UPDATE_PERIOD_HOUR);
    }

    public Date roundToNextWholeHour(Date date) {
        Calendar c = new GregorianCalendar();
        c.setTime(date);
        c.add(Calendar.HOUR, 1);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        return c.getTime();
    }

    private void startEndTimer()
    {
        endTimer = new Timer();
        if (endTime != null) {
            endTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    CaptureController captureController = new CaptureController();
                    captureController.stopCapture(captureID);
                }
            }, endTime);
        }
    }

    public void updateTimeManager(Date startTime, Date endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
        System.out.println(startTime);
        System.out.println(endTime);

        endTimer.cancel();

        startEndTimer();
    }

    public void end() {
        hourTimer.cancel();
        endTimer.cancel();
    }

}
