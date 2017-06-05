package com.puzzletimer.models;

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
}
