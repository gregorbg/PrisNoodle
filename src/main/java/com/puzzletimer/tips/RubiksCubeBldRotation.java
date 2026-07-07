package com.puzzletimer.tips;

import com.puzzletimer.models.Scramble;
import com.puzzletimer.puzzles.Puzzle;
import com.suushiemaniac.cubing.bld.analyze.BldAnalysis;
import com.suushiemaniac.cubing.bld.gsolve.GPuzzle;
import com.suushiemaniac.cubing.bld.model.puzzle.WCAPuzzle;

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
        GPuzzle bldPuzzle = WCAPuzzle.THREE_BLD.gPuzzle("2012BILL01_UFR");
        BldAnalysis analysis = bldPuzzle.getAnalysis(scramble.parseFor(WCAPuzzle.THREE_BLD));

        return this.getTipDescription() + ":\n" +
                analysis.getRotations();
    }
}
