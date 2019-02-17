package com.puzzletimer.tips;

import com.puzzletimer.models.Scramble;
import com.puzzletimer.puzzles.Puzzle;
import com.suushiemaniac.cubing.bld.analyze.BldPuzzle;
import com.suushiemaniac.cubing.bld.analyze.ThreeBldCube;
import com.suushiemaniac.cubing.bld.model.enumeration.puzzle.CubicPuzzle;

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
        BldPuzzle analyze = new ThreeBldCube(CubicPuzzle.THREE_BLD.getReader().parse(scramble.getRawSequence()));

        return this.getTipDescription() + ":\n" +
                analyze.getRotations();
    }
}
