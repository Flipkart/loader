package com.open.perf.core;

public class PercentileCalculatorThread extends Thread{
/*
	private Collection<GroupController> groupControllerList;
    private int interval = 120000;

    public PercentileCalculatorThread(Collection<GroupController> groupControllerList) {
        this.groupControllerList = groupControllerList;
    }

    public void run() {
        while(true) {
            try {
                try {
                    calculatePercentiles(false);
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (JSONException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                sleep(interval);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    public void calculatePercentiles(boolean allGroups) throws IOException, JSONException {
        Iterator<GroupController> controllers = this.groupControllerList.iterator();
        while(controllers.hasNext()) {
            GroupController groupController = controllers.next();
            if(allGroups == false) {
                if(groupController.isAlive()) {
                    groupController.calculatePercentiles();
                }
            }
            else {
                groupController.calculatePercentiles();
            }
        }
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }
*/
}
