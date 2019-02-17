package com.puzzletimer.scramblers;

import com.puzzletimer.models.Scramble;
import com.puzzletimer.puzzles.EmptyPuzzle;
import com.puzzletimer.puzzles.Puzzle;

public class EmptyScrambler implements Scrambler {
    @Override
    public Scramble getNextScramble() {
        return new Scramble(this.getScramblerId(), "".split(""));
    }

    @Override
    public Puzzle getPuzzle() {
        return new EmptyPuzzle();
    }

    @Override
    public String getScramblerId() {
        return "EMPTY";
    }

    @Override
    public String getDescription() {
        return "An empty scrambler producing no scrambles";
    }

    @Override
    public String toString() {
        return this.getScramblerId();
    }
}
