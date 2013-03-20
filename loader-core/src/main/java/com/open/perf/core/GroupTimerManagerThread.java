package com.open.perf.core;

import com.open.perf.domain.GroupTimer;
import org.apache.log4j.Logger;

import java.util.List;

public class GroupTimerManagerThread extends Thread{
	private GroupController groupController;
    private List<GroupTimer> timers;
    private String          currentTimer;
    private int interval;
    private Logger logger      = Logger.getLogger(GroupTimerManagerThread.class);

    public GroupTimerManagerThread(GroupController groupController, List<GroupTimer> timers) {
        this.groupController    =   groupController;
        this.timers = timers;
        this.currentTimer       =   timers.get(0).getName();
        this.interval           =   1000;
    }

    public void run() {
        logger.info("Timer Thread for group '"+groupController.getGroupName()+"'");
        do{
            logger.debug("Group '"+this.groupController.getGroupName()+"' is Alive.");

            GroupTimer newTimer =   findTimerBean();
            if(newTimer.getName().equals(this.currentTimer) == false) {
                logger.info("Timer needs to change from '"+this.currentTimer+"' to '"+ newTimer.getName()+"'");
                this.currentTimer   =   newTimer.getName();
                try {
                    if(newTimer.getThreads() <= 0) {
                        logger.info("Pausing Group '"+this.groupController.getGroupName()+"' for '"+ newTimer.getDuration()+"' milliseconds");
                        this.groupController.pause(); // Don't make threads 0 here. It might fool GroupController to Stop the execution.
                    }
                    else {
                        this.groupController.resume();
                        this.groupController.setThroughput(newTimer.getThroughput());
                        this.groupController.setThreads(newTimer.getThreads());
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }

            try {
                logger.debug("Sleeping for "+this.interval+" milli seconds");
                Thread.sleep(this.interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }while(this.groupController.isAlive());
    }

    public GroupTimer findTimerBean() {
        long groupRuntimeMS   =   this.groupController.getRunTimeMS();
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

            timerEnd    =   timerStart  +   timer.getDuration();

            logger.debug("Timer '"+ timer.getName()+"' timer Start '"+timerStart+"' timer End '"+timerEnd+"' group runtime '"+groupRuntimeMS+"'");

            if(groupRuntimeMS > timerStart && groupRuntimeMS < timerEnd) {
                GroupTimer newTimer = new GroupTimer().
                        setName(timer.getName()).
                        setDuration(timer.getDuration()).
                        setThreads(timer.getThreads()).
                        setThroughput(timer.getThroughput());
                return newTimer;
            }
        }
    }
}
