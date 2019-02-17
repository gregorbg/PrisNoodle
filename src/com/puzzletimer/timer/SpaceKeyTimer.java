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

public class SpaceKeyTimer implements Timer {
    private enum State {
        READY_FOR_INSPECTION,
        READY,
        RUNNING,
        FINISHED,
    }

    protected static final int INTERMEDIARY_THRESHOLD = 350;

    private JFrame frame;
    private TimerManager timerManager;
    private boolean inspectionEnabled;
    private boolean phasesEnabled;
    private KeyListener keyListener;
    private TimerManager.Listener timerListener;
    private java.util.Timer repeater;
    private Date start;
    private Date finish;
    private List<Date> currentPhaseStamps;
    private State state;
    private int currentPhase;
    private int phaseTotal;

    public SpaceKeyTimer(JFrame frame, TimerManager timerManager) {
        this.frame = frame;
        this.timerManager = timerManager;
        this.inspectionEnabled = false;
        this.phasesEnabled = false;
        this.repeater = null;
        this.start = null;
        this.finish = new Date(0);
        this.currentPhaseStamps = new ArrayList<>();
        this.state = State.READY;
        this.currentPhase = 0;
        this.phaseTotal = 1;
    }

    @Override
    public String getTimerId() {
        return "KEYBOARD-TIMER-SPACE";
    }

    @Override
    public void setInspectionEnabled(boolean inspectionEnabled) {
        this.inspectionEnabled = inspectionEnabled;

        switch (this.state) {
            case READY_FOR_INSPECTION:
                if (!inspectionEnabled) this.state = State.READY;
                break;

            case READY:
                if (inspectionEnabled) this.state = State.READY_FOR_INSPECTION;
                break;
        }
    }

	@Override
	public void setPhaseTotal(int phaseTotal) {
		this.phaseTotal = phaseTotal;

    	this.currentPhase = 0;
		this.currentPhaseStamps.clear();
	}

    @Override
    public void setPhasesEnabled(boolean phasesEnabled) {
        this.phasesEnabled = phasesEnabled;

        this.currentPhase = 0;
        this.currentPhaseStamps.clear();
    }

	@Override
    public void setSmoothTimingEnabled(boolean smoothTimingEnabled) {
    }

    @Override
    public void start() {
        this.keyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() != KeyEvent.VK_SPACE && SpaceKeyTimer.this.state != State.RUNNING) return;

                if (SpaceKeyTimer.this.state == State.RUNNING) {
					SpaceKeyTimer.this.finish = new Date();

					if (SpaceKeyTimer.this.finish.getTime() - SpaceKeyTimer.this.start.getTime() >= INTERMEDIARY_THRESHOLD) {
						SpaceKeyTimer.this.currentPhase++;

						if (!SpaceKeyTimer.this.phasesEnabled || SpaceKeyTimer.this.currentPhase == SpaceKeyTimer.this.phaseTotal) {
							SpaceKeyTimer.this.repeater.cancel();

							SpaceKeyTimer.this.timerManager.finishSolution(
									new Timing(SpaceKeyTimer.this.start, SpaceKeyTimer.this.finish, SpaceKeyTimer.this.currentPhaseStamps));

							SpaceKeyTimer.this.currentPhase = 0;
							SpaceKeyTimer.this.currentPhaseStamps.clear();

							SpaceKeyTimer.this.state = State.FINISHED;
						} else {
							SpaceKeyTimer.this.currentPhaseStamps.add(SpaceKeyTimer.this.finish);
						}
					}
                }

                SpaceKeyTimer.this.timerManager.pressLeftHand();
                SpaceKeyTimer.this.timerManager.pressRightHand();
            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() != KeyEvent.VK_SPACE && SpaceKeyTimer.this.state != State.FINISHED) return;

                switch (SpaceKeyTimer.this.state) {
                    case READY_FOR_INSPECTION:
                        if (new Date().getTime() - SpaceKeyTimer.this.finish.getTime() < INTERMEDIARY_THRESHOLD) break;

                        SpaceKeyTimer.this.timerManager.startInspection();

                        SpaceKeyTimer.this.state = State.READY;
                        break;

                    case READY:
						if (new Date().getTime() - SpaceKeyTimer.this.finish.getTime() < INTERMEDIARY_THRESHOLD) break;

                        SpaceKeyTimer.this.timerManager.startSolution();

                        SpaceKeyTimer.this.start = new Date();
                        SpaceKeyTimer.this.repeater = new java.util.Timer();
                        SpaceKeyTimer.this.repeater.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                SpaceKeyTimer.this.timerManager.updateSolutionTiming(
                                        new Timing(SpaceKeyTimer.this.start, new Date()));
                            }
                        }, 0, 5);

                        SpaceKeyTimer.this.state = State.RUNNING;
                        break;

                    case FINISHED:
                        SpaceKeyTimer.this.state = SpaceKeyTimer.this.inspectionEnabled ?
                                State.READY_FOR_INSPECTION : State.READY;
                        break;
                }

                SpaceKeyTimer.this.timerManager.releaseLeftHand();
                SpaceKeyTimer.this.timerManager.releaseRightHand();
            }
        };
        this.frame.addKeyListener(this.keyListener);

        this.timerListener = new TimerManager.Listener() {
            @Override
            public void inspectionFinished() {
                SpaceKeyTimer.this.state = SpaceKeyTimer.this.inspectionEnabled ?
                        State.READY_FOR_INSPECTION : State.READY;
            }
        };
        this.timerManager.addListener(this.timerListener);
    }

    @Override
    public void stop() {
        if (this.repeater != null) this.repeater.cancel();

        this.frame.removeKeyListener(this.keyListener);
        this.timerManager.removeListener(this.timerListener);
    }
}
