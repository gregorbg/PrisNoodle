// reference: http://hackvalue.de/hv_atmel_stackmat

package com.puzzletimer.timer;

import com.puzzletimer.managers.TimerManager;
import com.puzzletimer.models.Timing;

import javax.sound.sampled.TargetDataLine;
import java.util.Date;
import java.util.TimerTask;

public class StackmatTimer implements StackmatTimerReader.StackmatTimerReaderListener, Timer {
    private enum State {
        NOT_READY,
        RESET_FOR_INSPECTION,
        RESET,
        READY,
        RUNNING,
    }

    private StackmatTimerReader stackmatTimerReader;
    private TimerManager timerManager;
    private boolean smoothTimingEnabled;
    private boolean inspectionEnabled;
    private TimerManager.Listener timerListener;
    private java.util.Timer repeater;
    private Date start;
    private State state;
    private long previousTime;

    public StackmatTimer(TargetDataLine targetDataLine, TimerManager timerManager) {
        this.stackmatTimerReader = new StackmatTimerReader(targetDataLine, timerManager);
        this.timerManager = timerManager;
        this.smoothTimingEnabled = true;
        this.inspectionEnabled = false;
        this.start = null;
        this.state = State.NOT_READY;
        this.previousTime = -1;
    }

    @Override
    public String getTimerId() {
        return "STACKMAT-TIMER";
    }

    @Override
    public void setInspectionEnabled(boolean inspectionEnabled) {
        this.inspectionEnabled = inspectionEnabled;

        switch (this.state) {
            case RESET_FOR_INSPECTION:
                if (!inspectionEnabled) this.state = State.RESET;
                break;

            case RESET:
                if (inspectionEnabled) this.state = State.RESET_FOR_INSPECTION;
                break;
        }
    }

    @Override
    public void setSmoothTimingEnabled(boolean smoothTimingEnabled) {
        this.smoothTimingEnabled = smoothTimingEnabled;
    }

    @Override
    public void start() {
        this.timerListener = new TimerManager.Listener() {
            @Override
            public void inspectionFinished() {
                StackmatTimer.this.state = State.NOT_READY;
            }
        };
        this.timerManager.addListener(this.timerListener);

        this.stackmatTimerReader.addEventListener(this);
        Thread readerThread = new Thread(this.stackmatTimerReader);
        readerThread.start();

        this.repeater = new java.util.Timer();
        this.repeater.schedule(new TimerTask() {
            @Override
            public void run() {
                if (StackmatTimer.this.state == State.RUNNING)
                    if (smoothTimingEnabled) StackmatTimer.this.timerManager.updateSolutionTiming(
                            new Timing(StackmatTimer.this.start, new Date()));
            }
        }, 0, 5);
    }

    @Override
    public void stop() {
        this.timerManager.removeListener(this.timerListener);

        this.stackmatTimerReader.removeEventListener(this);
        this.stackmatTimerReader.stop();

        this.repeater.cancel();
    }

    @Override
    public void dataReceived(byte[] data, boolean hasSixDigits) {
        // hands status
        if (data[0] == 'A' || data[0] == 'L' || data[0] == 'C') this.timerManager.pressLeftHand();
        else this.timerManager.releaseLeftHand();

        if (data[0] == 'A' || data[0] == 'R' || data[0] == 'C') this.timerManager.pressRightHand();
        else this.timerManager.releaseRightHand();

        // time
        int minutes = data[1] - '0';
        int seconds = 10 * (data[2] - '0') + data[3] - '0';
        long time;
        if (hasSixDigits) {
            int milliseconds = 100 * (data[4] - '0') + 10 * (data[5] - '0') + data[6] - '0';
            time = 60000 * minutes + 1000 * seconds + milliseconds;
        } else {
            int centiseconds = 10 * (data[4] - '0') + data[5] - '0';
            time = 60000 * minutes + 1000 * seconds + 10 * centiseconds;
        }

        Date end = new Date();
        Date start = new Date(end.getTime() - time);
        Timing timing = new Timing(start, end);

        this.start = start;

        // state transitions
        switch (this.state) {
            case NOT_READY:
                this.timerManager.updateSolutionTiming(timing);

                // timer initialized
                if (time == 0) {
                    this.timerManager.resetTimer();
                    this.state = this.inspectionEnabled ? State.RESET_FOR_INSPECTION : State.RESET;
                }
                break;

            case RESET_FOR_INSPECTION:
                // some pad pressed
                if (data[0] == 'L' || data[0] == 'R' || data[0] == 'C') {
                    this.timerManager.startInspection();
                    this.state = State.RESET;
                }
                break;

            case RESET:
                if (!this.inspectionEnabled) this.timerManager.updateSolutionTiming(timing);

                // ready to start
                if (data[0] == 'A') this.state = State.READY;

                // timing started
                if (time > 0) {
                    this.timerManager.startSolution();
                    this.state = State.RUNNING;
                }
                break;

            case READY:
                this.timerManager.updateSolutionTiming(timing);

                // timing started
                if (time > 0) {
                    this.timerManager.startSolution();
                    this.state = State.RUNNING;
                }
                break;

            case RUNNING:
                this.timerManager.updateSolutionTiming(timing);

                // timer reset during solution
                if (time == 0) this.state = State.NOT_READY;

                // timer stopped
                if (data[0] == 'S' || time == previousTime) {
                    this.state = State.NOT_READY;
                    this.timerManager.finishSolution(timing);
                }

                break;
        }
        this.previousTime = time;
    }
}