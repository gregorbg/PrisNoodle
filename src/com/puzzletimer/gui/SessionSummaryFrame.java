package com.puzzletimer.gui;

import com.puzzletimer.categories.Category;
import com.puzzletimer.managers.CategoryManager;
import com.puzzletimer.managers.SessionManager;
import com.puzzletimer.models.Solution;
import com.puzzletimer.statistics.*;
import com.puzzletimer.util.SolutionUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.text.DateFormat;
import java.util.Date;

import static com.puzzletimer.Internationalization.i18n;

@SuppressWarnings("serial")
public class SessionSummaryFrame extends JFrame {
    private JTextArea textAreaSummary;
    private JButton buttonCopyToClipboard;
    private JButton buttonOk;

    public SessionSummaryFrame(final CategoryManager categoryManager, SessionManager sessionManager) {
        super();

        setMinimumSize(new Dimension(640, 480));

        createComponents();
        pack();

        // title
        categoryManager.addListener(new CategoryManager.Listener() {
            @Override
            public void categoriesUpdated(Category[] categories, Category currentCategory) {
                setTitle(
                        String.format(
                                i18n("session_summary.session_sumary_category"),
                                currentCategory.getDescription()));
            }
        });
        categoryManager.notifyListeners();

        // summary
        sessionManager.addListener(new SessionManager.Listener() {
            @Override
            public void solutionsUpdated(Solution[] solutions) {
                updateSummary(categoryManager.getCurrentCategory(), solutions);
            }
        });

        // copy to clipboard
        this.buttonCopyToClipboard.addActionListener(event -> {
            StringSelection contents =
                    new StringSelection(SessionSummaryFrame.this.textAreaSummary.getText());
            Clipboard clipboard =
                    Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(contents, contents);
        });

        // ok button
        this.setDefaultCloseOperation(HIDE_ON_CLOSE);
        this.buttonOk.addActionListener(event -> SessionSummaryFrame.this.setVisible(false));

        // esc key closes window
        this.getRootPane().registerKeyboardAction(
                arg0 -> SessionSummaryFrame.this.setVisible(false),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void createComponents() {
        setLayout(
                new MigLayout(
                        "fill",
                        "",
                        "[pref!][][pref!]16[pref!]"));

        // labelSessionSummary
        add(new JLabel(i18n("session_summary.summary")), "wrap");

        // textAreaContents
        this.textAreaSummary = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(this.textAreaSummary);
        scrollPane.setPreferredSize(new Dimension(0, 0));
        add(scrollPane, "grow, wrap");

        // button copy to clipboard
        this.buttonCopyToClipboard = new JButton(i18n("session_summary.copy_to_clipboard"));
        add(this.buttonCopyToClipboard, "width 150, right, wrap");

        // buttonOk
        this.buttonOk = new JButton(i18n("session_summary.ok"));
        add(this.buttonOk, "tag ok");
    }

    private void updateSummary(Category currentCategory, Solution[] solutions) {
        StringBuilder summary = new StringBuilder();

        if (solutions.length >= 1) {
            // categoryName
            summary.append(currentCategory.getDescription());
            summary.append("\n");

            // session interval
            Date start = solutions[solutions.length - 1].getTiming().getStart();
            Date end = solutions[0].getTiming().getEnd();

            DateFormat dateTimeFormat =
                    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
            DateFormat timeFormat =
                    DateFormat.getTimeInstance(DateFormat.MEDIUM);

            summary.append(dateTimeFormat.format(start)).append(" - ").append(timeFormat.format(end));
            summary.append("\n");

            summary.append("\n");

            // statistics
            String[] labels = {
                    i18n("session_summary.accuracy"),
                    i18n("session_summary.mean"),
                    i18n("session_summary.mean_50%"),
                    i18n("session_summary.average"),
                    i18n("session_summary.averageNoDNF"),
                    i18n("session_summary.best_time"),
                    i18n("session_summary.median"),
                    i18n("session_summary.worst_time"),
                    i18n("session_summary.standard_deviation"),
            };

            StatisticalMeasure[] statistics = {
                    new Accuracy(1, Integer.MAX_VALUE),
                    new Mean(1, Integer.MAX_VALUE),
                    new MeanPercent(1, Integer.MAX_VALUE),
                    new Average(3, Integer.MAX_VALUE),
                    new FullAverage(3, Integer.MAX_VALUE),
                    new Best(1, Integer.MAX_VALUE),
                    new Percentile(1, Integer.MAX_VALUE, 0.5),
                    new Worst(1, Integer.MAX_VALUE),
                    new StandardDeviation(1, Integer.MAX_VALUE),
            };

            int maxLabelLength = 0;
            for (String label : labels) if (label.length() > maxLabelLength) maxLabelLength = label.length();

            int maxStringLength = 0;
            for (StatisticalMeasure statistic : statistics) {
                if (solutions.length < statistic.getMinimumWindowSize()) continue;

                statistic.setSolutions(solutions);

                String s = statistic.toFormatString();
                if (s.length() > maxStringLength) maxStringLength = s.length();
            }

            for (int i = 0; i < labels.length; i++) {
                if (solutions.length < statistics[i].getMinimumWindowSize()) continue;

                summary.append(String.format(
                        "%-" + maxLabelLength + "s %" + maxStringLength + "s",
                        labels[i],
                        statistics[i].toFormatString()));
                summary.append("\n");
            }

            summary.append("\n");
        }

        // best average of X
        String[] labels = {
                i18n("session_summary.best_average_of_5"),
                i18n("session_summary.best_average_of_12"),
        };

        StatisticalMeasure[] statistics = {
                new BestAverage(5, Integer.MAX_VALUE),
                new BestAverage(12, Integer.MAX_VALUE),
        };

        for (int i = 0; i < statistics.length; i++) {
            int windowSize = statistics[i].getMinimumWindowSize();

            if (solutions.length >= windowSize) {
                statistics[i].setSolutions(solutions);
                int windowPosition = statistics[i].getWindowPosition();

                // value
                summary.append(labels[i]).append(" ").append(statistics[i].toFormatString());
                summary.append("\n");

                // index range
                summary.append(String.format(
                        "  %d-%d - ",
                        solutions.length - windowPosition - windowSize + 1,
                        solutions.length - windowPosition));

                // find indices of best and worst times
                int indexBest = 0;
                int indexWorst = 0;
                long[] times = new long[windowSize];
                for (int j = 0; j < windowSize; j++) {
                    times[j] = SolutionUtils.realTime(solutions[windowPosition + j]);

                    if (times[j] < times[indexBest]) indexBest = j;

                    if (times[j] > times[indexWorst]) indexWorst = j;
                }

                // times
                String sTimes = "";
                for (int j = windowSize - 1; j >= 0; j--)
                    sTimes += j == indexBest || j == indexWorst ? "(" + SolutionUtils.format(times[j], false) + ") " : SolutionUtils.format(times[j], false) + " ";

                summary.append(sTimes.trim());
                summary.append("\n");

                summary.append("\n");
            }
        }

        // solutions
        String[] sSolutions = new String[solutions.length];
        long[] realTimes = SolutionUtils.realTimes(solutions, false);

        int maxStringLength = 0;
        for (int i = 0; i < realTimes.length; i++) {
            sSolutions[i] = SolutionUtils.format(realTimes[i], false);
            if (sSolutions[i].length() > maxStringLength) maxStringLength = sSolutions[i].length();
        }

        for (int i = solutions.length - 1; i >= 0; i--) {
            // index
            String indexFormat = "%" + ((int) Math.log10(solutions.length) + 1) + "d. ";
            summary.append(String.format(indexFormat, solutions.length - i));

            // time
            String timeFormat = "%" + maxStringLength + "s  ";
            summary.append(String.format(timeFormat, sSolutions[i]));

            // scramble
            summary.append(solutions[i].getScramble().getRawSequence());
            summary.append("\n");
        }

        this.textAreaSummary.setText(summary.toString());
        this.textAreaSummary.setCaretPosition(0);
    }
}
