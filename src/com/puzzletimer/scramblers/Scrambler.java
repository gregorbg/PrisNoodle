package com.puzzletimer.scramblers;

import com.puzzletimer.models.Scramble;
import com.puzzletimer.puzzles.Puzzle;

public interface Scrambler {
    Scramble getNextScramble();

    Puzzle getPuzzle();

    String getScramblerId();

    String getDescription();
}
