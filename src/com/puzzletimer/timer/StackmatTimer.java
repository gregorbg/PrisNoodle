// reference: http://hackvalue.de/hv_atmel_stackmat

package com.puzzletimer.timer;

import com.puzzletimer.managers.TimerManager;
import com.puzzletimer.models.Timing;

import javax.sound.sampled.TargetDataLine;
import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.*;

public class StackmatTimer implements StackmatTimerReader.StackmatTimerReaderListener, Timer {
    private enum State {
        NOT_READY,
        RESET_FOR_INSPECTION,
        RESET,
        READY,
        RUNNING,
    }

    private JFrame frame;
    private StackmatTimerReader stackmatTimerReader;
    private TimerManager timerManager;
    private boolean smoothTimingEnabled;
    private boolean inspectionEnabled;
    private KeyListener keyListener;
    private TimerManager.Listener timerListener;
    private java.util.Timer repeater;
    private Date start;
    private Date end;
    private List<Date> phaseStamps;
    private State state;
    private long previousTime;
    private int phase;
    private int phaseTotal;

    public StackmatTimer(JFrame frame, TargetDataLine targetDataLine, TimerManager timerManager) {
        this.frame = frame;
    	this.stackmatTimerReader = new StackmatTimerReader(targetDataLine, timerManager);
        this.timerManager = timerManager;
        this.smoothTimingEnabled = true;
        this.inspectionEnabled = false;
        this.start = null;
        this.end = new Date(0);
        this.phaseStamps = new ArrayList<>();
        this.state = State.NOT_READY;
        this.previousTime = -1;
        this.phase = 0;
        this.phaseTotal = 1;
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
    public void setMemoSplitEnabled(boolean memoSplitEnabled) {
        this.phaseTotal = memoSplitEnabled ? 2 : 1;

        this.phase = 0;
        this.phaseStamps.clear();
    }

    @Override
    public void setSmoothTimingEnabled(boolean smoothTimingEnabled) {
        this.smoothTimingEnabled = smoothTimingEnabled;
    }

    @Override
    public void start() {
    	this.keyListener = new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent keyEvent) {
				if (keyEvent.getKeyCode() != KeyEvent.VK_SPACE && StackmatTimer.this.state != State.RUNNING) return;

				if (StackmatTimer.this.state == State.RUNNING) {
					Date finish = new Date();

					if (finish.getTime() - StackmatTimer.this.start.getTime() >= 350) {
						StackmatTimer.this.phase++;

						if (StackmatTimer.this.phase < StackmatTimer.this.phaseTotal) {
							StackmatTimer.this.phaseStamps.add(finish);
						}
					}
				}
			}
		};
    	this.frame.addKeyListener(this.keyListener);

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
    	this.frame.removeKeyListener(this.keyListener);
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

        this.end = new Date();
        this.start = new Date(this.end.getTime() - time);

        Timing timing = new Timing(this.start, this.end, this.phaseStamps);

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

                    this.phase = 0;
                    this.phaseStamps.clear();
                }

                break;
        }
        this.previousTime = time;
    }
}