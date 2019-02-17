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

public class RubiksCubeM2Edges implements Tip {
    @Override
    public String getTipId() {
        return "RUBIKS-CUBE-M2-EDGES";
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
        BldPuzzle analyze = new ThreeBldCube(scramble.parseFor(CubicPuzzle.THREE_BLD));

        String[] pureTargets = analyze.getSolutionPairs(CubicPieceType.EDGE).replaceAll("\\s+?", "").split("");
        // TODO
        String[] flippedEdges = analyze.getSolutionPairs(CubicPieceType.EDGE).replaceAll("\\s+?", "").split("");

        List<String> stickerSequence = new ArrayList<>();
        if (analyze.getStatLength(CubicPieceType.EDGE) > 0) stickerSequence.addAll(Arrays.asList(pureTargets));
        if (analyze.getMisOrientedCount(CubicPieceType.EDGE) > 0) stickerSequence.addAll(Arrays.asList(flippedEdges));

        // solution
        StringBuilder tip = new StringBuilder();

        tip.append(i18n("tip.RUBIKS-CUBE-M2-EDGES")).append(":\n");

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
        solutions.put("A", "M2");
        solutions.put("B", "R' U R U' M2 U R' U' R");
        solutions.put("C", "U2 M' U2 M'");
        solutions.put("D", "L U' L' U M2 U' L U L'");
        solutions.put("E", "x' U L' U' M2 U L U' x");
        solutions.put("F", "x' U L2' U' M2 U L2 U' x");
        solutions.put("G", "x' U L U' M2 U L' U' x");
        solutions.put("H", "r' U L U' M2 U L' U' r");
        solutions.put("I", "F E R U R' E' R U' R' F' M2");
        solutions.put("J", "U R U' M2 U R' U'");
        solutions.put("L", "U' L' U M2 U' L U");
        solutions.put("M", "x' U' R U M2 U' R' U x");
        solutions.put("N", "l U' R' U M2 U' R U l'");
        solutions.put("O", "x' U' R' U M2 U' R U x");
        solutions.put("P", "x' U' R2 U M2 U' R2 U x");
        solutions.put("Q", "F' D R' F D' M2 D F' R D' F");
        solutions.put("R", "U' L U M2 U' L' U");
        solutions.put("S", "M2 D R' U R' U' M' U R U' M R D'");
        solutions.put("T", "U R' U' M2 U R U'");
        solutions.put("V", "U R2 U' M2 U R2 U'");
        solutions.put("W", "M U2 M U2");
        solutions.put("X", "U' L2 U M2 U' L2 U");

        HashMap<String, String> mLayerInverse = new HashMap<>();
        mLayerInverse.put("I", "S");
        mLayerInverse.put("C", "W");
        mLayerInverse.put("S", "I");
        mLayerInverse.put("W", "C");

        for (int i = 0; i < stickerSequence.size(); i++) {
            String sticker = stickerSequence.get(i);
            tip.append("  (DF ").append(letteringScheme.get(sticker)).append(") ").append(sticker);

            if (i % 2 == 1 && mLayerInverse.containsKey(sticker)) {
                sticker = mLayerInverse.get(sticker);
            }

            tip.append("  ").append(solutions.get(sticker)).append("\n");
        }

        return tip.toString().trim();
    }

    @Override
    public String toString() {
        return getTipDescription();
    }
}
