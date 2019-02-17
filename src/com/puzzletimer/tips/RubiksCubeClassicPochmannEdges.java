package com.puzzletimer.tips;

import com.puzzletimer.models.Scramble;
import com.puzzletimer.puzzles.Puzzle;
import com.suushiemaniac.cubing.bld.analyze.BldPuzzle;
import com.suushiemaniac.cubing.bld.analyze.ThreeBldCube;
import com.suushiemaniac.cubing.bld.model.enumeration.piece.CubicPieceType;
import com.suushiemaniac.cubing.bld.model.enumeration.puzzle.CubicPuzzle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.puzzletimer.Internationalization.i18n;

public class RubiksCubeClassicPochmannEdges implements Tip {
    @Override
    public String getTipId() {
        return "RUBIKS-CUBE-CLASSIC-POCHMANN-EDGES";
    }

    @Override
    public String getPuzzleId() {
        return Puzzle.THREE;
    }

    @Override
    public String getTipDescription() {
        return i18n("tip.RUBIKS-CUBE-CLASSIC-POCHMANN-EDGES");
    }

    @Override
    public String getTip(Scramble scramble) {
        BldPuzzle analyze = new ThreeBldCube(scramble.parseFor(CubicPuzzle.THREE_BLD));
        analyze.setBuffer(CubicPieceType.EDGE, "B");

        String[] pureTargets = analyze.getSolutionPairs(CubicPieceType.EDGE).replaceAll("\\s+?", "").split("");
        // TODO
        String[] flippedEdges = analyze.getSolutionPairs(CubicPieceType.EDGE).replaceAll("\\s+?", "").split("");

        List<String> stickerSequence = new ArrayList<>();
        if (analyze.getStatLength(CubicPieceType.EDGE) > 0) stickerSequence.addAll(Arrays.asList(pureTargets));
        if (analyze.getMisOrientedCount(CubicPieceType.EDGE) > 0) stickerSequence.addAll(Arrays.asList(flippedEdges));

        // solution
        StringBuilder tip = new StringBuilder();

        tip.append(i18n("tip.RUBIKS-CUBE-CLASSIC-POCHMANN-EDGES")).append(":\n");
        tip.append("  [T1]  R U R' U' R' F R2 U' R' U' R U R' F'\n");
        tip.append("  [T2]  x' R2 U' R' U x R' F' U' F R U R' U'\n");
        tip.append("  [J1]  R U R' F' R U R' U' R' F R2 U' R' U'\n");
        tip.append("  [J2]  R' U2 R U R' U2 L U' R U L'\n");
        tip.append("\n");

        HashMap<String, String> letteringScheme = new HashMap<>();
        letteringScheme.put("A", "UB");
        letteringScheme.put("B", "UR");
        letteringScheme.put("C", "UF");
        letteringScheme.put("D", "UL");
        letteringScheme.put("E", "LU");
        letteringScheme.put("F", "LF");
        letteringScheme.put("G", "LD");
        letteringScheme.put("H", "LB");
        letteringScheme.put("I", "FU");
        letteringScheme.put("J", "FR");
        letteringScheme.put("K", "FD");
        letteringScheme.put("L", "FL");
        letteringScheme.put("M", "RU");
        letteringScheme.put("N", "RB");
        letteringScheme.put("O", "RD");
        letteringScheme.put("P", "RF");
        letteringScheme.put("Q", "BU");
        letteringScheme.put("R", "BL");
        letteringScheme.put("S", "BD");
        letteringScheme.put("T", "BR");
        letteringScheme.put("U", "DF");
        letteringScheme.put("V", "DR");
        letteringScheme.put("W", "DB");
        letteringScheme.put("X", "DL");

        HashMap<String, String> solutions = new HashMap<>();
        solutions.put("A", "[J2]");
        solutions.put("Q", "l [J1] l'");
        solutions.put("C", "[J1]");
        solutions.put("I", "l' [J2] l");
        solutions.put("D", "[T1]");
        solutions.put("E", "[T2]");
        solutions.put("R", "L [T1] L'");
        solutions.put("H", "L [T2] L'");
        solutions.put("T", "d2 L' [T1] L d2");
        solutions.put("N", "d L [T1] L' d'");
        solutions.put("J", "d2 L [T1] L' d2");
        solutions.put("P", "d' L' [T1] L d");
        solutions.put("L", "L' [T1] L");
        solutions.put("F", "L' [T2] L");
        solutions.put("W", "l2 [J1] l2");
        solutions.put("S", "l [J2] l'");
        solutions.put("V", "S' [T1] S");
        solutions.put("O", "D' l' [J1] l D");
        solutions.put("U", "l2 [J2] l2");
        solutions.put("K", "l' [J1] l");
        solutions.put("X", "L2 [T1] L2");
        solutions.put("G", "L2 [T2] L2");

        for (String sticker : stickerSequence) {
            tip.append("  (UR ").append(letteringScheme.get(sticker)).append(") ").append(sticker).append("  ").append(solutions.get(sticker)).append("\n");
        }

        return tip.toString().trim();
    }

    @Override
    public String toString() {
        return getTipDescription();
    }
}
