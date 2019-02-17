package com.puzzletimer.gui;

import com.puzzletimer.categories.Category;
import com.puzzletimer.managers.CategoryManager;
import com.puzzletimer.managers.SessionManager;
import com.puzzletimer.managers.SolutionManager;
import com.puzzletimer.models.Solution;
import com.puzzletimer.statistics.*;
import com.puzzletimer.util.SolutionUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DateFormat;

import static com.puzzletimer.Internationalization.i18n;

@SuppressWarnings("serial")
public class HistoryFrame extends JFrame {
    private GraphPanel graphPanel;

    private JLabel labelNumberOfSolutions;
    private JLabel labelMean;
    private JLabel labelBest;
    private JLabel labelMeanOf3;
    private JLabel labelBestMeanOf3;
    private JLabel labelAverage;
    private JLabel labelLowerQuartile;
    private JLabel labelMeanOf10;
    private JLabel labelBestMeanOf10;
    private JLabel labelInterquartileMean;
    private JLabel labelMedian;
    private JLabel labelMeanOf100;
    private JLabel labelBestMeanOf100;
    private JLabel labelUpperQuartile;
    private JLabel labelAverageOf5;
    private JLabel labelBestAverageOf5;
    private JLabel labelStandardDeviation;
    private JLabel labelWorst;
    private JLabel labelAverageOf12;
    private JLabel labelBestAverageOf12;
    private JLabel labelAccuracy;
    private JLabel labelFullWorst;
    private JLabel labelFullAverage;
    private JLabel labelMeanPercent;
    private JTable table;
    private JButton buttonEdit;
    private JButton buttonRemove;
    private JButton buttonSelectSession;
    private JButton buttonSelectNone;
    private JButton buttonOk;
    private String nullTime;

    public HistoryFrame(
            final CategoryManager categoryManager,
            final SolutionManager solutionManager,
            final SessionManager sessionManager) {
        super();

        this.nullTime = "XX:XX.XX";

        setMinimumSize(new Dimension(800, 600));

        createComponents();
        pack();

        // title
        categoryManager.addListener(new CategoryManager.Listener() {
            @Override
            public void categoriesUpdated(Category[] categories, Category currentCategory) {
                setTitle(
                        String.format(
                                i18n("history.history_category"),
                                currentCategory.getDescription()));
            }
        });
        categoryManager.notifyListeners();

        // statistics, table
        solutionManager.addListener(new SolutionManager.Listener() {
            @Override
            public void solutionsUpdated(Solution[] solutions) {
                int[] selectedRows = new int[solutions.length];
                for (int i = 0; i < selectedRows.length; i++) selectedRows[i] = i;

                HistoryFrame.this.graphPanel.setSolutions(solutions);
                updateStatistics(solutions, selectedRows);
                updateTable(solutions);
            }
        });
        solutionManager.notifyListeners();

        // table selection
        this.table.getSelectionModel().addListSelectionListener(
                event -> {
                    Solution[] solutions = solutionManager.getSolutions();
                    Solution[] selectedSolutions;

                    int[] selectedRows = HistoryFrame.this.table.getSelectedRows();
                    if (selectedRows.length <= 0) {
                        selectedRows = new int[HistoryFrame.this.table.getRowCount()];
                        for (int i = 0; i < selectedRows.length; i++) selectedRows[i] = i;

                        selectedSolutions = solutions;
                    } else {
                        selectedSolutions = new Solution[selectedRows.length];
                        for (int i = 0; i < selectedSolutions.length; i++)
                            selectedSolutions[i] = solutions[selectedRows[i]];
                    }

                    HistoryFrame.this.graphPanel.setSolutions(selectedSolutions);
                    updateStatistics(selectedSolutions, selectedRows);

                    HistoryFrame.this.buttonEdit.setEnabled(
                            HistoryFrame.this.table.getSelectedRowCount() == 1);
                    HistoryFrame.this.buttonRemove.setEnabled(
                            HistoryFrame.this.table.getSelectedRowCount() > 0);
                });

        // edit button
        this.buttonEdit.addActionListener(e -> {
            Solution[] solutions = solutionManager.getSolutions();
            Solution solution = solutions[HistoryFrame.this.table.getSelectedRow()];

            SolutionEditingDialog.SolutionEditingDialogListener listener =
                    new SolutionEditingDialog.SolutionEditingDialogListener() {
                        @Override
                        public void solutionEdited(Solution solution) {
                            solutionManager.updateSolution(solution);
                        }
                    };

            SolutionEditingDialog solutionEditingDialog =
                    new SolutionEditingDialog(
                            HistoryFrame.this,
                            true,
                            solution,
                            listener
                    );
            solutionEditingDialog.setLocationRelativeTo(null);
            solutionEditingDialog.setVisible(true);
        });

        // remove button
        this.buttonRemove.addActionListener(e -> {
            if (HistoryFrame.this.table.getSelectedRows().length > 5) {
                int result = JOptionPane.showConfirmDialog(
                        HistoryFrame.this,
                        i18n("history.solution_removal_confirmation_message"),
                        i18n("history.remove_solutions"),
                        JOptionPane.YES_NO_CANCEL_OPTION);
                if (result != JOptionPane.YES_OPTION) return;
            }

            Solution[] solutions = solutionManager.getSolutions();

            int[] selectedRows = HistoryFrame.this.table.getSelectedRows();
            for (int selectedRow : selectedRows) solutionManager.removeSolution(solutions[selectedRow]);

            // request focus
            HistoryFrame.this.buttonRemove.requestFocusInWindow();
        });

        // select session button
        this.buttonSelectSession.addActionListener(e -> {
            if (HistoryFrame.this.table.getRowCount() > 0)
                HistoryFrame.this.table.removeRowSelectionInterval(0, HistoryFrame.this.table.getRowCount() - 1);

            Solution[] solutions = solutionManager.getSolutions();
            Solution[] sessionSolutions = sessionManager.getSolutions();

            for (int i = 0, j = 0; i < solutions.length && j < sessionSolutions.length; i++)
                if (solutions[i].getSolutionId().equals(sessionSolutions[j].getSolutionId())) {
                    HistoryFrame.this.table.addRowSelectionInterval(i, i);
                    HistoryFrame.this.table.scrollRectToVisible(HistoryFrame.this.table.getCellRect(i, 0, true));
                    j++;
                }
        });

        // select none button
        this.buttonSelectNone.addActionListener(e -> {
            if (HistoryFrame.this.table.getRowCount() > 0)
                HistoryFrame.this.table.removeRowSelectionInterval(0, HistoryFrame.this.table.getRowCount() - 1);
        });

        // close button
        this.setDefaultCloseOperation(HIDE_ON_CLOSE);
        this.buttonOk.addActionListener(arg0 -> HistoryFrame.this.setVisible(false));

        // esc key closes window
        this.getRootPane().registerKeyboardAction(
                arg0 -> HistoryFrame.this.setVisible(false),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void createComponents() {
        setLayout(
                new MigLayout(
                        "fill",
                        "[][pref!]",
                        "[pref!][pref!]12[pref!][pref!]12[pref!][]16[pref!]"));

        // labelGraph
        add(new JLabel(i18n("history.graph")), "span, wrap");

        // Graph
        this.graphPanel = new GraphPanel(new Solution[0]);
        this.graphPanel.setBackground(this.getBackground());
        add(this.graphPanel, "growx, height 90, span, wrap");

        // labelStatistics
        add(new JLabel(i18n("history.statistics")), "span, wrap");

        // panelStatistics
        JPanel panelStatistics = new JPanel(
                new MigLayout(
                        "fill, insets 0 n 0 n",
                        "[][pref!]32[][pref!]32[][pref!]32[][pref!]",
                        "[pref!]1[pref!]1[pref!]1[pref!]1[pref!]"));
        add(panelStatistics, "growx, span, wrap");

        // labelNumberOfSolutions
        panelStatistics.add(new JLabel(i18n("history.number_of_solutions")), "");
        this.labelNumberOfSolutions = new JLabel("");
        panelStatistics.add(this.labelNumberOfSolutions, "right");

        // labelBest
        panelStatistics.add(new JLabel(i18n("history.best")), "");
        this.labelBest = new JLabel(this.nullTime);
        panelStatistics.add(this.labelBest, "right");

        // labelMeanOf3
        panelStatistics.add(new JLabel(i18n("history.mean_of_3")), "");
        this.labelMeanOf3 = new JLabel(this.nullTime);
        panelStatistics.add(this.labelMeanOf3, "right");

        // labelBestMeanOf3
        panelStatistics.add(new JLabel(i18n("history.best_mean_of_3")), "");
        this.labelBestMeanOf3 = new JLabel(this.nullTime);
        panelStatistics.add(this.labelBestMeanOf3, "right, wrap");

        // labelMean
        panelStatistics.add(new JLabel(i18n("history.mean")), "");
        this.labelMean = new JLabel(this.nullTime);
        panelStatistics.add(this.labelMean, "right");

        // labelLowerQuartile
        panelStatistics.add(new JLabel(i18n("history.lower_quartile")), "");
        this.labelLowerQuartile = new JLabel(this.nullTime);
        panelStatistics.add(this.labelLowerQuartile, "right");

        // labelMeanOf10
        panelStatistics.add(new JLabel(i18n("history.mean_of_10")), "");
        this.labelMeanOf10 = new JLabel(this.nullTime);
        panelStatistics.add(this.labelMeanOf10, "right");

        // labelBestMeanOf10
        panelStatistics.add(new JLabel(i18n("history.best_mean_of_10")), "");
        this.labelBestMeanOf10 = new JLabel(this.nullTime);
        panelStatistics.add(this.labelBestMeanOf10, "right, wrap");

        // labelAverage
        panelStatistics.add(new JLabel(i18n("history.average")), "");
        this.labelAverage = new JLabel(this.nullTime);
        panelStatistics.add(this.labelAverage, "right");

        // labelMedian
        panelStatistics.add(new JLabel(i18n("history.median")), "");
        this.labelMedian = new JLabel(this.nullTime);
        panelStatistics.add(this.labelMedian, "right");

        // labelMeanOf100
        panelStatistics.add(new JLabel(i18n("history.mean_of_100")), "");
        this.labelMeanOf100 = new JLabel(this.nullTime);
        panelStatistics.add(this.labelMeanOf100, "right");

        // labelBestMeanOf100
        panelStatistics.add(new JLabel(i18n("history.best_mean_of_100")), "");
        this.labelBestMeanOf100 = new JLabel(this.nullTime);
        panelStatistics.add(this.labelBestMeanOf100, "right, wrap");

        // labelInterquartileMean
        panelStatistics.add(new JLabel(i18n("history.interquartile_mean")), "");
        this.labelInterquartileMean = new JLabel(this.nullTime);
        panelStatistics.add(this.labelInterquartileMean, "right");

        // labelUpperQuartile
        panelStatistics.add(new JLabel(i18n("history.upper_quartile")), "");
        this.labelUpperQuartile = new JLabel(this.nullTime);
        panelStatistics.add(this.labelUpperQuartile, "right");

        // labelAverageOf5
        panelStatistics.add(new JLabel(i18n("history.average_of_5")), "");
        this.labelAverageOf5 = new JLabel(this.nullTime);
        panelStatistics.add(this.labelAverageOf5, "right");

        // labelBestAverageOf5
        panelStatistics.add(new JLabel(i18n("history.best_average_of_5")), "");
        this.labelBestAverageOf5 = new JLabel(this.nullTime);
        panelStatistics.add(this.labelBestAverageOf5, "right, wrap");

        // labelStandardDeviation
        panelStatistics.add(new JLabel(i18n("history.standard_deviation")), "");
        this.labelStandardDeviation = new JLabel(this.nullTime);
        panelStatistics.add(this.labelStandardDeviation, "right");

        // labelWorst
        panelStatistics.add(new JLabel(i18n("history.worst")), "");
        this.labelWorst = new JLabel(this.nullTime);
        panelStatistics.add(this.labelWorst, "right");

        // labelAverageOf12
        panelStatistics.add(new JLabel(i18n("history.average_of_12")), "");
        this.labelAverageOf12 = new JLabel(this.nullTime);
        panelStatistics.add(this.labelAverageOf12, "right");

        // labelBestAverageOf12
        panelStatistics.add(new JLabel(i18n("history.best_average_of_12")), "");
        this.labelBestAverageOf12 = new JLabel(this.nullTime);
        panelStatistics.add(this.labelBestAverageOf12, "right, wrap");

        // labelAccuracy
        panelStatistics.add(new JLabel(i18n("history.accuracy")), "");
        this.labelAccuracy = new JLabel(this.nullTime);
        panelStatistics.add(this.labelAccuracy, "right");

        // labelFullWorst
        panelStatistics.add(new JLabel(i18n("history.full_worst")), "");
        this.labelFullWorst = new JLabel(this.nullTime);
        panelStatistics.add(this.labelFullWorst, "right");

        // labelFullAverage
        panelStatistics.add(new JLabel(i18n("history.full_average")), "");
        this.labelFullAverage = new JLabel(this.nullTime);
        panelStatistics.add(this.labelFullAverage, "right");

        // labelMeanPercent
        panelStatistics.add(new JLabel(i18n("history.mean_50%")), "");
        this.labelMeanPercent = new JLabel(this.nullTime);
        panelStatistics.add(this.labelMeanPercent, "right");

        // labelSolutions
        JLabel labelTimes = new JLabel(i18n("history.solutions"));
        add(labelTimes, "span, wrap");

        // table
        this.table = new JTable();
        this.table.setShowVerticalLines(false);

        JScrollPane scrollPane = new JScrollPane(this.table);
        this.table.setFillsViewportHeight(true);
        scrollPane.setPreferredSize(new Dimension(0, 0));
        add(scrollPane, "grow");

        // buttonEdit
        this.buttonEdit = new JButton(i18n("history.edit"));
        this.buttonEdit.setEnabled(false);
        add(this.buttonEdit, "growx, top, split 5, flowy");

        // buttonRemove
        this.buttonRemove = new JButton(i18n("history.remove"));
        this.buttonRemove.setEnabled(false);
        add(this.buttonRemove, "growx, top");

        // buttonSelectSession
        this.buttonSelectSession = new JButton(i18n("history.select_session"));
        add(this.buttonSelectSession, "growx, top, gaptop 16");

        // buttonSelectNone
        this.buttonSelectNone = new JButton(i18n("history.select_none"));
        add(this.buttonSelectNone, "growx, top, wrap");

        // buttonOk
        this.buttonOk = new JButton(i18n("history.ok"));
        add(this.buttonOk, "tag ok, span");
    }

    private void updateStatistics(Solution[] solutions, final int[] selectedRows) {
        this.labelNumberOfSolutions.setText(Integer.toString(solutions.length));

        JLabel labels[] = {
                this.labelBest,
                this.labelMeanOf3,
                this.labelBestMeanOf3,

                this.labelMean,
                this.labelLowerQuartile,
                this.labelMeanOf10,
                this.labelBestMeanOf10,

                this.labelAverage,
                this.labelMedian,
                this.labelMeanOf100,
                this.labelBestMeanOf100,

                this.labelInterquartileMean,
                this.labelUpperQuartile,
                this.labelAverageOf5,
                this.labelBestAverageOf5,

                this.labelStandardDeviation,
                this.labelWorst,
                this.labelAverageOf12,
                this.labelBestAverageOf12,

                this.labelAccuracy,
                this.labelFullWorst,
                this.labelFullAverage,
                this.labelMeanPercent,
        };

        StatisticalMeasure[] measures = {
                new Best(1, Integer.MAX_VALUE),
                new Mean(3, 3),
                new BestMean(3, Integer.MAX_VALUE),

                new Mean(1, Integer.MAX_VALUE),
                new Percentile(1, Integer.MAX_VALUE, 0.25),
                new Mean(10, 10),
                new BestMean(10, Integer.MAX_VALUE),

                new Average(3, Integer.MAX_VALUE),
                new Percentile(1, Integer.MAX_VALUE, 0.5),
                new Mean(100, 100),
                new BestMean(100, Integer.MAX_VALUE),

                new InterquartileMean(3, Integer.MAX_VALUE),
                new Percentile(1, Integer.MAX_VALUE, 0.75),
                new Average(5, 5),
                new BestAverage(5, Integer.MAX_VALUE),

                new StandardDeviation(1, Integer.MAX_VALUE),
                new Worst(1, Integer.MAX_VALUE),
                new Average(12, 12),
                new BestAverage(12, Integer.MAX_VALUE),

                new Accuracy(1, Integer.MAX_VALUE),
                new FullWorst(1, Integer.MAX_VALUE),
                new FullAverage(3, Integer.MAX_VALUE),
                new MeanPercent(1, Integer.MAX_VALUE)
        };

        boolean[] clickable = {
                true,
                true,
                true,

                false,
                false,
                true,
                true,

                false,
                false,
                true,
                true,

                false,
                false,
                true,
                true,

                false,
                true,
                true,
                true,

                false,
                true,
                false,
                false
        };

        for (int i = 0; i < labels.length; i++) {
            if (solutions.length >= measures[i].getMinimumWindowSize()) {
                int size = Math.min(solutions.length, measures[i].getMaximumWindowSize());

                Solution[] window = new Solution[size];
                System.arraycopy(solutions, 0, window, 0, size);

                measures[i].setSolutions(window);
                labels[i].setText(measures[i].toFullString());
            } else labels[i].setText(this.nullTime);

            if (clickable[i]) {
                MouseListener[] mouseListeners = labels[i].getMouseListeners();
                for (MouseListener mouseListener : mouseListeners) labels[i].removeMouseListener(mouseListener);

                labels[i].setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

                if (solutions.length >= measures[i].getMinimumWindowSize()) {
                    labels[i].setCursor(new Cursor(Cursor.HAND_CURSOR));

                    final int windowSize = measures[i].getMinimumWindowSize();
                    final int windowPosition = measures[i].getWindowPosition();

                    labels[i].addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            if (HistoryFrame.this.table.getRowCount() > 0)
                                HistoryFrame.this.table.removeRowSelectionInterval(0, HistoryFrame.this.table.getRowCount() - 1);

                            for (int i = 0; i < windowSize; i++) {
                                HistoryFrame.this.table.addRowSelectionInterval(
                                        selectedRows[windowPosition + i],
                                        selectedRows[windowPosition + i]);
                                HistoryFrame.this.table.scrollRectToVisible(HistoryFrame.this.table.getCellRect(selectedRows[windowPosition + i], 0, true));
                            }
                        }
                    });
                }
            }
        }
    }

    private void updateTable(Solution[] solutions) {
        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableModel.addColumn(i18n("history.#"));
        tableModel.addColumn(i18n("history.start"));
        tableModel.addColumn(i18n("history.time"));
        tableModel.addColumn(i18n("history.penalty"));
        tableModel.addColumn(i18n("history.scramble"));

        this.table.setModel(tableModel);

        this.table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        int[] columnsWidth = {100, 400, 200, 200, 1000};
        for (int i = 0; i < columnsWidth.length; i++) {
            TableColumn indexColumn = this.table.getColumnModel().getColumn(i);
            indexColumn.setPreferredWidth(columnsWidth[i]);
        }

        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);

        for (int i = 0; i < solutions.length; i++) {
            // start
            String sStart = dateFormat.format(solutions[i].getTiming().getStart());

            // time
            String sTime = SolutionUtils.formatMinutes(solutions[i].getTiming().getElapsedTime(), false);

            tableModel.addRow(new Object[]{
                    solutions.length - i,
                    sStart,
                    sTime,
                    solutions[i].getPenalty(),
                    solutions[i].getScramble().getRawSequence(),
            });
        }
    }
}
