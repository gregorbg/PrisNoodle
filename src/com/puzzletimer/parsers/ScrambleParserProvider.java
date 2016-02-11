package com.puzzletimer.parsers;

import com.puzzletimer.puzzles.Puzzle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScrambleParserProvider {
    private HashMap<String, ScrambleParser> scrambleParserMap;

    public ScrambleParserProvider() {
        ScrambleParser[] scrambleParsers = new ScrambleParser[]{
                new RegexScrambleParser("", Puzzle.EMPTY) {
                    @Override
                    public String[] parse(String input) {
                        return new String[0];
                    }
                },
                new RegexScrambleParser("[URF][2\\']?", Puzzle.TWO),
                new RegexScrambleParser("[UDLRFB]w?[2\\']?", Puzzle.THREE),
                new RegexScrambleParser("([UDLRFBxyz][2\\']?)|([2-3]?[UDLRFB]w[2\\']?)", Puzzle.FOUR),
                new RegexScrambleParser("([UDLRFBxyz][2\\']?)|([2-4]?[UDLRFB]w[2\\']?)", Puzzle.FIVE),
                new RegexScrambleParser("([UDLRFBxyz][2\\']?)|([2-5]?[UDLRFB]w[2\\']?)", Puzzle.SIX),
                new RegexScrambleParser("([UDLRFBxyz][2\\']?)|([2-6]?[UDLRFB]w[2\\']?)", Puzzle.SEVEN),
                new RegexScrambleParser("(U\\'?)|(R((\\+\\+)|(\\-\\-)))", Puzzle.MEGA),
                new RegexScrambleParser("[UuBbLlRr][2\\']?", Puzzle.PYRA),
                new RegexScrambleParser("((([UD][LR]?)|[LR]|ALL)[0-6][+-])|y2", Puzzle.CLOCK),
                new RegexScrambleParser("[UBLR][2\\']?", Puzzle.SKEWB),
                new RegexScrambleParser("/|(\\(\\-?[0-6],\\-?[0-6]\\))", Puzzle.SQ1) {
                    @Override
                    public String[] parse(String input) {
                        ArrayList<String> regexMoves = new ArrayList<>();
                        ArrayList<String> validMoves = new ArrayList<>();

                        for (String move : input.split("\\s+?")) {
                            if (move.matches(this.matchingRegex)) regexMoves.add(move);
                            else break;
                        }

                        for (String move : regexMoves) {
                            validMoves.add(move);
                            if (!isValidScramble(validMoves)) {
                                validMoves.remove(validMoves.size() - 1);
                                break;
                            }
                        }

                        validMoves = fixImplicitTwists(validMoves);
                        return validMoves.toArray(new String[validMoves.size()]);
                    }

                    private ArrayList<String> fixImplicitTwists(ArrayList<String> sequence) {
                        if (sequence.contains("/")) return sequence;
                        ArrayList<String> newSequence = new ArrayList<>();

                        for (String move : sequence) {
                            newSequence.add(move);
                            newSequence.add("/");
                        }

                        return newSequence;
                    }

                    private boolean isValidScramble(ArrayList<String> sequence) {
                        int[] state = {0, 0, 1, 2, 2, 3, 4, 4, 5, 6, 6, 7, 8, 9, 9, 10, 11, 11, 12, 13, 13, 14, 15, 15};
                        for (String move : sequence) {
                            if (move.equals("/")) {
                                boolean isTwistable = state[0] != state[11] && state[6] != state[5] && state[12] != state[23] && state[18] != state[17];
                                if (!isTwistable) return false;
                                else {
                                    int[] newState = Arrays.copyOf(state, state.length);
                                    for (int i = 0; i < 6; i++) {
                                        int piece = newState[i + 12];
                                        newState[i + 12] = newState[i + 6];
                                        newState[i + 6] = piece;
                                    }
                                    state = Arrays.copyOf(newState, newState.length);
                                }
                            } else {
                                int[] newState = Arrays.copyOf(state, state.length);
                                Matcher topBottom = Pattern.compile("\\((\\-?[0-6]),(\\-?[0-6])\\)").matcher(move);
                                if (topBottom.find()) {
                                    int[] t = new int[12];
                                    for (int i = 0; i < 2; i++) {
                                        int side = Integer.parseInt(topBottom.group(i + 1));
                                        side = -side % 12;
                                        if (side < 0) side += 12;
                                        System.arraycopy(newState, i % 2 == 0 ? 0 : 12, t, 0, 12);
                                        for (int j = 0; j < 12; j++)
                                            newState[j + (i % 2 == 0 ? 0 : 12)] = t[(side + j) % 12];
                                    }
                                    state = Arrays.copyOf(newState, newState.length);
                                } else return false;
                            }
                        }
                        return true;
                    }
                }
        };

        this.scrambleParserMap = new HashMap<>();
        for (ScrambleParser scrambleParser : scrambleParsers)
            this.scrambleParserMap.put(scrambleParser.getPuzzleId(), scrambleParser);
    }

    public ScrambleParser get(String puzzleId) {
        return this.scrambleParserMap.get(puzzleId);
    }
}
