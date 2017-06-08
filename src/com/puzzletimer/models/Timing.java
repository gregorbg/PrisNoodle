package com.puzzletimer.models;

import com.puzzletimer.util.SolutionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class Timing {
    private final Date start;
    private final Date end;
    private final List<Date> phases;

    public Timing(Date start, Date end, List<Date> phases) {
        this.start = start;
        this.end = end;

        this.phases = new ArrayList<>(phases);
    }

    public Timing(Date start, Date end) {
        this(start, end, Collections.emptyList());
    }

    public Date getStart() {
        return this.start;
    }

    public Date getEnd() {
        return this.end;
    }

    public List<Date> getPhases() {
        return new ArrayList<>(this.phases);
    }

    public Date getPhase(int index) {
        return this.phases.get(index);
    }

    public long getElapsedTime() {
        return this.end == null ?
                new Date().getTime() - this.start.getTime() :
                this.end.getTime() - this.start.getTime();
    }

	public List<Long> getElapsedPhases() {
		return this.phases.stream()
				.map(d -> d.getTime() - this.start.getTime())
				.collect(Collectors.toList());
	}

	public String toFormatString() {
        String elapsed = SolutionUtils.formatMinutes(this.getElapsedTime(), false);
        String phases = String.join("/", this.getElapsedPhases().stream().map(t -> SolutionUtils.formatMinutes(t, false)).collect(Collectors.toList()));

        String format = elapsed + " [" + phases + "]";
        return format.replace(" []", "");
    }
}
