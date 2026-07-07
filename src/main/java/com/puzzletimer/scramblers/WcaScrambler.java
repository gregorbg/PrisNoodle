package com.puzzletimer.scramblers;

import com.puzzletimer.models.Scramble;
import com.puzzletimer.puzzles.Puzzle;

public class WcaScrambler implements Scrambler {
    private Puzzle puzzle;

    public WcaScrambler(Puzzle puzzle) {
        this.puzzle = puzzle;
    }

    @Override
    public Scramble getNextScramble() {
        return new Scramble(this.getScramblerId(), this.puzzle.getTNoodle().generateScramble().split("\\s+?"));
    }

    @Override
    public Puzzle getPuzzle() {
        return this.puzzle;
    }

    public String getScramblerId() {
        return this.puzzle.getTNoodle().getShortName();
    }

    public String getDescription() {
        return "TNoodle: " + this.getScramblerId();
    }

    @Override
    public String toString() {
        return this.getDescription();
    }
}
