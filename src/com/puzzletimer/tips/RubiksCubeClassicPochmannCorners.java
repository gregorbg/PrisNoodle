package com.puzzletimer.tips;

import com.puzzletimer.models.Scramble;
import com.puzzletimer.puzzles.Puzzle;
import com.puzzletimer.solvers.RubiksCubeSolver.State;
import com.suushiemaniac.bld.analyze.ThreeBldCube;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.puzzletimer.Internationalization.i18n;

public class RubiksCubeClassicPochmannCorners implements Tip {
    @Override
    public String getTipId() {
        return "RUBIKS-CUBE-CLASSIC-POCHMANN-CORNERS";
    }

    @Override
    public String getPuzzleId() {
        return Puzzle.THREE;
    }

    @Override
    public String getTipDescription() {
        return i18n("tip.RUBIKS-CUBE-CLASSIC-POCHMANN-CORNERS");
    }

    @Override
    public String getTip(Scramble scramble) {
        ThreeBldCube analyze = new ThreeBldCube(scramble.getRawSequence());

        String[] pureTargets = analyze.getCornerPairs(false).replaceAll("\\s+?", "").split("");
        String[] cwCorners = analyze.getCwCornerSingleTargetPairs().replaceAll("\\s+?", "").split("");
        String[] ccwCorners = analyze.getCcwCornerSingleTargetPairs().replaceAll("\\s+?", "").split("");

        List<String> stickerSequence = new ArrayList<>();
        if (analyze.getCornerLength() > 0) stickerSequence.addAll(Arrays.asList(pureTargets));
        if (analyze.getNumPreCWCorners() > 0) stickerSequence.addAll(Arrays.asList(cwCorners));
        if (analyze.getNumPreCCWCorners() > 0) stickerSequence.addAll(Arrays.asList(ccwCorners));

        // solution
        StringBuilder tip = new StringBuilder();

        tip.append(i18n("tip.RUBIKS-CUBE-CLASSIC-POCHMANN-CORNERS")).append(":\n");
        tip.append("  [Y]  R U' R' U' R U R' F' R U R' U' R' F R\n");
        tip.append("\n");

        HashMap<String, String> letteringScheme = new HashMap<>();
        letteringScheme.put("A", "ULB");
        letteringScheme.put("B", "UBR");
        letteringScheme.put("C", "URF");
        letteringScheme.put("D", "UFL");
        letteringScheme.put("E", "LBU");
        letteringScheme.put("F", "LUF");
        letteringScheme.put("G", "LFD");
        letteringScheme.put("H", "LDB");
        letteringScheme.put("I", "FLU");
        letteringScheme.put("J", "FUR");
        letteringScheme.put("K", "FRD");
        letteringScheme.put("L", "FDL");
        letteringScheme.put("M", "RFU");
        letteringScheme.put("N", "RUB");
        letteringScheme.put("O", "RBD");
        letteringScheme.put("P", "RDF");
        letteringScheme.put("Q", "BRU");
        letteringScheme.put("R", "BUL");
        letteringScheme.put("S", "BLD");
        letteringScheme.put("T", "BDR");
        letteringScheme.put("U", "DLF");
        letteringScheme.put("V", "DFR");
        letteringScheme.put("W", "DRB");
        letteringScheme.put("X", "DBL");

        HashMap<String, String> solutions = new HashMap<>();
        solutions.put("N", "R2 [Y] R2");
        solutions.put("B", "R D' [Y] D R'");
        solutions.put("Q", "R' F [Y] F' R");
        solutions.put("J", "R2 D' [Y] D R2");
        solutions.put("C", "F [Y] F'");
        solutions.put("M", "R' [Y] R");
        solutions.put("F", "F2 [Y] F2");
        solutions.put("D", "F R' [Y] R F'");
        solutions.put("I", "F' D [Y] D' F");
        solutions.put("H", "D2 [Y] D2");
        solutions.put("X", "D F' [Y] F D'");
        solutions.put("S", "D' R [Y] R' D");
        solutions.put("T", "D' [Y] D");
        solutions.put("W", "R2 F [Y] F' R2");
        solutions.put("O", "R [Y] R'");
        solutions.put("P", "[Y]");
        solutions.put("V", "F' R' [Y] R F");
        solutions.put("K", "R F [Y] F' R'");
        solutions.put("L", "D [Y] D'");
        solutions.put("U", "F' [Y] F");
        solutions.put("G", "F2 R' [Y] R F2");

        for (String sticker : stickerSequence) {
            tip.append("  (LBU ").append(letteringScheme.get(sticker)).append(") ").append(sticker).append("  ").append(solutions.get(sticker)).append("\n");
        }

        return tip.toString().trim();
    }

    @Override
    public String toString() {
        return getTipDescription();
    }
}