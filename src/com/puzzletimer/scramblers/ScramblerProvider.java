package com.puzzletimer.scramblers;

import com.puzzletimer.puzzles.WcaPuzzle;
import puzzle.*;

import java.util.HashMap;

public class ScramblerProvider {
    private Scrambler[] scramblers;
    private HashMap<String, Scrambler> scramblerMap;

    public ScramblerProvider() {
        this.scramblers = new Scrambler[]{
                new WcaScrambler(new WcaPuzzle(new TwoByTwoCubePuzzle())),
                new WcaScrambler(new WcaPuzzle(new ThreeByThreeCubePuzzle())),
                new WcaScrambler(new WcaPuzzle(new NoInspectionThreeByThreeCubePuzzle())),
                new WcaScrambler(new WcaPuzzle(new FourByFourCubePuzzle())),
                new WcaScrambler(new WcaPuzzle(new NoInspectionFourByFourCubePuzzle())),
                new WcaScrambler(new WcaPuzzle(new CubePuzzle(5))),
                new WcaScrambler(new WcaPuzzle(new NoInspectionFiveByFiveCubePuzzle())),
                new WcaScrambler(new WcaPuzzle(new CubePuzzle(6))),
                new WcaScrambler(new WcaPuzzle(new CubePuzzle(7))),
                new WcaScrambler(new WcaPuzzle(new ClockPuzzle())),
                new WcaScrambler(new WcaPuzzle(new MegaminxPuzzle())),
                new WcaScrambler(new WcaPuzzle(new PyraminxPuzzle())),
                new WcaScrambler(new WcaPuzzle(new SquareOnePuzzle())),
                new WcaScrambler(new WcaPuzzle(new SkewbPuzzle())),
                new EmptyScrambler()
        };

        this.scramblerMap = new HashMap<>();
        for (Scrambler scrambler : this.scramblers)
            this.scramblerMap.put(scrambler.getScramblerId(), scrambler);
    }

    public Scrambler[] getAll() {
        return this.scramblers;
    }

    public Scrambler get(String scramblerId) {
        return this.scramblerMap.get(scramblerId);
    }
}
