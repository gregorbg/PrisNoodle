package com.puzzletimer.timer;

import com.puzzletimer.managers.TimerManager;
import com.puzzletimer.models.Timing;
import com.puzzletimer.util.SolutionUtils;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ManualInputTimer implements Timer {
	private static final String TIME_FORMAT = "(?:\\d{1,2})?:\\d{2}\\.\\d{2,3}";
    private static final Pattern TIME_PATTERN = Pattern.compile("(" + TIME_FORMAT + ")\\s?(?:\\[(" + TIME_FORMAT + ")(?:/(" + TIME_FORMAT + "))*" + "\\])?");

    private TimerManager timerManager;
    private KeyListener keyListener;
    private Date start;
    private JTextField textFieldTime;

    public ManualInputTimer(TimerManager timerManager, JTextField textFieldTime) {
        this.textFieldTime = textFieldTime;
        this.timerManager = timerManager;
        this.start = null;
    }

    @Override
    public String getTimerId() {
        return "MANUAL-INPUT";
    }

    @Override
    public void start() {
        this.keyListener = new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent keyEvent) {
			if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
				ManualInputTimer.this.start = new Date();
				Matcher matcher = TIME_PATTERN.matcher(ManualInputTimer.this.textFieldTime.getText());

				if (matcher.find()) {
					long time = SolutionUtils.parseTime(matcher.group(1));
					List<Date> phases = new ArrayList<>();

					for (int i = 2; i < matcher.groupCount(); i++) {
						long groupTime = SolutionUtils.parseTime(matcher.group(i));
						phases.add(new Date(ManualInputTimer.this.start.getTime() + groupTime));
					}

					Timing timing =
							new Timing(
									ManualInputTimer.this.start,
									new Date(ManualInputTimer.this.start.getTime() + time),
									phases
							);

					ManualInputTimer.this.timerManager.finishSolution(timing);
					ManualInputTimer.this.textFieldTime.setText(null);
				}
			}
            }
        };

        this.textFieldTime.addKeyListener(this.keyListener);
    }

    @Override
    public void setInspectionEnabled(boolean inspectionEnabled) {
    }

	@Override
	public void setPhasesEnabled(boolean phasesEnabled) {
	}

    @Override
    public void setPhaseTotal(int phaseTotal) {
    }

    @Override
    public void stop() {
        this.textFieldTime.removeKeyListener(this.keyListener);
    }

	@Override
	public void setSmoothTimingEnabled(boolean smoothTimingEnabled) {
	}
}
