package com.open.perf.load;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.open.perf.domain.GroupTimer;
import org.apache.log4j.Logger;

public class TimerThread extends Thread{
/*
	private GroupController groupController;
    private List<GroupTimer> timers;
    private String          currentTimer;
    private int interval;
    Logger logger      = Logger.getLogger(TimerThread.class);

    public TimerThread(GroupController groupController, List<GroupTimer> timers) {
        this.groupController    =   groupController;
        this.timers = timers;
        this.currentTimer       =   timers.get(0).getName();
        this.interval           =   1000;
    }

    public void run() {
        logger.info("Timer Thread for group '"+groupController.getGroupName()+"'");
        while(this.groupController.isAlive()) {
            logger.debug("Group '"+this.groupController.getGroupName()+"' is Alive.");

            if(this.groupController.getRunTimeInMilliSeconds() > 0) {
                GroupTimer newTimer =   findTimerBean();

                if(newTimer.getName().equals(this.currentTimer) == false) {
                    logger.info("Timer needs to change from '"+this.currentTimer+"' to '"+ newTimer.getName()+"'");
                    this.currentTimer   =   newTimer.getName();
                    try {
                        if(newTimer.getThreads() <= 0) {
                            logger.info("Pausing Group '"+this.groupController.getGroupName()+"' for '"+ newTimer.getRuntime()+"' milliseconds");
                            this.groupController.pause(); // Don't make threads 0 here. It might fool GroupController to Stop the execution.
                        }
                        else {
                            this.groupController.resume0();
                            this.groupController.setThreads(newTimer.getThreads());
                            this.groupController.setDelayAfterRepeats(newTimer.getDelayAfterRepeats());
                            this.groupController.setCurrentTime(newTimer);
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }

            try {
                logger.debug("Sleeping for "+this.interval+" milli seconds");
                Thread.sleep(this.interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public GroupTimer findTimerBean() {
        long groupRuntime   =   this.groupController.getRunTimeInMilliSeconds();
        long timerStart      =   0;
        long timerEnd        =   0;

        for(int i=0;true;i++) {
            GroupTimer timer =   null;
            if(i == 0) {
                timer =   this.timers.get(0);
                timerStart  =   0;
            }
            else {
                timer =   this.timers.get(i% timers.size());
                timerStart  =   timerEnd;
            }

            timerEnd    =   timerStart  +   timer.getRuntime();

            logger.debug("Timer '"+ timer.getName()+"' timer Start '"+timerStart+"' timer End '"+timerEnd+"' group runtime '"+groupRuntime+"'");

            if(groupRuntime > timerStart && groupRuntime < timerEnd) {
                GroupTimer newTimer = new GroupTimer(timer.getName()).
                        setDelayAfterRepeats(timer.getDelayAfterRepeats()).
                        setRuntime(timer.getRuntime()).
                        setRepeats(timer.getRepeats()).
                        setThreads(timer.getThreads()).
                        setStartTime(System.currentTimeMillis());
;
                return newTimer;
                // OLD return timer;
            }
        }
    }

*/
}
