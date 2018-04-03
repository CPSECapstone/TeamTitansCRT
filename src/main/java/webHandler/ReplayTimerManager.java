package webHandler;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class ReplayTimerManager
{
    private String replayId;

    private Date startTime;

    private Timer startTimer;

    public ReplayTimerManager(String id, Date startTime)
    {
        this.replayId = id;
        this.startTime = startTime;
        startStartTimer();
    }

    private void startStartTimer()
    {
        if (startTimer == null)
        {
            startTimer = new Timer();
        }
        if (startTime.before(new Date()))
        {
            startTimer.schedule(new TimerTask()
            {
                @Override
                public void run() {
                    ReplayController.startReplay(replayId);
                }
            }, new Date());
        }
        else
        {
            startTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    ReplayController.startReplay(replayId);
                }
            }, startTime);
        }


    }

    public void end()
    {
        if (startTimer != null)
        {
            startTimer.cancel();
            startTimer = null;
        }
    }

    // public void updateTimer()
}
