package com.puzzletimer.statistics;

import com.puzzletimer.models.Solution;

public class Accuracy implements StatisticalMeasure {
    private int minimumWindowSize;
    private int maximumWindowSize;
    private long attempted, solved;

    public Accuracy(int minimumWindowSize, int maximumWindowSize) {
        this.minimumWindowSize = minimumWindowSize;
        this.maximumWindowSize = maximumWindowSize;
        this.attempted = 0;
        this.solved = 0;
    }

    @Override
    public int getMinimumWindowSize() {
        return this.minimumWindowSize;
    }

    @Override
    public int getMaximumWindowSize() {
        return this.maximumWindowSize;
    }

    @Override
    public int getWindowPosition() {
        return 0;
    }

    @Override
    public long getValue() {
        return this.solved;
    }

    @Override
    public String toFormatString() {
        return this.solved + " / " + this.attempted + " = " + Math.round((this.solved / (float) this.attempted) * 100) + "%";
    }

    @Override
    public String toFullString() {
        return this.toFormatString();
    }

    @Override
    public boolean getRound() {
        return false;
    }

    @Override
    public void setSolutions(Solution[] solutions) {
        this.attempted = solutions.length;
        this.solved = 0;
        for (Solution s : solutions) if (!"DNF".equals(s.getPenalty())) this.solved++;
    }
}
