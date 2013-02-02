package com.open.perf.load;

public class FunctionStats {
	private String name;
    private int failureCount;
    private int errorCount;
    private float min, max, avg, dumpAvg, nineteeth, nintyFifth, nintyEight;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public float getMin() {
        return min;
    }

    public void setMin(float min) {
        this.min = min;
    }

    public float getMax() {
        return max;
    }

    public void setMax(float max) {
        this.max = max;
    }

    public float getAvg() {
        return avg;
    }

    public void setAvg(float avg) {
        this.avg = avg;
    }

    public float getDumpAvg() {
        return dumpAvg;
    }

    public void setDumpAvg(float dumpAvg) {
        this.dumpAvg = dumpAvg;
    }

    public float getNineteeth() {
        return nineteeth;
    }

    public void setNineteeth(float nineteeth) {
        this.nineteeth = nineteeth;
    }

    public float getNintyFifth() {
        return nintyFifth;
    }

    public void setNintyFifth(float nintyFifth) {
        this.nintyFifth = nintyFifth;
    }

    public float getNintyEight() {
        return nintyEight;
    }

    public void setNintyEight(float nintyEight) {
        this.nintyEight = nintyEight;
    }

}
