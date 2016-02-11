package com.puzzletimer.tips;

import com.puzzletimer.models.Scramble;
import com.puzzletimer.puzzles.Puzzle;
import com.suushiemaniac.bld.analyze.ThreeBldCube;

import static com.puzzletimer.Internationalization.i18n;

public class RubiksCubeBldRotation implements Tip {
    @Override
    public String getTipId() {
        return "RUBIKS-CUBE-BLD-ROTATION";
    }

    @Override
    public String getPuzzleId() {
        return Puzzle.THREE;
    }

    @Override
    public String getTipDescription() {
        return i18n("tip." + this.getTipId());
    }

    @Override
    public String getTip(Scramble scramble) {
        ThreeBldCube analyze = new ThreeBldCube(scramble.getRawSequence());

        return this.getTipDescription() + ":\n" +
                analyze.getRotations();
    }
}
