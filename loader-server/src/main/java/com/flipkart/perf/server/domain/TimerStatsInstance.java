package com.flipkart.perf.server.domain;

import java.util.Date;

/**
     * Bean class to collect stats and write to file
     */
    public class TimerStatsInstance {
        private Date time;
        private long opsDone;
        private double min, max;
        private double dumpMean, dumpThroughput, overallMean, overAllThroughput, SD, fiftieth, seventyFifth, ninetieth, ninetyFifth, ninetyEight, ninetyNinth, nineNineNine;

        public Date getTime() {
            return time;
        }

        public TimerStatsInstance setTime(Date time) {
            this.time = time;
            return this;
        }

        public long getOpsDone() {
            return opsDone;
        }

        public TimerStatsInstance setOpsDone(long opsDone) {
            this.opsDone = opsDone;
            return this;
        }

        public double getMin() {
            return min;
        }

        public TimerStatsInstance setMin(double min) {
            this.min = min;
            return this;
        }

        public double getMax() {
            return max;
        }

        public TimerStatsInstance setMax(double max) {
            this.max = max;
            return this;
        }

        public double getDumpMean() {
            return dumpMean;
        }

        public TimerStatsInstance setDumpMean(double dumpMean) {
            this.dumpMean = dumpMean;
            return this;
        }

        public double getDumpThroughput() {
            return dumpThroughput;
        }

        public TimerStatsInstance setDumpThroughput(double dumpThroughput) {
            this.dumpThroughput = dumpThroughput;
            return this;
        }

        public double getOverallMean() {
            return overallMean;
        }

        public TimerStatsInstance setOverallMean(double overallMean) {
            this.overallMean = overallMean;
            return this;
        }

        public double getOverAllThroughput() {
            return overAllThroughput;
        }

        public TimerStatsInstance setOverAllThroughput(double overAllThroughput) {
            this.overAllThroughput = overAllThroughput;
            return this;
        }

        public double getSD() {
            return SD;
        }

        public TimerStatsInstance setSD(double SD) {
            this.SD = SD;
            return this;
        }

        public double getFiftieth() {
            return fiftieth;
        }

        public TimerStatsInstance setFiftieth(double fiftieth) {
            this.fiftieth = fiftieth;
            return this;
        }

        public double getSeventyFifth() {
            return seventyFifth;
        }

        public TimerStatsInstance setSeventyFifth(double seventyFifth) {
            this.seventyFifth = seventyFifth;
            return this;
        }

        public double getNinetieth() {
            return ninetieth;
        }

        public TimerStatsInstance setNinetieth(double ninetieth) {
            this.ninetieth = ninetieth;
            return this;
        }

        public double getNinetyFifth() {
            return ninetyFifth;
        }

        public TimerStatsInstance setNinetyFifth(double ninetyFifth) {
            this.ninetyFifth = ninetyFifth;
            return this;
        }

        public double getNinetyEight() {
            return ninetyEight;
        }

        public TimerStatsInstance setNinetyEight(double ninetyEight) {
            this.ninetyEight = ninetyEight;
            return this;
        }

        public double getNinetyNinth() {
            return ninetyNinth;
        }

        public TimerStatsInstance setNinetyNinth(double ninetyNinth) {
            this.ninetyNinth = ninetyNinth;
            return this;
        }

        public double getNineNineNine() {
            return nineNineNine;
        }

        public TimerStatsInstance setNineNineNine(double nineNineNine) {
            this.nineNineNine = nineNineNine;
            return this;
        }
    }
