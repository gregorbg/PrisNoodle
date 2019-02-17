package com.puzzletimer.gui;

import com.puzzletimer.categories.Category;
import com.puzzletimer.managers.CategoryManager;
import com.puzzletimer.managers.ScrambleManager;
import com.puzzletimer.models.Scramble;
import com.puzzletimer.puzzles.Puzzle;
import com.puzzletimer.puzzles.PuzzleProvider;
import com.puzzletimer.scramblers.Scrambler;
import com.puzzletimer.scramblers.ScramblerProvider;
import com.puzzletimer.tips.TipProvider;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

import static com.puzzletimer.Internationalization.i18n;

@SuppressWarnings("serial")
public class TipsFrame extends JFrame {
    private JTextArea textAreaTips;
    private JButton buttonOk;

    public TipsFrame(
            final PuzzleProvider puzzleProvider,
            final TipProvider tipProvider,
            final ScramblerProvider scrambleProvider,
            final CategoryManager categoryManager,
            final ScrambleManager scrambleManager) {
        super();

        setMinimumSize(new Dimension(480, 320));

        createComponents();
        pack();

        // title
        categoryManager.addListener(new CategoryManager.Listener() {
            @Override
            public void categoriesUpdated(Category[] categories, Category currentCategory) {
                Scrambler scrambler = scrambleProvider.get(currentCategory.getScramblerId());
                Puzzle puzzle = puzzleProvider.get(scrambler.getPuzzle().getPuzzleId());
                setTitle(
                    String.format(
                        i18n("tips.tips_category"),
                        puzzle.getDescription()));
            }
        });
        categoryManager.notifyListeners();

        // tips
        scrambleManager.addListener(new ScrambleManager.Listener() {
            @Override
            public void scrambleChanged(Scramble scramble) {
                Category category = categoryManager.getCurrentCategory();

                StringBuilder contents = new StringBuilder();
                for (String tipId : category.getTipIds()) {
                    contents.append(tipProvider.get(tipId).getTip(scramble));
                    contents.append("\n\n");
                }

                TipsFrame.this.textAreaTips.setText(contents.toString().trim());
                TipsFrame.this.textAreaTips.setCaretPosition(0);
            }
        });

        // ok button
        this.setDefaultCloseOperation(HIDE_ON_CLOSE);
        this.buttonOk.addActionListener(event -> TipsFrame.this.setVisible(false));

        // esc key closes window
        this.getRootPane().registerKeyboardAction(
                arg0 -> TipsFrame.this.setVisible(false),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void createComponents() {
        setLayout(
            new MigLayout(
                "fill",
                "",
                "[pref!][]16[pref!]"));

        // labelTips
        add(new JLabel(i18n("tips.tips")), "wrap");

        // textAreaContents
        this.textAreaTips = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(this.textAreaTips);
        scrollPane.setPreferredSize(new Dimension(0, 0));
        add(scrollPane, "grow, wrap");

        // buttonOk
        this.buttonOk = new JButton(i18n("tips.ok"));
        add(this.buttonOk, "tag ok");
    }
}
