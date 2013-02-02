package com.open.perf.domain;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public class TimerBean {
		@JsonProperty
		private String name;
		@JsonProperty
	 	private String delayAfterRepeats;
		@JsonProperty
		private long runtime;
		@JsonProperty
		private long repeats ;
		@JsonProperty
		private int threads ;
		@JsonProperty
		private long startTime;
		@JsonProperty
		private long repeatsDone;

		@JsonCreator
	    public TimerBean(String name){
	    	this.name = name;
	    	this.delayAfterRepeats = "0,0";
	    	this.repeats=-1;
	    	this.threads =1;
	    }
	    
	    public String getName() {
	        return name;
	    }

	    public TimerBean setName(String name) {
	        this.name = name;
	        return this;
	    }

	    public String getDelayAfterRepeats() {
	        return delayAfterRepeats;
	    }

	    public TimerBean setDelayAfterRepeats(String delayAfterRepeats) {
	        this.delayAfterRepeats = delayAfterRepeats;
	        return this;
	    }

	    public long getRuntime() {
	        return runtime;
	    }

	    public TimerBean setRuntime(long runtime) {
	        this.runtime = runtime;
	        return this;
	    }

	    public int getThreads() {
	        return threads;
	    }

	    public TimerBean setThreads(int threads) {
	        this.threads = threads;
	        return this;
	    }

	    public long getRepeats() {
	        return repeats;
	    }

	    public TimerBean setRepeats(long repeats) {
	        this.repeats = repeats;
	        return this;
	    }

	    public TimerBean clone() throws CloneNotSupportedException {
	        return (TimerBean) super.clone();
	    }

	    public TimerBean setStartTime(long startTime) {
	        this.startTime = startTime;
	        return this;
	    }

	    public long getStartTime() {
	        return this.startTime;
	    }

	    public TimerBean incrementRepeatsDone() {
	        this.repeatsDone++;
	        return this;
	    }

	    public long getRepeatsDone() {
	        return repeatsDone;
	    }

	    public TimerBean setRepeatsDone(long repeatsDone) {
	        this.repeatsDone = repeatsDone;
	        return this;
	    }

	    public String toString() {
	        return this.name +" threads : "+this.threads+" runtime : "+this.runtime+" repeats : "+this.repeats;
	    }

}
