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

    // Todo: Needs to update on the hour
    // Todo: Needs to
    private void startTimers() {
        // Timer to handle downloading logs and concatenating them to a file on the hour
        hourTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                CaptureController.getInstance().writeHourlyLogFile(captureID, hour);
            }
        }, startTime, UPDATE_PERIOD_HOUR);

        if (endTime != null) {
            endTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    CaptureController.getInstance().captureStop(captureID);
                }
            }, endTime);
        }
    }

    // Todo: If timer has already started it should wait until the hour to go off, as of right now it will possibly double write to the file
    public void updateTimeManager(Date startTime, Date endTime) {
        this.startTime = startTime;
        this.endTime = endTime;

        hourTimer.cancel();
        endTimer.cancel();

        startTimers();
    }

    public void end() {
        hourTimer.cancel();
        endTimer.cancel();
    }

}
