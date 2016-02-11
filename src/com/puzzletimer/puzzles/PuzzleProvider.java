package com.puzzletimer.puzzles;

import puzzle.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PuzzleProvider {
    private Puzzle[] puzzles;
    private Map<String, Puzzle> puzzleMap;

    public PuzzleProvider() {
        this.puzzles = new Puzzle[] {
                new WcaPuzzle(new TwoByTwoCubePuzzle()),
                new WcaPuzzle(new ThreeByThreeCubePuzzle()),
                new WcaPuzzle(new FourByFourCubePuzzle()),
                new WcaPuzzle(new CubePuzzle(5)),
                new WcaPuzzle(new CubePuzzle(6)),
                new WcaPuzzle(new CubePuzzle(7)),
                new WcaPuzzle(new ClockPuzzle()),
                new WcaPuzzle(new SquareOnePuzzle()),
                new WcaPuzzle(new PyraminxPuzzle()),
                new WcaPuzzle(new MegaminxPuzzle()),
                new WcaPuzzle(new SkewbPuzzle()),
                new EmptyPuzzle()
        };

        this.puzzleMap = new HashMap<>();
        for (Puzzle p : this.puzzles) this.puzzleMap.put(p.getPuzzleId(), p);
    }

    public Puzzle get(String name) {
        return this.puzzleMap.get(name);
    }

    public Puzzle[] getAll() {
        return this.puzzles;
    }

    public Puzzle[] getAllNoEmpty() {
        return Arrays.copyOfRange(this.puzzles, 0, this.puzzles.length - 1);
    }
}
