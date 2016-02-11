package com.puzzletimer.puzzles;

import com.puzzletimer.models.ColorScheme;
import net.gnehzr.tnoodle.scrambles.InvalidScrambleException;

import static com.puzzletimer.Internationalization.i18n;

public class WcaPuzzle implements Puzzle {
    private net.gnehzr.tnoodle.scrambles.Puzzle tNoodle;

    public WcaPuzzle(net.gnehzr.tnoodle.scrambles.Puzzle tNoodle) {
        this.tNoodle = tNoodle;
    }

    @Override
    public String getPuzzleId() {
        return this.tNoodle.getLongName().replace("no inspection", "").trim();
    }

    @Override
    public String getDescription() {
        return i18n("puzzle." + this.getPuzzleId());
    }

    @Override
    public net.gnehzr.tnoodle.scrambles.Puzzle getTNoodle() {
        return this.tNoodle;
    }

    @Override
    public String getScrambleSVG(String sequence, ColorScheme scheme) throws InvalidScrambleException {
        return this.tNoodle.drawScramble(sequence, scheme.toTNoodle()).toString();
    }

    @Override
    public String toString() {
        return this.getPuzzleId();
    }
}
