package com.puzzletimer.statistics;

import com.puzzletimer.models.Solution;
import com.puzzletimer.util.SolutionUtils;

import java.util.Arrays;
import java.util.UUID;

public class MeanPercent implements StatisticalMeasure {
    private int minimumWindowSize;
    private int maximumWindowSize;
    private long value;

    public MeanPercent(int minimumWindowSize, int maximumWindowSize) {
        this.minimumWindowSize = minimumWindowSize;
        this.maximumWindowSize = maximumWindowSize;
        this.value = 0;
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
        return this.value;
    }

    @Override
    public boolean getRound() {
        return false;
    }

    @Override
    public void setSolutions(Solution[] solutions) {
        Solution[] solCopy = Arrays.copyOf(solutions, solutions.length);
        Arrays.sort(solCopy, (sol1, sol2) -> -1 * Long.compare(sol1.getTiming().getElapsedTime(), sol2.getTiming().getElapsedTime()));

        while (SolutionUtils.dnfCount(solCopy) < solCopy.length / 2) {
            for (int i = 0; i < solCopy.length; i++) if (!"DNF".equals(solCopy[i].getPenalty())) {
                solCopy[i] = solCopy[i].setPenalty("DNF");
                break;
            }
        }

        Arrays.sort(solCopy, (sol1, sol2) -> -1 * Integer.compare(sol1.getPenalty().length(), sol2.getPenalty().length()));

        Solution[] nonDNF = new Solution[solCopy.length / 2 + (solCopy.length % 2 == 0 ? 0 : 1)];
        System.arraycopy(solCopy, solCopy.length / 2, nonDNF, 0, nonDNF.length);

        Mean actualMean = new Mean(0, Integer.MAX_VALUE);
        actualMean.setSolutions(nonDNF);
        this.value = actualMean.getValue();
    }
}
