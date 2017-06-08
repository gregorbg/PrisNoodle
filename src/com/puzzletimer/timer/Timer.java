package com.puzzletimer.timer;

public interface Timer {
    String getTimerId();

    void setInspectionEnabled(boolean inspectionEnabled);

    void setSmoothTimingEnabled(boolean smoothTimingEnabled);

    void setPhasesEnabled(boolean phasesEnabled);

    void setPhaseTotal(int phaseTotal);

    void start();

    void stop();
}
