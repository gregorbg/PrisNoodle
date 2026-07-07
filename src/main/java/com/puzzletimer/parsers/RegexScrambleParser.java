package com.puzzletimer.parsers;

import java.util.ArrayList;

public class RegexScrambleParser implements ScrambleParser {
    protected String matchingRegex, puzzleId;

    public RegexScrambleParser(String matchingRegex, String puzzleId) {
        this.matchingRegex = matchingRegex;
        this.puzzleId = puzzleId;
    }

    @Override
    public String getPuzzleId() {
        return this.puzzleId;
    }

    @Override
    public String[] parse(String input) {
        ArrayList<String> moves = new ArrayList<>();

        for (String move : input.split("\\s+?")) {
            if (move.matches(this.matchingRegex)) moves.add(move);
            else break;
        }

        return moves.toArray(new String[moves.size()]);
    }
}
