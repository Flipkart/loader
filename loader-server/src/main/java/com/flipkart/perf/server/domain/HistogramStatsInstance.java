package com.flipkart.perf.server.domain;

import java.util.Date;

/**
     * Bean class to collect stats and write to file
     */
    public class HistogramStatsInstance {
        private Date time;
        private long opsDone;
        private double min, max;
        private double dumpMean, overallMean, SD, fiftieth, seventyFifth, ninetieth, ninetyFifth, ninetyEight, ninetyNinth, nineNineNine;

        public Date getTime() {
            return time;
        }

        public HistogramStatsInstance setTime(Date time) {
            this.time = time;
            return this;
        }

        public long getOpsDone() {
            return opsDone;
        }

        public HistogramStatsInstance setOpsDone(long opsDone) {
            this.opsDone = opsDone;
            return this;
        }

        public double getMin() {
            return min;
        }

        public HistogramStatsInstance setMin(double min) {
            this.min = min;
            return this;
        }

        public double getMax() {
            return max;
        }

        public HistogramStatsInstance setMax(double max) {
            this.max = max;
            return this;
        }

        public double getDumpMean() {
            return dumpMean;
        }

        public HistogramStatsInstance setDumpMean(double dumpMean) {
            this.dumpMean = dumpMean;
            return this;
        }

        public double getOverallMean() {
            return overallMean;
        }

        public HistogramStatsInstance setOverallMean(double overallMean) {
            this.overallMean = overallMean;
            return this;
        }

        public double getSD() {
            return SD;
        }

        public HistogramStatsInstance setSD(double SD) {
            this.SD = SD;
            return this;
        }

        public double getFiftieth() {
            return fiftieth;
        }

        public HistogramStatsInstance setFiftieth(double fiftieth) {
            this.fiftieth = fiftieth;
            return this;
        }

        public double getSeventyFifth() {
            return seventyFifth;
        }

        public HistogramStatsInstance setSeventyFifth(double seventyFifth) {
            this.seventyFifth = seventyFifth;
            return this;
        }

        public double getNinetieth() {
            return ninetieth;
        }

        public HistogramStatsInstance setNinetieth(double ninetieth) {
            this.ninetieth = ninetieth;
            return this;
        }

        public double getNinetyFifth() {
            return ninetyFifth;
        }

        public HistogramStatsInstance setNinetyFifth(double ninetyFifth) {
            this.ninetyFifth = ninetyFifth;
            return this;
        }

        public double getNinetyEight() {
            return ninetyEight;
        }

        public HistogramStatsInstance setNinetyEight(double ninetyEight) {
            this.ninetyEight = ninetyEight;
            return this;
        }

        public double getNinetyNinth() {
            return ninetyNinth;
        }

        public HistogramStatsInstance setNinetyNinth(double ninetyNinth) {
            this.ninetyNinth = ninetyNinth;
            return this;
        }

        public double getNineNineNine() {
            return nineNineNine;
        }

        public HistogramStatsInstance setNineNineNine(double nineNineNine) {
            this.nineNineNine = nineNineNine;
            return this;
        }
    }
