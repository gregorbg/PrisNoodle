package com.puzzletimer.models;

import com.suushiemaniac.cubing.alglib.alg.Algorithm;
import com.suushiemaniac.cubing.bld.model.enumeration.puzzle.TwistyPuzzle;

public class Scramble {
    private final String scramblerId;
    private final String[] sequence;

    public Scramble(String scramblerId, String[] sequence) {
        this.scramblerId = scramblerId;
        this.sequence = sequence;
    }

    public String getScramblerId() {
        return this.scramblerId;
    }

    public String[] getSequence() {
        return this.sequence;
    }

    public String getRawSequence() {
        return String.join(" ", this.sequence);
    }

    public Algorithm parseFor(TwistyPuzzle puzzle) {
    	return puzzle.getReader().parse(this.getRawSequence());
	}
}
