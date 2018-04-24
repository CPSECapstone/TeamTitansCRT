package app.managers;

import app.controllers.CaptureController;

import java.util.*;

public class CaptureTimerManager {

    // Number of minutes to milliseconds to wait before updating captures.
    private final int UPDATE_PERIOD_MINUTE = 1000 * 60 * 1;
    private final int UPDATE_PERIOD_HOUR = UPDATE_PERIOD_MINUTE * 60;

    private Date startTime;
    private Date endTime;

    private Timer hourTimer;
    private Timer endTimer;
    private Timer startTimer;

    private String captureID;

    public CaptureTimerManager(String id, Date startTime, Date endTime) {
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
        startStartTimer();
    }

    private void startHourTimer()
    {
        if (hourTimer == null)
        {
            hourTimer = new Timer();
        }
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
        if (c.get(Calendar.MINUTE) == 0 && c.get(Calendar.SECOND) == 0)
        {
            return c.getTime();
        }
        c.add(Calendar.HOUR, 1);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        return c.getTime();
    }

    private void endHourTimer()
    {
        if (hourTimer != null)
        {
            hourTimer.cancel();
            hourTimer = null;
        }
    }

    private void startEndTimer()
    {
        if (endTimer == null)
        {
            endTimer = new Timer();
        }
        if (endTime != null) {
            endTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    CaptureController.stopCapture(captureID);
                }
            }, endTime);
        }
    }

    private void endEndTimer()
    {
        if (endTimer != null) {
            endTimer.cancel();
            endTimer = null;
        }
    }

    private void startStartTimer()
    {
        if (startTime.after(new Date()))
        {
            if (startTimer == null)
            {
                startTimer = new Timer();
            }
            startTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    CaptureController.startCapture(captureID);
                }
            }, startTime);
        }
    }

    private void endStartTimer()
    {
        if (startTimer != null)
        {
            startTimer.cancel();
            startTimer = null;
        }
    }

    public void updateTimeManager(Date startTime, Date endTime) {
        this.endTime = endTime;
        if (this.startTime.compareTo(startTime) != 0)
        {
            this.startTime = startTime;
            endStartTimer();
            endHourTimer();
            startStartTimer();
            startHourTimer();
        }
        endEndTimer();
        startEndTimer();
    }

    private void endAllTimers()
    {
        endHourTimer();
        endStartTimer();
        endEndTimer();
    }

    public void end() {
        endAllTimers();
    }

}
