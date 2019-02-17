package com.puzzletimer.tips;

import com.puzzletimer.models.Scramble;
import com.puzzletimer.puzzles.Puzzle;
import com.puzzletimer.solvers.Square1ShapeSolver;

import static com.puzzletimer.Internationalization.i18n;

public class Square1OptimalCubeShapeTip implements Tip {
    @Override
    public String getTipId() {
        return "SQUARE-1-OPTIMAL-CUBE-SHAPE";
    }

    @Override
    public String getPuzzleId() {
        return Puzzle.SQ1;
    }

    @Override
    public String getTipDescription() {
        return i18n("tip.SQUARE-1-OPTIMAL-CUBE-SHAPE");
    }

    @Override
    public String getTip(Scramble scramble) {
        String[] solution = Square1ShapeSolver.solve(
            Square1ShapeSolver.State.id.applySequence(scramble.getSequence()));

        return i18n("tip.SQUARE-1-OPTIMAL-CUBE-SHAPE") + ":\n  " + String.join(" ", solution);
    }

    @Override
    public String toString() {
        return getTipDescription();
    }
}
