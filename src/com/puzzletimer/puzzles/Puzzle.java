package com.puzzletimer.puzzles;

import com.puzzletimer.models.ColorScheme;
import net.gnehzr.tnoodle.scrambles.InvalidScrambleException;

public interface Puzzle {
    String TWO = "2x2x2";
    String THREE = "3x3x3";
    String FOUR = "4x4x4";
    String FIVE = "5x5x5";
    String SIX = "6x6x6";
    String SEVEN = "7x7x7";
    String MEGA = "Megaminx";
    String PYRA = "Pyraminx";
    String SQ1 = "Square-1";
    String CLOCK = "Clock";
    String SKEWB = "Skewb";
    String EMPTY = "EMPTY";

    String getPuzzleId();

    String getDescription();

    net.gnehzr.tnoodle.scrambles.Puzzle getTNoodle();

    String getScrambleSVG(String sequence, ColorScheme scheme) throws InvalidScrambleException;
}
