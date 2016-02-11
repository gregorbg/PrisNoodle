package com.puzzletimer.managers;

import com.puzzletimer.models.ColorScheme;
import com.puzzletimer.models.ColorScheme.FaceColor;
import com.puzzletimer.puzzles.PuzzleProvider;
import net.gnehzr.tnoodle.svglite.Color;
import puzzle.CubePuzzle;

import java.util.ArrayList;
import java.util.HashMap;

public class ColorManager {
    public static class Listener {
        public void colorSchemeUpdated(ColorScheme colorScheme) {
        }
    }

    private ArrayList<Listener> listeners;
    private HashMap<String, ColorScheme> colorSchemeMap;

    public ColorManager(ColorScheme[] colorSchemes) {
        this.listeners = new ArrayList<>();

        this.colorSchemeMap = new HashMap<>();
        for (ColorScheme colorScheme : colorSchemes) {
            this.colorSchemeMap.put(colorScheme.getPuzzleId(), colorScheme);
        }
    }

    public void loadTNoodleDefaults(PuzzleProvider puzzleProvider) {
        for (String key : this.colorSchemeMap.keySet()) {
            FaceColor[] prismaScheme = this.colorSchemeMap.get(key).getFaceColors();
            HashMap<String, Color> tNoodleMap = puzzleProvider.get(key).getTNoodle().getDefaultColorScheme();
            for (int i = 0; i < prismaScheme.length; i++) {
                String faceId = prismaScheme[i].getFaceId();
                java.awt.Color tNoodleDefault = FaceColor.fromTNoodle(tNoodleMap.get(faceId));
                prismaScheme[i] = prismaScheme[i].setDefaultColor(tNoodleDefault);
            }
        }
    }

    public ColorScheme getColorScheme(String puzzleId) {
        if (this.colorSchemeMap.containsKey(puzzleId)) {
            return this.colorSchemeMap.get(puzzleId);
        }

        return new ColorScheme(puzzleId, new FaceColor[0]);
    }

    public void setColorScheme(ColorScheme colorScheme) {
        this.colorSchemeMap.put(colorScheme.getPuzzleId(), colorScheme);
        for (Listener listener : this.listeners) {
            listener.colorSchemeUpdated(colorScheme);
        }
    }

    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }
}
