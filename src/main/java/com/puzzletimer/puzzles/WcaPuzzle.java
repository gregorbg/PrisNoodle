package com.puzzletimer.puzzles;

import com.puzzletimer.models.ColorScheme;
import org.worldcubeassociation.tnoodle.scrambles.InvalidScrambleException;

import static com.puzzletimer.Internationalization.i18n;

public class WcaPuzzle implements Puzzle {
    private org.worldcubeassociation.tnoodle.scrambles.Puzzle tNoodle;

    public WcaPuzzle(org.worldcubeassociation.tnoodle.scrambles.Puzzle tNoodle) {
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
    public org.worldcubeassociation.tnoodle.scrambles.Puzzle getTNoodle() {
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
