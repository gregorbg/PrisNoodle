package com.puzzletimer.gui;

import com.puzzletimer.models.ColorScheme;
import net.gnehzr.tnoodle.scrambles.InvalidScrambleException;
import net.gnehzr.tnoodle.scrambles.Puzzle;
import net.miginfocom.swing.MigLayout;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.svg.SVGDocument;
import puzzle.ThreeByThreeCubePuzzle;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.StringReader;

import static com.puzzletimer.Internationalization.i18n;
import static com.puzzletimer.models.ColorScheme.*;

@SuppressWarnings("serial")
public class AboutDialog extends JDialog {
    private final SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(XMLResourceDescriptor.getXMLParserClassName());

    public AboutDialog(JFrame owner, boolean modal) {
        super(owner, modal);

        setTitle(i18n("about.prisma_puzzle_timer_version"));
        setResizable(false);

        createComponents();
        pack();
    }

    private void createComponents() {
        setLayout(new MigLayout("", "", ""));

        // panel3D
        Puzzle puzzle = new ThreeByThreeCubePuzzle();
        ColorScheme colorScheme =
            new ColorScheme(
                com.puzzletimer.puzzles.Puzzle.THREE,
                new FaceColor[] {
                    new FaceColor(com.puzzletimer.puzzles.Puzzle.THREE, "B", null, Color.WHITE),
                    new FaceColor(com.puzzletimer.puzzles.Puzzle.THREE, "D", null, Color.WHITE),
                    new FaceColor(com.puzzletimer.puzzles.Puzzle.THREE, "L", null, Color.WHITE),
                    new FaceColor(com.puzzletimer.puzzles.Puzzle.THREE, "R", null, Color.WHITE),
                    new FaceColor(com.puzzletimer.puzzles.Puzzle.THREE, "F", null, Color.WHITE),
                    new FaceColor(com.puzzletimer.puzzles.Puzzle.THREE, "U", null, Color.WHITE),
                });

        JSVGCanvas panelSVG = new JSVGCanvas();
        panelSVG.setBackground(getBackground());

        try {
            StringReader reader = new StringReader(puzzle.drawScramble("", colorScheme.toTNoodle()).toString());
            SVGDocument doc = f.createSVGDocument("", reader);
            panelSVG.setSVGDocument(doc);
        } catch (IOException | InvalidScrambleException e) {
            e.printStackTrace();
        }

        add(panelSVG, "width 125, height 125, spany");

        // labelPrismaPuzzleTimer
        JLabel labelPrismaPuzzleTimer = new JLabel(i18n("about.prisma_puzzle_timer_version"));
        labelPrismaPuzzleTimer.setFont(new Font("Arial", Font.BOLD, 16));
        add(labelPrismaPuzzleTimer, "split 3, gapbottom 10, flowy");

        // labelURL
        JLabel labelURL = new JLabel(i18n("about.prisma_puzzle_timer_address"));
        add(labelURL);

        // labelWalter
        JLabel labelWalter = new JLabel(i18n("about.walters_email"));
        add(labelWalter);
    }
}
