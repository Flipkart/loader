package com.open.perf.load;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 20/2/13
 * Time: 5:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class GroupStatsQueue {
    private LinkedBlockingQueue<GroupStatsInstance> groupWriteQueue;
    private LinkedBlockingQueue<GroupStatsInstance> groupReadQueue;
    private LinkedBlockingQueue<GroupStatsInstance> groupWriteQueueLink;
    private LinkedBlockingQueue<GroupStatsInstance> groupReadQueueLink;

    private boolean oppositeQueue = true;

    public GroupStatsQueue() {
        this.groupWriteQueue = new LinkedBlockingQueue<GroupStatsInstance>();
        this.groupReadQueue = new LinkedBlockingQueue<GroupStatsInstance>();
        this.groupWriteQueueLink = this.groupWriteQueue;
        this.groupReadQueueLink = this.groupReadQueue;
    }

    public GroupStatsQueue addGroupStats(GroupStatsInstance groupStatsInstance) {
        groupStatsInstance.setEndTime(System.nanoTime());
        this.groupWriteQueueLink.offer(groupStatsInstance);
        return this;
    }

    public GroupStatsInstance getGroupStats() {
        return this.groupReadQueueLink.poll();
    }

    synchronized void swapQueues() {
        System.out.println("Swapping Queue");
        if(oppositeQueue) {
            this.groupWriteQueueLink = this.groupReadQueue;
            this.groupReadQueueLink = this.groupWriteQueue;
        }
        else {
            this.groupWriteQueueLink = this.groupWriteQueue;
            this.groupReadQueueLink = this.groupReadQueue;
        }
        oppositeQueue = !oppositeQueue;
    }
}
