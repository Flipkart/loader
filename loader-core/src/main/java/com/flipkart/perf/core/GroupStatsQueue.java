package com.flipkart.perf.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Its an abstraction queues that allow add and set operations on circularly changing queues.
 * It provides optimization where writer and reader will never be using the same queue and hence less synchronization
 */
final public class GroupStatsQueue {
    private LinkedBlockingQueue<GroupStatsInstance> groupWriteQueue;
    private LinkedBlockingQueue<GroupStatsInstance> groupReadQueue;
    private LinkedBlockingQueue<GroupStatsInstance> groupWriteQueueLink;
    private LinkedBlockingQueue<GroupStatsInstance> groupReadQueueLink;
    private boolean oppositeQueue = true;
    private static Logger logger = LoggerFactory.getLogger(GroupStatsQueue.class);

    public GroupStatsQueue() {
        this.groupWriteQueue = new LinkedBlockingQueue<GroupStatsInstance>();
        this.groupReadQueue = new LinkedBlockingQueue<GroupStatsInstance>();
        this.groupWriteQueueLink = this.groupWriteQueue;
        this.groupReadQueueLink = this.groupReadQueue;
    }

    /**
     * Add to the Queue
     * @param groupStatsInstance
     * @return
     */
    public GroupStatsQueue addGroupStats(GroupStatsInstance groupStatsInstance) {
        this.groupWriteQueueLink.offer(groupStatsInstance);
        return this;
    }

    /**
     * Remove and return
     * @return
     */
    public GroupStatsInstance getGroupStats() {
        return this.groupReadQueueLink.poll();
    }

    public void swapQueues() {
        logger.debug("Swapping Queue");
        if(oppositeQueue) {
            this.groupWriteQueueLink = this.groupReadQueue;
            this.groupReadQueueLink = this.groupWriteQueue;
        }
        else {
            this.groupWriteQueueLink = this.groupWriteQueue;
            this.groupReadQueueLink = this.groupReadQueue;
        }
        oppositeQueue = !oppositeQueue;
        logger.debug("GroupStats Instances in Read Queue :"+this.groupReadQueueLink.size());
        logger.debug("GroupStats Instances in Write Queue :"+this.groupWriteQueueLink.size());
    }
}