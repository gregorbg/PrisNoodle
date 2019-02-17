package com.puzzletimer.models;

import net.gnehzr.tnoodle.svglite.*;

import java.awt.*;
import java.awt.Color;
import java.util.HashMap;

import static com.puzzletimer.Internationalization.i18n;

public class ColorScheme {
    public static class FaceColor {
        private final String puzzleId;
        private final String faceId;
        private final Color defaultColor;
        private final Color color;

        public static Color fromTNoodle(net.gnehzr.tnoodle.svglite.Color tNoodleColor) {
            return new Color(
                    tNoodleColor.getRed(),
                    tNoodleColor.getGreen(),
                    tNoodleColor.getBlue()
            );
        }

        public FaceColor(String puzzleId, String faceId, Color defaultColor, Color color) {
            this.puzzleId = puzzleId;
            this.faceId = faceId;
            this.defaultColor = defaultColor;
            this.color = color;
        }

        public String getPuzzleId() {
            return this.puzzleId;
        }

        public String getFaceId() {
            return this.faceId;
        }

        public String getFaceDescription() {
            return i18n("face." + this.puzzleId + "." + this.faceId);
        }

        public Color getDefaultColor() {
            return this.defaultColor;
        }

        public FaceColor setColorToDefault() {
            return new FaceColor(
                this.puzzleId,
                this.faceId,
                this.defaultColor,
                this.defaultColor);
        }

        public Color getColor() {
            return this.color;
        }

        public FaceColor setColor(Color color) {
            return new FaceColor(
                this.puzzleId,
                this.faceId,
                this.defaultColor,
                color);
        }

        public FaceColor setDefaultColor(Color defaultColor) {
            return new FaceColor(
                    this.puzzleId,
                    this.faceId,
                    defaultColor,
                    this.color
            );
        }

        public net.gnehzr.tnoodle.svglite.Color toTNoodle() {
            return new net.gnehzr.tnoodle.svglite.Color(
                    this.color.getRed(),
                    this.color.getGreen(),
                    this.color.getBlue(),
                    this.color.getAlpha()
            );
        }
    }

    private final String puzzleId;
    private final FaceColor[] faceColors;

    public ColorScheme(String puzzleId, FaceColor[] faceColors) {
        this.puzzleId = puzzleId;
        this.faceColors = faceColors;
    }

    public String getPuzzleId() {
        return this.puzzleId;
    }

    public FaceColor[] getFaceColors() {
        return this.faceColors;
    }

    public FaceColor getFaceColor(String faceId) {
        for (FaceColor faceColor : this.faceColors) {
            if (faceColor.getFaceId().equals(faceId)) {
                return faceColor;
            }
        }

        return null;
    }

    public ColorScheme setFaceColor(FaceColor faceColor) {
        FaceColor[] faceColors = new FaceColor[this.faceColors.length];
        for (int i = 0; i < faceColors.length; i++) {
            faceColors[i] = this.faceColors[i];
            if (this.faceColors[i].getFaceId().equals(faceColor.getFaceId())) {
                faceColors[i] = faceColor;
            }
        }

        return new ColorScheme(this.puzzleId, faceColors);
    }

    public HashMap<String, net.gnehzr.tnoodle.svglite.Color> toTNoodle() {
        HashMap<String, net.gnehzr.tnoodle.svglite.Color> tNoodleScheme = new HashMap<>();
        for (FaceColor f : this.faceColors) tNoodleScheme.put(f.getFaceId(), f.toTNoodle());
        return tNoodleScheme;
    }
}