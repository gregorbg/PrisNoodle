package com.puzzletimer.timer;

import com.puzzletimer.managers.TimerManager;
import com.puzzletimer.models.Timing;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;

public class ControlKeysTimer implements Timer {
    private enum State {
        READY_FOR_INSPECTION,
        NOT_READY,
        READY,
        RUNNING,
        FINISHED,
    };

    private JFrame frame;
    private TimerManager timerManager;
    private boolean inspectionEnabled;
    private boolean leftPressed;
    private boolean rightPressed;
    private KeyListener keyListener;
    private TimerManager.Listener timerListener;
    private java.util.Timer repeater;
    private Date start;
    private Date finish;
    private List<Date> phaseStamps;
    private State state;
    private int phase;
    private int phaseTotal;

    public ControlKeysTimer(JFrame frame, TimerManager timerManager) {
        this.frame = frame;
        this.timerManager = timerManager;
        this.inspectionEnabled = false;
        this.leftPressed = false;
        this.rightPressed = false;
        this.repeater = null;
        this.start = null;
        this.finish = new Date(0);
        this.phaseStamps = new ArrayList<>();
        this.state = State.NOT_READY;
        this.phase = 0;
        this.phaseTotal = 1;
    }

    @Override
    public String getTimerId() {
        return "KEYBOARD-TIMER-CONTROL";
    }

    @Override
    public void setInspectionEnabled(boolean inspectionEnabled) {
        this.inspectionEnabled = inspectionEnabled;

        switch (this.state) {
            case READY_FOR_INSPECTION:
                if (!inspectionEnabled) this.state = State.NOT_READY;
                break;

            case NOT_READY:
                if (inspectionEnabled) this.state = State.READY_FOR_INSPECTION;
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
    public void start() {
        this.keyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() != KeyEvent.VK_CONTROL) {
                    return;
                }

                switch (keyEvent.getKeyLocation()) {
                    case KeyEvent.KEY_LOCATION_LEFT:
                        ControlKeysTimer.this.leftPressed = true;
                        ControlKeysTimer.this.timerManager.pressLeftHand();
                        break;

                    case KeyEvent.KEY_LOCATION_RIGHT:
                        ControlKeysTimer.this.rightPressed = true;
                        ControlKeysTimer.this.timerManager.pressRightHand();
                        break;
                }

                switch (ControlKeysTimer.this.state) {
                    case READY_FOR_INSPECTION:
                        if (new Date().getTime() - ControlKeysTimer.this.finish.getTime() < 250) {
                            break;
                        }

                        ControlKeysTimer.this.timerManager.startInspection();

                        ControlKeysTimer.this.state = State.NOT_READY;
                        break;

                    case NOT_READY:
                        if (ControlKeysTimer.this.leftPressed && ControlKeysTimer.this.rightPressed) {
                            ControlKeysTimer.this.state = State.READY;
                        }

                        break;

                    case RUNNING:
                        if (ControlKeysTimer.this.leftPressed && ControlKeysTimer.this.rightPressed) {
                            ControlKeysTimer.this.finish = new Date();

                            if (ControlKeysTimer.this.finish.getTime() - ControlKeysTimer.this.start.getTime() < 350) {
                                break;
                            }

                            ControlKeysTimer.this.phase++;

                            if (ControlKeysTimer.this.phase == ControlKeysTimer.this.phaseTotal) {
								ControlKeysTimer.this.repeater.cancel();

								ControlKeysTimer.this.timerManager.finishSolution(
										new Timing(ControlKeysTimer.this.start, ControlKeysTimer.this.finish, ControlKeysTimer.this.phaseStamps)
								);

								ControlKeysTimer.this.phase = 0;
								ControlKeysTimer.this.phaseStamps.clear();

								ControlKeysTimer.this.state = State.FINISHED;
							} else {
                            	ControlKeysTimer.this.phaseStamps.add(ControlKeysTimer.this.finish);
							}
                        }
                        break;
                }
            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() != KeyEvent.VK_CONTROL) {
                    return;
                }

                switch (keyEvent.getKeyLocation()) {
                    case KeyEvent.KEY_LOCATION_LEFT:
                        ControlKeysTimer.this.leftPressed = false;
                        ControlKeysTimer.this.timerManager.releaseLeftHand();
                        break;

                    case KeyEvent.KEY_LOCATION_RIGHT:
                        ControlKeysTimer.this.rightPressed = false;
                        ControlKeysTimer.this.timerManager.releaseRightHand();
                        break;
                }

                switch (ControlKeysTimer.this.state) {
                    case READY:
                        if (new Date().getTime() - ControlKeysTimer.this.finish.getTime() < 250) {
                            break;
                        }

                        ControlKeysTimer.this.timerManager.startSolution();

                        ControlKeysTimer.this.start = new Date();
                        ControlKeysTimer.this.repeater = new java.util.Timer();
                        ControlKeysTimer.this.repeater.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                ControlKeysTimer.this.timerManager.updateSolutionTiming(
                                    new Timing(ControlKeysTimer.this.start, new Date()));
                            }
                        }, 0, 5);

                        ControlKeysTimer.this.state = State.RUNNING;
                        break;

                    case FINISHED:
                        if (!ControlKeysTimer.this.leftPressed && !ControlKeysTimer.this.rightPressed) {
                            ControlKeysTimer.this.state = ControlKeysTimer.this.inspectionEnabled ?
                                State.READY_FOR_INSPECTION : State.NOT_READY;
                        }
                        break;
                }
            }
        };
        this.frame.addKeyListener(this.keyListener);

        this.timerListener = new TimerManager.Listener() {
            @Override
            public void inspectionFinished() {
                ControlKeysTimer.this.state = ControlKeysTimer.this.inspectionEnabled ?
                    State.READY_FOR_INSPECTION : State.NOT_READY;
            }
        };
        this.timerManager.addListener(this.timerListener);
    }

    @Override
    public void stop() {
        if (this.repeater != null) {
            this.repeater.cancel();
        }

        this.frame.removeKeyListener(this.keyListener);
        this.timerManager.removeListener(this.timerListener);
    }

	@Override
	public void setSmoothTimingEnabled(boolean smoothTimingEnabled) {
	}
}
