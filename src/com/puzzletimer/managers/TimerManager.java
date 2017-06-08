package com.puzzletimer.managers;

import com.puzzletimer.models.Timing;
import com.puzzletimer.timer.Timer;

import java.util.ArrayList;
import java.util.Date;
import java.util.TimerTask;

public class TimerManager {
    public static class Listener {
        // timer
        public void timerChanged(Timer timer) {
        }

        public void timerReset() {
        }

        public void smoothTimingSet(boolean smoothTimingEnabled) {
        }

        // hands
        public void leftHandPressed() {
        }

        public void leftHandReleased() {
        }

        public void rightHandPressed() {
        }

        public void rightHandReleased() {
        }

        // inspection
        public void inspectionEnabledSet(boolean inspectionEnabled) {
        }

        public void inspectionStarted() {
        }

        public void inspectionRunning(long remainingTime) {
        }

        public void inspectionFinished() {
        }

        //memo
        public void phasesEnabledSet(boolean phasesEnabled) {
        }

        // solution
        public void solutionStarted() {
        }

        public void solutionRunning(Timing timing) {
        }

        public void solutionFinished(Timing timing, String penalty) {
        }

        public void dataNotReceived(byte[] data) {
        }
    }

    private ArrayList<Listener> listeners;
    private Timer currentTimer;
    private boolean smoothTimingEnabled;
    private boolean inspectionEnabled;
    private boolean phasesEnabled;
    private java.util.Timer repeater;
    private Date inspectionStart;
    private String penalty;

    public TimerManager() {
        this.listeners = new ArrayList<>();
        this.currentTimer = null;
        this.smoothTimingEnabled = true;
        this.inspectionEnabled = false;
        this.phasesEnabled = false;
        this.repeater = null;
        this.inspectionStart = null;
        this.penalty = "";
    }


    // timer

    public void setTimer(Timer timer) {
        // suspend running inspection
        if (this.inspectionStart != null) {
            this.repeater.cancel();
            this.inspectionStart = null;
            this.penalty = "";
        }

        if (this.currentTimer != null) this.currentTimer.stop();

        this.currentTimer = timer;
        this.currentTimer.setInspectionEnabled(this.inspectionEnabled);
        this.currentTimer.setPhasesEnabled(this.phasesEnabled);

        for (Listener listener : this.listeners) listener.timerChanged(timer);

        this.currentTimer.start();
    }

    public Timer getTimer() {
        return this.currentTimer;
    }

    public void resetTimer() {
        this.listeners.forEach(TimerManager.Listener::timerReset);
    }

    // hands

    public void pressLeftHand() {
        this.listeners.forEach(TimerManager.Listener::leftHandPressed);
    }

    public void releaseLeftHand() {
        this.listeners.forEach(TimerManager.Listener::leftHandReleased);
    }

    public void pressRightHand() {
        this.listeners.forEach(TimerManager.Listener::rightHandPressed);
    }

    public void releaseRightHand() {
        this.listeners.forEach(TimerManager.Listener::rightHandReleased);
    }

    public boolean isSmoothTimingEnabled() {
        return this.smoothTimingEnabled;
    }

    public void setSmoothTimingEnabled(boolean smoothTimingEnabled) {
        this.smoothTimingEnabled = smoothTimingEnabled;

        if (this.currentTimer != null) {
            this.currentTimer.setSmoothTimingEnabled(smoothTimingEnabled);
        }

        for (Listener listener : this.listeners) {
            listener.smoothTimingSet(smoothTimingEnabled);
        }
    }

    // inspection

    public boolean isInspectionEnabled() {
        return this.inspectionEnabled;
    }

    public void setInspectionEnabled(boolean inspectionEnabled) {
        this.inspectionEnabled = inspectionEnabled;

        if (this.currentTimer != null)
            this.currentTimer.setInspectionEnabled(inspectionEnabled);

        for (Listener listener : this.listeners) listener.inspectionEnabledSet(inspectionEnabled);
    }

    // phases

    public boolean isPhasesEnabled() {
        return this.phasesEnabled;
    }

    public void setPhasesEnabled(boolean phasesEnabled) {
        this.phasesEnabled = phasesEnabled;

        if (currentTimer != null)
            this.currentTimer.setPhasesEnabled(phasesEnabled);

        for (Listener listener : this.listeners) listener.phasesEnabledSet(phasesEnabled);
    }

    public void startInspection() {
        TimerManager.this.listeners.forEach(TimerManager.Listener::inspectionStarted);

        this.inspectionStart = new Date();
        this.penalty = "";

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                long start = TimerManager.this.inspectionStart.getTime();
                long now = new Date().getTime();

                for (Listener listener : TimerManager.this.listeners) listener.inspectionRunning(15000 - (now - start));

                if (now - start > 17000) {
                    TimerManager.this.repeater.cancel();

                    TimerManager.this.listeners.forEach(TimerManager.Listener::inspectionFinished);

                    TimerManager.this.inspectionStart = null;
                    TimerManager.this.penalty = "DNF";

                    finishSolution(new Timing(new Date(now), new Date(now)));
                } else if (now - start > 15000) TimerManager.this.penalty = "+2";
            }
        };

        this.repeater = new java.util.Timer();
        this.repeater.schedule(timerTask, 0, 10);
    }


    // solution

    public void startSolution() {
        if (this.inspectionStart != null) {
            this.repeater.cancel();
            this.inspectionStart = null;

            TimerManager.this.listeners.forEach(TimerManager.Listener::inspectionFinished);
        }

        this.listeners.forEach(TimerManager.Listener::solutionStarted);
    }

    public void updateSolutionTiming(Timing timing) {
        for (Listener listener : this.listeners) listener.solutionRunning(timing);
    }

    public void finishSolution(Timing timing) {
        for (Listener listener : this.listeners) listener.solutionFinished(timing, this.penalty);

        this.penalty = "";
    }

    public void dataNotReceived(byte[] data) {
        for (Listener listener : this.listeners) {
            listener.dataNotReceived(data);
        }
    }


    // listeners

    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        this.listeners.remove(listener);
    }
}
