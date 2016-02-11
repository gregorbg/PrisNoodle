package com.puzzletimer.puzzles;

import com.puzzletimer.models.ColorScheme;
import net.gnehzr.tnoodle.scrambles.InvalidScrambleException;

public class EmptyPuzzle implements Puzzle {
    @Override
    public String getPuzzleId() {
        return "EMPTY";
    }

    @Override
    public String getDescription() {
        return "An empty puzzle wrapper.";
    }

    @Override
    public net.gnehzr.tnoodle.scrambles.Puzzle getTNoodle() {
        return null;
    }

    @Override
    public String getScrambleSVG(String sequence, ColorScheme scheme) throws InvalidScrambleException {
        return "<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.0\" width=\"1\" height=\"1\"></svg>";
    }

    @Override
    public String toString() {
        return this.getPuzzleId();
    }
}
