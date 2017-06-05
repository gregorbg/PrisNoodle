package com.puzzletimer.timer;

public interface Timer {
    String getTimerId();

    void setInspectionEnabled(boolean inspectionEnabled);

    void setMemoSplitEnabled(boolean memoSplitEnabled);

    void setSmoothTimingEnabled(boolean smoothTimingEnabled);

    void start();

    void stop();
}
