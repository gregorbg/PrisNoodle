package com.puzzletimer.statistics;

import com.puzzletimer.models.Solution;
import com.puzzletimer.util.SolutionUtils;

public interface StatisticalMeasure {
    int getMinimumWindowSize();

    int getMaximumWindowSize();

    int getWindowPosition();

    long getValue();

    default String toFormatString() {
        return SolutionUtils.format(this.getValue(), this.getRound());
    }
    
    default String toFullString() {
        return SolutionUtils.formatMinutes(this.getValue(), this.getRound());
    }

    boolean getRound();

    void setSolutions(Solution[] solutions);
}
