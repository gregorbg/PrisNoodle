package com.puzzletimer.gui;

import com.puzzletimer.categories.Category;
import com.puzzletimer.managers.*;
import com.puzzletimer.managers.MessageManager.MessageType;
import com.puzzletimer.models.Scramble;
import com.puzzletimer.models.Solution;
import com.puzzletimer.models.Timing;
import com.puzzletimer.parsers.ScrambleParserProvider;
import com.puzzletimer.puzzles.Puzzle;
import com.puzzletimer.puzzles.PuzzleProvider;
import com.puzzletimer.scramblers.Scrambler;
import com.puzzletimer.scramblers.ScramblerProvider;
import com.puzzletimer.statistics.*;
import com.puzzletimer.timer.ControlKeysTimer;
import com.puzzletimer.timer.ManualInputTimer;
import com.puzzletimer.timer.SpaceKeyTimer;
import com.puzzletimer.timer.StackmatTimer;
import com.puzzletimer.tips.TipProvider;
import com.puzzletimer.util.SolutionUtils;
import net.gnehzr.tnoodle.scrambles.InvalidScrambleException;
import net.miginfocom.swing.MigLayout;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.svg.SVGDocument;

import javax.sound.sampled.*;
import javax.sound.sampled.DataLine.Info;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import static com.puzzletimer.Internationalization.i18n;

@SuppressWarnings("serial")
public class MainFrame extends JFrame {
    private class ScramblePanel extends JPanel {
        private ScrambleViewerPanel scrambleViewerPanel;

        public ScramblePanel(ScrambleManager scrambleManager) {
            createComponents();

            scrambleManager.addListener(new ScrambleManager.Listener() {
                @Override
                public void scrambleChanged(Scramble scramble) {
                    setScramble(scramble);
                }
            });
        }

        public void setScrambleViewerPanel(ScrambleViewerPanel scrambleViewerPanel) {
            this.scrambleViewerPanel = scrambleViewerPanel;
        }

        private void createComponents() {
            setLayout(new WrapLayout(10, 3));
        }

        private void setScramble(final Scramble scramble) {
            removeAll();

            final JLabel[] labels = new JLabel[scramble.getSequence().length];
            for (int i = 0; i < labels.length; i++) {
                labels[i] = new JLabel(scramble.getSequence()[i]);
                labels[i].setFont(new Font("Arial", Font.PLAIN, 18));
                labels[i].setCursor(new Cursor(Cursor.HAND_CURSOR));

                final int index = i;
                labels[i].addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        for (int i = 0; i < labels.length; i++) {
                            labels[i].setForeground(
                                    i <= index ? Color.BLACK : Color.LIGHT_GRAY);
                        }

                        try {
                            ScramblePanel.this.scrambleViewerPanel.setScramble(
                                    new Scramble(
                                            scramble.getScramblerId(),
                                            Arrays.copyOf(scramble.getSequence(), index + 1)));
                        } catch (InvalidScrambleException e1) {
                            e1.printStackTrace();
                        }
                    }
                });

                add(labels[i], "gap 10");
            }

            revalidate();
            repaint();
        }
    }

    private class TimerPanel extends JPanel {
        private HandImage leftHand;
        private TimeLabel timeLabel;
        private JTextField textFieldTime;
        private HandImage rightHand;
        private boolean currentManualInput;
        private long time;

        public TimerPanel(TimerManager timerManager) {
            createComponents();

            timerManager.addListener(new TimerManager.Listener() {
                @Override
                public void timerReset() {
                    TimerPanel.this.time = 0;
                    TimerPanel.this.timeLabel.setForeground(Color.BLACK);
                    TimerPanel.this.timeLabel.setText(
                            SolutionUtils.formatMinutes(TimerPanel.this.time, false));
                }

                @Override
                public void leftHandPressed() {
                    TimerPanel.this.leftHand.setPressed(true);
                }

                @Override
                public void leftHandReleased() {
                    TimerPanel.this.leftHand.setPressed(false);
                }

                @Override
                public void rightHandPressed() {
                    TimerPanel.this.rightHand.setPressed(true);
                }

                @Override
                public void rightHandReleased() {
                    TimerPanel.this.rightHand.setPressed(false);
                }

                @Override
                public void inspectionRunning(long remainingTime) {
                    Color startColor = Color.BLACK;
                    Color endColor = new Color(0xD4, 0x11, 0x11);

                    Color color;
                    if (remainingTime > 7000) {
                        color = startColor;
                    } else if (remainingTime > 0) {
                        double x = remainingTime / 7000.0;
                        color = new Color(
                                (int) (x * startColor.getRed() + (1 - x) * endColor.getRed()),
                                (int) (x * startColor.getGreen() + (1 - x) * endColor.getGreen()),
                                (int) (x * startColor.getBlue() + (1 - x) * endColor.getBlue()));
                    } else {
                        color = endColor;
                        remainingTime = 0;
                    }

                    TimerPanel.this.timeLabel.setForeground(color);
                    TimerPanel.this.timeLabel.setText(
                            Long.toString((long) Math.ceil(remainingTime / 1000.0)));
                }

                @Override
                public void solutionRunning(Timing timing) {
                    TimerPanel.this.time = timing.getElapsedTime();
                    TimerPanel.this.timeLabel.setForeground(Color.BLACK);
                    TimerPanel.this.timeLabel.setText(
                            SolutionUtils.formatMinutes(TimerPanel.this.time, false));
                    TimerPanel.this.timeLabel.revalidate();
                }

                @Override
                public void solutionFinished(Timing timing, String penalty) {
                    TimerPanel.this.time = timing.getElapsedTime();
                    TimerPanel.this.timeLabel.setForeground(Color.BLACK);
                    TimerPanel.this.timeLabel.setText(
                            SolutionUtils.formatMinutes(TimerPanel.this.time, false));
                }
            });
        }

        private void createComponents() {
            setLayout(new MigLayout("fill", "2%[19%]1%[56%]1%[19%]2%"));

            // leftHand
            this.leftHand = new HandImage(false);
            add(this.leftHand, "grow");

            // timeLabel
            this.timeLabel = new TimeLabel(SolutionUtils.formatMinutes(0, false));
            this.timeLabel.setFont(new Font("Arial", Font.BOLD, 108));
            add(this.timeLabel, "grow");

            // textFieldTime
            this.textFieldTime = new JTextField();
            this.textFieldTime.setHorizontalAlignment(JTextField.CENTER);
            this.textFieldTime.setFont(new Font("Arial", Font.BOLD, 108));

            // rightHand
            this.rightHand = new HandImage(true);
            add(this.rightHand, "grow");

            this.currentManualInput = false;

            this.updateTimer(MainFrame.this.configurationManager.getConfiguration("TIMER-TRIGGER").equals("MANUAL-INPUT"));
        }

        private void updateTimer(boolean manualInput) {
            if(this.currentManualInput && !manualInput) {
                remove(this.textFieldTime);
                setLayout(new MigLayout("fill", "2%[19%]1%[56%]1%[19%]2%"));
                add(this.leftHand, "grow");
                add(this.timeLabel, "grow");
                add(this.rightHand, "grow");
                MainFrame.this.requestFocusInWindow();
                this.updateUI();
                this.currentManualInput = false;
            } else if(!this.currentManualInput && manualInput) {
                remove(this.timeLabel);
                remove(this.leftHand);
                remove(this.rightHand);
                setLayout(new MigLayout("fill", "12%[76%]12%"));
                add(this.textFieldTime, "growx");
                this.textFieldTime.requestFocusInWindow();
                this.updateUI();
                this.currentManualInput = true;
            }
        }
    }

    private class TimesScrollPane extends JScrollPane {
        private SolutionManager solutionManager;

        private JPanel panel;

        public TimesScrollPane(SolutionManager solutionManager, SessionManager sessionManager) {
            this.solutionManager = solutionManager;

            createComponents();

            sessionManager.addListener(new SessionManager.Listener() {
                @Override
                public void solutionsUpdated(Solution[] solutions) {
                    setSolutions(solutions);
                }
            });
        }

        private void createComponents() {
            // scroll doesn't work without this
            setPreferredSize(new Dimension(0, 0));

            // panel
            this.panel = new JPanel(
                    new MigLayout(
                            "center",
                            "0[right]8[pref!]16[pref!]8[pref!]16[pref!]8[pref!]8[pref!]0",
                            ""));
        }

        private void setSolutions(final Solution[] solutions) {
            this.panel.removeAll();

            for (int i = 0; i < solutions.length; i++) {
                final Solution solution = solutions[i];

                JLabel labelIndex = new JLabel(Integer.toString(solutions.length - i) + ".");
                labelIndex.setFont(new Font("Tahoma", Font.BOLD, 13));
                this.panel.add(labelIndex);

                JLabel labelTime = new JLabel(SolutionUtils.formatMinutes(solutions[i].getTiming().getElapsedTime(), false));
                labelTime.setFont(new Font("Tahoma", Font.PLAIN, 13));
                this.panel.add(labelTime);

                final JLabel labelPlus2 = new JLabel("+2");
                labelPlus2.setFont(new Font("Tahoma", Font.PLAIN, 13));
                if (!solution.getPenalty().equals("+2")) {
                    labelPlus2.setForeground(Color.LIGHT_GRAY);
                }
                labelPlus2.setCursor(new Cursor(Cursor.HAND_CURSOR));
                labelPlus2.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (!solution.getPenalty().equals("+2") && !solution.getPenalty().equals("DNF")) {
                            TimesScrollPane.this.solutionManager.updateSolution(
                                    solution.setPenalty("+2"));
                        } else if (solution.getPenalty().equals("+2")) {
                            TimesScrollPane.this.solutionManager.updateSolution(
                                    solution.setPenalty(""));
                        }
                    }
                });
                this.panel.add(labelPlus2);

                final JLabel labelDNF = new JLabel("DNF");
                labelDNF.setFont(new Font("Tahoma", Font.PLAIN, 13));
                if (!solution.getPenalty().equals("DNF")) {
                    labelDNF.setForeground(Color.LIGHT_GRAY);
                }
                labelDNF.setCursor(new Cursor(Cursor.HAND_CURSOR));
                labelDNF.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (!solution.getPenalty().equals("DNF")) {
                            TimesScrollPane.this.solutionManager.updateSolution(
                                    solution.setPenalty("DNF"));
                        } else if (solution.getPenalty().equals("DNF")) {
                            TimesScrollPane.this.solutionManager.updateSolution(
                                    solution.setPenalty(""));
                        }
                    }
                });
                this.panel.add(labelDNF);

                JLabel labelRetry = new JLabel();
                labelRetry.setIcon(new ImageIcon(getClass().getResource("/com/puzzletimer/resources/retry.png")));
                labelRetry.setCursor(new Cursor(Cursor.HAND_CURSOR));
                labelRetry.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        MainFrame.this.scrambleManager.addScrambles(new Scramble[]{solution.getScramble()}, true);
                        TimesScrollPane.this.solutionManager.removeSolution(solution);
                        MainFrame.this.scrambleManager.changeScramble();
                    }
                });
                this.panel.add(labelRetry);

                JLabel labelX = new JLabel();
                labelX.setIcon(new ImageIcon(getClass().getResource("/com/puzzletimer/resources/x.png")));
                labelX.setCursor(new Cursor(Cursor.HAND_CURSOR));
                labelX.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        TimesScrollPane.this.solutionManager.removeSolution(solution);
                    }
                });
                this.panel.add(labelX, "wrap");
            }

            setViewportView(this.panel);
        }
    }

    private class StatisticsPanel extends JPanel {
        private JLabel labelMean;
        private JLabel labelAverage;
        private JLabel labelBestTime;
        private JLabel labelMedian;
        private JLabel labelWorstTime;
        private JLabel labelStandardDeviation;
        private JLabel labelMeanOf3;
        private JLabel labelBestMeanOf3;
        private JLabel labelAverageOf5;
        private JLabel labelBestAverageOf5;
        private JLabel labelAverageOf12;
        private JLabel labelBestAverageOf12;
        private String nullTime;

        private StatisticsPanel(SessionManager sessionManager) {
            this.nullTime = "XX:XX.XX";

            createComponents();

            final JLabel[] labels = {
                    this.labelMean,
                    this.labelAverage,
                    this.labelBestTime,
                    this.labelMedian,
                    this.labelWorstTime,
                    this.labelStandardDeviation,
                    this.labelMeanOf3,
                    this.labelBestMeanOf3,
                    this.labelAverageOf5,
                    this.labelBestAverageOf5,
                    this.labelAverageOf12,
                    this.labelBestAverageOf12,
            };

            final StatisticalMeasure[] measures = {
                    new Mean(1, Integer.MAX_VALUE),
                    new Average(3, Integer.MAX_VALUE),
                    new Best(1, Integer.MAX_VALUE),
                    new Percentile(1, Integer.MAX_VALUE, 0.5),
                    new Worst(1, Integer.MAX_VALUE),
                    new StandardDeviation(1, Integer.MAX_VALUE),
                    new Mean(3, 3),
                    new BestMean(3, Integer.MAX_VALUE),
                    new Average(5, 5),
                    new BestAverage(5, Integer.MAX_VALUE),
                    new Average(12, 12),
                    new BestAverage(12, Integer.MAX_VALUE),
            };

            sessionManager.addListener(new SessionManager.Listener() {
                @Override
                public void solutionsUpdated(Solution[] solutions) {
                    for (int i = 0; i < labels.length; i++) {
                        if (solutions.length >= measures[i].getMinimumWindowSize()) {
                            int size = Math.min(solutions.length, measures[i].getMaximumWindowSize());

                            Solution[] window = new Solution[size];
                            System.arraycopy(solutions, 0, window, 0, size);

                            measures[i].setSolutions(window);
                            labels[i].setText(measures[i].toFullString());
                        } else {
                            labels[i].setText(StatisticsPanel.this.nullTime);
                        }
                    }
                }
            });
        }

        private void createComponents() {
            setLayout(
                    new MigLayout(
                            "center",
                            "[pref!,right]8[pref!]",
                            "1[pref!]1[pref!]1[pref!]1[pref!]1[pref!]1[pref!]6[pref!]1[pref!]6[pref!]1[pref!]6[pref!]1[pref!]1"));

            // labelMean
            JLabel labelMeanDescription = new JLabel(i18n("statistics.mean"));
            labelMeanDescription.setFont(new Font("Tahoma", Font.BOLD, 11));
            add(labelMeanDescription);

            this.labelMean = new JLabel(this.nullTime);
            add(this.labelMean, "wrap");

            // labelAverage
            JLabel labelAverageDescription = new JLabel(i18n("statistics.average"));
            labelAverageDescription.setFont(new Font("Tahoma", Font.BOLD, 11));
            add(labelAverageDescription);

            this.labelAverage = new JLabel(this.nullTime);
            add(this.labelAverage, "wrap");

            // labelBestTime
            JLabel labelBestTimeDescription = new JLabel(i18n("statistics.best_time"));
            labelBestTimeDescription.setFont(new Font("Tahoma", Font.BOLD, 11));
            add(labelBestTimeDescription);

            this.labelBestTime = new JLabel(this.nullTime);
            add(this.labelBestTime, "wrap");

            // labelMedian
            JLabel labelMedianDescription = new JLabel(i18n("statistics.median"));
            labelMedianDescription.setFont(new Font("Tahoma", Font.BOLD, 11));
            add(labelMedianDescription);

            this.labelMedian = new JLabel(this.nullTime);
            add(this.labelMedian, "wrap");

            // labelWorstTime
            JLabel labelWorstTimeDescription = new JLabel(i18n("statistics.worst_time"));
            labelWorstTimeDescription.setFont(new Font("Tahoma", Font.BOLD, 11));
            add(labelWorstTimeDescription);

            this.labelWorstTime = new JLabel(this.nullTime);
            add(this.labelWorstTime, "wrap");

            // labelStandardDeviation
            JLabel labelStandardDeviationDescription = new JLabel(i18n("statistics.standard_deviation"));
            labelStandardDeviationDescription.setFont(new Font("Tahoma", Font.BOLD, 11));
            add(labelStandardDeviationDescription);

            this.labelStandardDeviation = new JLabel(this.nullTime);
            add(this.labelStandardDeviation, "wrap");

            // labelMeanOf3
            JLabel labelMeanOf3Description = new JLabel(i18n("statistics.mean_of_3"));
            labelMeanOf3Description.setFont(new Font("Tahoma", Font.BOLD, 11));
            add(labelMeanOf3Description);

            this.labelMeanOf3 = new JLabel(this.nullTime);
            add(this.labelMeanOf3, "wrap");

            // labelBestMeanOf3
            JLabel labelBestMeanOf3Description = new JLabel(i18n("statistics.best_mean_of_3"));
            labelBestMeanOf3Description.setFont(new Font("Tahoma", Font.BOLD, 11));
            add(labelBestMeanOf3Description);

            this.labelBestMeanOf3 = new JLabel(this.nullTime);
            add(this.labelBestMeanOf3, "wrap");

            // labelAverageOf5
            JLabel labelAverageOf5Description = new JLabel(i18n("statistics.average_of_5"));
            labelAverageOf5Description.setFont(new Font("Tahoma", Font.BOLD, 11));
            add(labelAverageOf5Description);

            this.labelAverageOf5 = new JLabel(this.nullTime);
            add(this.labelAverageOf5, "wrap");

            // labelBestAverageOf5
            JLabel labelBestAverageOf5Description = new JLabel(i18n("statistics.best_average_of_5"));
            labelBestAverageOf5Description.setFont(new Font("Tahoma", Font.BOLD, 11));
            add(labelBestAverageOf5Description);

            this.labelBestAverageOf5 = new JLabel(this.nullTime);
            add(this.labelBestAverageOf5, "wrap");

            // labelAverageOf12
            JLabel labelAverageOf12Description = new JLabel(i18n("statistics.average_of_12"));
            labelAverageOf12Description.setFont(new Font("Tahoma", Font.BOLD, 11));
            add(labelAverageOf12Description);

            this.labelAverageOf12 = new JLabel(this.nullTime);
            add(this.labelAverageOf12, "wrap");

            // labelBestAverageOf12
            JLabel labelBestAverageOf12Description = new JLabel(i18n("statistics.best_average_of_12"));
            labelBestAverageOf12Description.setFont(new Font("Tahoma", Font.BOLD, 11));
            add(labelBestAverageOf12Description);

            this.labelBestAverageOf12 = new JLabel(this.nullTime);
            add(this.labelBestAverageOf12, "wrap");
        }
    }

    public class ScrambleViewerPanel extends JPanel {
        private final SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(XMLResourceDescriptor.getXMLParserClassName());

        private ScramblerProvider scramblerProvider;
        private ColorManager colorManager;
        private Scramble currentScramble;

        private JCheckBox hide;
        private JSVGCanvas scrambleImagePanel;

        public ScrambleViewerPanel(
                ScramblerProvider scramblerProvider,
                ColorManager colorManager,
                ScrambleManager scrambleManager) {
            this.scramblerProvider = scramblerProvider;
            this.colorManager = colorManager;

            createComponents();

            scrambleManager.addListener(new ScrambleManager.Listener() {
                @Override
                public void scrambleChanged(Scramble scramble) {
                    try {
                        setScramble(scramble);
                    } catch (InvalidScrambleException e) {
                        scrambleManager.changeScramble();
                    }
                }
            });

            this.hide.addActionListener(e -> ScrambleViewerPanel.this.scrambleImagePanel.setVisible(!ScrambleViewerPanel.this.hide.isSelected()));
        }

        private void createComponents() {
            setLayout(new MigLayout("fill", "0[fill]0", "0[pref!]0[fill]0"));

            this.hide = new JCheckBox(i18n("main.hide"));
            this.hide.setFocusable(false);
            add(this.hide, "wrap");

            this.scrambleImagePanel = new JSVGCanvas();
            this.scrambleImagePanel.setBackground(getBackground());
            this.scrambleImagePanel.setFocusable(false);
            add(this.scrambleImagePanel);

            this.setMaximumSize(new Dimension(Integer.MAX_VALUE, 257));
        }

        public void setScramble(Scramble scramble) throws InvalidScrambleException {
            Scrambler scrambler = this.scramblerProvider.get(scramble.getScramblerId());
            this.currentScramble = scramble;
            Puzzle puzzle = scrambler.getPuzzle();

            String scrambleSvg = puzzle.getScrambleSVG(scramble.getRawSequence(), this.colorManager.getColorScheme(puzzle.getPuzzleId()));
            SVGDocument doc = null;

            if (scrambleSvg != null) {
                StringReader reader = new StringReader(scrambleSvg);

                try {
                    doc = f.createSVGDocument("", reader);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            this.scrambleImagePanel.setSVGDocument(doc);
        }

        public void updateSVGBackground() {
            this.scrambleImagePanel.setBackground(getBackground());
        }

        public void redrawScramble() throws InvalidScrambleException {
            this.setScramble(this.currentScramble);
        }
    }

    private MessageManager messageManager;
    private ConfigurationManager configurationManager;
    private TimerManager timerManager;
    private PuzzleProvider puzzleProvider;
    private ColorManager colorManager;
    private ScrambleParserProvider scrambleParserProvider;
    private ScramblerProvider scramblerProvider;
    private TipProvider tipProvider;
    private CategoryManager categoryManager;
    private ScrambleManager scrambleManager;
    private SolutionManager solutionManager;
    private SessionManager sessionManager;

    private JMenuItem menuItemAddSolution;
    private JMenuItem menuItemExit;
    private JMenuItem menuItemTips;
    private JMenuItem menuItemScrambleQueue;
    private JMenuItem menuItemHistory;
    private JMenuItem menuItemSessionSummary;
    private JMenuItem menuItemStackmatDeveloper;
    private JMenu menuCategory;
    private JMenuItem menuItemColorScheme;
    private JCheckBoxMenuItem menuItemInspectionTime;
    private JCheckBoxMenuItem menuItemSmoothTiming;
    private JMenu stackmatTimerInputDevice;
    private ButtonGroup stackmatTimerInputDeviceGroup;
    private JRadioButtonMenuItem menuItemManualInput;
    private JRadioButtonMenuItem menuItemCtrlKeys;
    private JRadioButtonMenuItem menuItemSpaceKey;
    private JRadioButtonMenuItem menuItemStackmatTimer;
    private JMenu menuLookAndFeel;
    private ButtonGroup lookAndFeelGroup;
    private JRadioButtonMenuItem menuItemDefaultLnF;

    private JMenuItem menuItemAbout;
    private JMenuItem menuItemFeedback;
    private JLabel labelMessage;
    private TimerPanel timerPanel;
    private ScrambleViewerPanel scrambleViewerPanel;

    private TipsFrame tipsFrame;
    private ScrambleQueueFrame scrambleQueueFrame;
    private HistoryFrame historyFrame;
    private SessionSummaryFrame sessionSummaryFrame;
    private StackmatDeveloperFrame stackmatDeveloperFrame;
    private CategoryManagerFrame categoryManagerDialog;
    private ColorSchemeFrame colorSchemeFrame;

    private AudioFormat audioFormat;
    private Mixer.Info mixerInfo;


    public MainFrame(
            MessageManager messageManager,
            ConfigurationManager configurationManager,
            TimerManager timerManager,
            PuzzleProvider puzzleProvider,
            ColorManager colorManager,
            ScrambleParserProvider scrambleParserProvider,
            ScramblerProvider scramblerProvider,
            TipProvider tipProvider, CategoryManager categoryManager,
            ScrambleManager scrambleManager,
            SolutionManager solutionManager,
            SessionManager sessionManager) {
        this.messageManager = messageManager;
        this.puzzleProvider = puzzleProvider;
        this.colorManager = colorManager;
        this.scrambleParserProvider = scrambleParserProvider;
        this.scramblerProvider = scramblerProvider;
        this.configurationManager = configurationManager;
        this.timerManager = timerManager;
        this.tipProvider = tipProvider;
        this.categoryManager = categoryManager;
        this.scrambleManager = scrambleManager;
        this.solutionManager = solutionManager;
        this.sessionManager = sessionManager;

        setMinimumSize(new Dimension(800, 600));

        createComponents();
        pack();

        // timer configuration
        this.audioFormat = new AudioFormat(8000, 8, 1, true, false);
        this.mixerInfo = null;

        String stackmatTimerInputDeviceName =
                this.configurationManager.getConfiguration("STACKMAT-TIMER-INPUT-DEVICE");
        for (Mixer.Info mixerInfo : AudioSystem.getMixerInfo()) {
            if (stackmatTimerInputDeviceName.equals(mixerInfo.getName())) {
                this.mixerInfo = mixerInfo;
                break;
            }
        }

        setTimerTrigger(this.configurationManager.getConfiguration("TIMER-TRIGGER"));

        // inspection time sounds
        try {
            final Clip inspectionBeep = AudioSystem.getClip();
            inspectionBeep.open(AudioSystem.getAudioInputStream(new BufferedInputStream(MainFrame.class.getResourceAsStream("/com/puzzletimer/resources/inspection/beep.wav"))));

            this.timerManager.addListener(new TimerManager.Listener() {
                private int next;

                @Override
                public void inspectionStarted() {
                    this.next = 0;
                }

                @Override
                public void inspectionRunning(long remainingTime) {
                    int[] soundStartTimes = {7000, 3000, Integer.MIN_VALUE};
                    if (remainingTime <= soundStartTimes[this.next]) {
                        inspectionBeep.setFramePosition(0);
                        inspectionBeep.start();
                        this.next++;
                    }
                }
            });
        } catch (Exception ignored) {
        }

        // title
        this.categoryManager.addListener(new CategoryManager.Listener() {
            @Override
            public void categoriesUpdated(Category[] categories, Category currentCategory) {
                setTitle(
                        String.format(
                                i18n("main.prisma_puzzle_time_category"),
                                currentCategory.getDescription()));
            }
        });

        // menuItemAddSolution
        this.menuItemAddSolution.addActionListener(e -> {
            Date now = new Date();
            Solution solution = new Solution(
                    UUID.randomUUID(),
                    MainFrame.this.categoryManager.getCurrentCategory().getCategoryId(),
                    MainFrame.this.sessionManager.getSessionID(),
                    MainFrame.this.scrambleManager.getCurrentScramble(),
                    new Timing(now, now),
                    "");

            SolutionEditingDialog.SolutionEditingDialogListener listener =
                    new SolutionEditingDialog.SolutionEditingDialogListener() {
                        @Override
                        public void solutionEdited(Solution solution) {
                            MainFrame.this.solutionManager.addSolution(solution);

                            // check for personal records
                            StatisticalMeasure[] measures = {
                                    new Best(1, Integer.MAX_VALUE),
                                    new BestMean(3, 3),
                                    new BestMean(100, 100),
                                    new BestAverage(5, 5),
                                    new BestAverage(12, 12),
                            };

                            String[] descriptions = {
                                    i18n("main.single"),
                                    i18n("main.mean_of_3"),
                                    i18n("main.mean_of_100"),
                                    i18n("main.average_of_5"),
                                    i18n("main.average_of_12"),
                            };

                            Solution[] solutions = MainFrame.this.solutionManager.getSolutions();
                            Solution[] sessionSolutions = MainFrame.this.sessionManager.getSolutions();

                            for (int i = 0; i < measures.length; i++) {
                                if (sessionSolutions.length < measures[i].getMinimumWindowSize()) {
                                    continue;
                                }

                                measures[i].setSolutions(solutions);
                                long allTimeBest = measures[i].getValue();

                                measures[i].setSolutions(sessionSolutions);
                                long sessionBest = measures[i].getValue();

                                if (measures[i].getWindowPosition() == 0 && sessionBest <= allTimeBest) {
                                    MainFrame.this.messageManager.enqueueMessage(
                                            MessageType.INFORMATION,
                                            String.format(i18n("main.personal_record_message"),
                                                    MainFrame.this.categoryManager.getCurrentCategory().getDescription(),
                                                    SolutionUtils.formatMinutes(measures[i].getValue(), measures[i].getRound()),
                                                    descriptions[i]));
                                }
                            }

                            MainFrame.this.scrambleManager.changeScramble();
                        }
                    };

            SolutionEditingDialog solutionEditingDialog =
                    new SolutionEditingDialog(MainFrame.this, true, solution, listener);
            solutionEditingDialog.setTitle(i18n("main.add_solution_title"));
            solutionEditingDialog.setLocationRelativeTo(null);
            solutionEditingDialog.setVisible(true);
        });

        // menuItemExit
        this.menuItemExit.addActionListener(e -> System.exit(0));

        // menuItemTips
        this.menuItemTips.addActionListener(e -> MainFrame.this.tipsFrame.setVisible(true));

        // menuItemScrambleQueue
        this.menuItemScrambleQueue.addActionListener(e -> MainFrame.this.scrambleQueueFrame.setVisible(true));

        // menuItemHistory
        this.menuItemHistory.addActionListener(e -> MainFrame.this.historyFrame.setVisible(true));

        // menuItemSessionSummary
        this.menuItemSessionSummary.addActionListener(e -> MainFrame.this.sessionSummaryFrame.setVisible(true));

        // menuItemStackmatDeveloper
        this.menuItemStackmatDeveloper.addActionListener(e -> MainFrame.this.stackmatDeveloperFrame.setVisible(true));

        // menuItemScrambleQueue
        this.menuItemScrambleQueue.addActionListener(e -> MainFrame.this.scrambleQueueFrame.setVisible(true));

        // menuItemHistory
        this.menuItemHistory.addActionListener(e -> MainFrame.this.historyFrame.setVisible(true));

        // menuItemSessionSummary
        this.menuItemSessionSummary.addActionListener(e -> MainFrame.this.sessionSummaryFrame.setVisible(true));

        // menuCategory
        this.categoryManager.addListener(new CategoryManager.Listener() {
            @Override
            public void categoriesUpdated(Category[] categories, Category currentCategory) {
                int menuShortcutKey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

                MainFrame.this.menuCategory.removeAll();

                // category manager
                JMenuItem menuItemCategoryManager = new JMenuItem(i18n("main.category_manager"));
                menuItemCategoryManager.setMnemonic(KeyEvent.VK_M);
                menuItemCategoryManager.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, menuShortcutKey | InputEvent.ALT_MASK));
                menuItemCategoryManager.addActionListener(event -> MainFrame.this.categoryManagerDialog.setVisible(true));
                MainFrame.this.menuCategory.add(menuItemCategoryManager);

                MainFrame.this.menuCategory.addSeparator();

                ButtonGroup categoryGroup = new ButtonGroup();

                for (final Category wcaCategory : categories) {
                    if (!wcaCategory.isCustom()) {
                        JRadioButtonMenuItem menuItemCategory = new JRadioButtonMenuItem(wcaCategory.getDescription());
                        menuItemCategory.setMnemonic(wcaCategory.getMnemonic());
                        menuItemCategory.setAccelerator(KeyStroke.getKeyStroke(wcaCategory.getAccelerator(), wcaCategory.hasAlt() ? menuShortcutKey | KeyEvent.ALT_MASK : menuShortcutKey));
                        menuItemCategory.setSelected(wcaCategory == currentCategory);
                        menuItemCategory.addActionListener(e -> MainFrame.this.categoryManager.setCurrentCategory(wcaCategory));
                        MainFrame.this.menuCategory.add(menuItemCategory);
                        categoryGroup.add(menuItemCategory);
                    }
                }

                MainFrame.this.menuCategory.add(new JSeparator());

                for (final Category wcaCategory : categories) {
                    if (wcaCategory.isCustom()) {
                        JRadioButtonMenuItem menuItemCategory = new JRadioButtonMenuItem(wcaCategory.getDescription());
                        menuItemCategory.setMnemonic(wcaCategory.getMnemonic());
                        menuItemCategory.setSelected(wcaCategory == currentCategory);
                        menuItemCategory.addActionListener(e -> MainFrame.this.categoryManager.setCurrentCategory(wcaCategory));
                        MainFrame.this.menuCategory.add(menuItemCategory);
                        categoryGroup.add(menuItemCategory);
                    }
                }
            }
        });

        // menuColorScheme
        this.menuItemColorScheme.addActionListener(e -> MainFrame.this.colorSchemeFrame.setVisible(true));

        // menuItemSmoothTiming
        this.menuItemSmoothTiming.setSelected(timerManager.isSmoothTimingEnabled());
        this.menuItemSmoothTiming.addActionListener(e -> MainFrame.this.timerManager.setSmoothTimingEnabled(
                MainFrame.this.menuItemSmoothTiming.isSelected()));

        // menuItemInspectionTime
        this.menuItemInspectionTime.setSelected(timerManager.isInspectionEnabled());
        this.menuItemInspectionTime.addActionListener(e -> MainFrame.this.timerManager.setInspectionEnabled(MainFrame.this.menuItemInspectionTime.isSelected(), MainFrame.this.categoryManager.getCurrentCategory().isBldMode()));

        // menuItemManualInput
        this.menuItemManualInput.addActionListener(e -> setTimerTrigger("MANUAL-INPUT"));

        // menuItemCtrlKeys
        this.menuItemCtrlKeys.addActionListener(e -> setTimerTrigger("KEYBOARD-TIMER-CONTROL"));

        // menuItemSpaceKey
        this.menuItemSpaceKey.addActionListener(e -> setTimerTrigger("KEYBOARD-TIMER-SPACE"));

        // menuItemStackmatTimer
        this.menuItemStackmatTimer.addActionListener(e -> setTimerTrigger("STACKMAT-TIMER"));

        // menuItemDevice
        for (final Mixer.Info mixerInfo : AudioSystem.getMixerInfo()) {
            Line.Info[] targetLinesInfo = AudioSystem.getTargetLineInfo(new Info(TargetDataLine.class, this.audioFormat));

            boolean validMixer = false;
            for (Line.Info lineInfo : targetLinesInfo)
                if (AudioSystem.getMixer(mixerInfo).isLineSupported(lineInfo)) {
                    validMixer = true;
                    break;
                }

            if (!validMixer) continue;

            JRadioButtonMenuItem menuItemDevice = new JRadioButtonMenuItem(mixerInfo.getName());
            menuItemDevice.setSelected(stackmatTimerInputDeviceName.equals(mixerInfo.getName()));
            menuItemDevice.addActionListener(arg0 -> {
                MainFrame.this.mixerInfo = mixerInfo;
                MainFrame.this.configurationManager.setConfiguration("STACKMAT-TIMER-INPUT-DEVICE", mixerInfo.getName());

                String timerTrigger = MainFrame.this.configurationManager.getConfiguration("TIMER-TRIGGER");
                if (timerTrigger.equals("STACKMAT-TIMER")) setTimerTrigger("STACKMAT-TIMER");
            });
            this.stackmatTimerInputDevice.add(menuItemDevice);
            this.stackmatTimerInputDeviceGroup.add(menuItemDevice);

            if (MainFrame.this.mixerInfo == null) {
                menuItemDevice.setSelected(true);
                MainFrame.this.mixerInfo = mixerInfo;
                this.configurationManager.setConfiguration("STACKMAT-TIMER-INPUT-DEVICE", mixerInfo.getName());
            }
        }

        this.menuItemDefaultLnF.addActionListener(e -> changeLookAndFeel(UIManager.getSystemLookAndFeelClassName()));

        for (final UIManager.LookAndFeelInfo lafInfo : UIManager.getInstalledLookAndFeels()) {
            JRadioButtonMenuItem lafMenuItem = new JRadioButtonMenuItem(lafInfo.getName());
            this.menuLookAndFeel.add(lafMenuItem);
            this.lookAndFeelGroup.add(lafMenuItem);

            if (UIManager.getLookAndFeel().getName().equals(lafInfo.getName())) {
                lafMenuItem.setSelected(true);
            }

            lafMenuItem.addActionListener(e -> changeLookAndFeel(lafInfo.getClassName()));
        }

        // menuItemFeedback
        this.menuItemFeedback.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(new URI("https://bitbucket.org/Methuselah96/puzzle-timer/issues/new"));
            } catch (Exception ex) {
                MainFrame.this.messageManager.enqueueMessage(MessageType.ERROR, "Failed to open feedback page: "+ex.getLocalizedMessage());
            }
        });

        // menuItemAbout
        this.menuItemAbout.addActionListener(e -> {
            AboutDialog aboutDialog = new AboutDialog(MainFrame.this, true);
            aboutDialog.setLocationRelativeTo(null);
            aboutDialog.setVisible(true);
        });

        // labelMessage
        this.messageManager.addListener(new MessageManager.Listener() {
            @Override
            public void messagesCleared() {
                MainFrame.this.labelMessage.setPreferredSize(new Dimension());
                MainFrame.this.labelMessage.setVisible(false);
            }

            @Override
            public void messageReceived(MessageType messageType, String message) {
                MainFrame.this.labelMessage.setPreferredSize(new Dimension(MainFrame.this.getWidth(), 30));
                if (messageType == MessageType.INFORMATION) {
                    MainFrame.this.labelMessage.setBackground(new Color(0x45, 0x73, 0xD5));
                } else if (messageType == MessageType.ERROR) {
                    MainFrame.this.labelMessage.setBackground(new Color(0xFF, 0x40, 0x40));
                }
                MainFrame.this.labelMessage.setText(message);
                MainFrame.this.labelMessage.setVisible(true);
            }
        });
    }

    private void changeLookAndFeel(String className) {
        this.configurationManager.setConfiguration("LOOK-AND-FEEL", className);
        try {
            UIManager.setLookAndFeel(className);
        } catch (Exception ignored) {}
        SwingUtilities.updateComponentTreeUI(this);
        SwingUtilities.updateComponentTreeUI(this.tipsFrame);
        SwingUtilities.updateComponentTreeUI(this.scrambleQueueFrame);
        SwingUtilities.updateComponentTreeUI(this.historyFrame);
        SwingUtilities.updateComponentTreeUI(this.sessionSummaryFrame);
        SwingUtilities.updateComponentTreeUI(this.stackmatDeveloperFrame);
        SwingUtilities.updateComponentTreeUI(this.categoryManagerDialog);
        SwingUtilities.updateComponentTreeUI(this.colorSchemeFrame);
        this.pack();
        this.scrambleViewerPanel.updateSVGBackground();
        this.tipsFrame.pack();
        this.scrambleQueueFrame.pack();
        this.historyFrame.pack();
        this.sessionSummaryFrame.pack();
        this.stackmatDeveloperFrame.pack();
        this.categoryManagerDialog.pack();
        this.colorSchemeFrame.pack();
    }

    private void setTimerTrigger(String timerTriggerId) {
        switch (timerTriggerId) {
            case "MANUAL-INPUT":
                this.menuItemManualInput.setSelected(true);
                this.timerPanel.updateTimer(true);
                this.timerManager.setTimer(
                        new ManualInputTimer(this.timerManager, this.timerPanel.textFieldTime), MainFrame.this.categoryManager.getCurrentCategory().isBldMode());
                break;
            case "KEYBOARD-TIMER-CONTROL":
                this.menuItemCtrlKeys.setSelected(true);
                this.timerPanel.updateTimer(false);
                this.timerManager.setTimer(
                        new ControlKeysTimer(this, this.timerManager), MainFrame.this.categoryManager.getCurrentCategory().isBldMode());
                break;
            case "KEYBOARD-TIMER-SPACE":
                this.menuItemSpaceKey.setSelected(true);
                this.timerPanel.updateTimer(false);
                this.timerManager.setTimer(new SpaceKeyTimer(this, this.timerManager), MainFrame.this.categoryManager.getCurrentCategory().isBldMode());
                break;
            case "STACKMAT-TIMER":
                if (this.mixerInfo != null) {
                    TargetDataLine targetDataLine;
                    try {
                        targetDataLine = AudioSystem.getTargetDataLine(MainFrame.this.audioFormat, MainFrame.this.mixerInfo);
                        targetDataLine.open(MainFrame.this.audioFormat);
                        this.menuItemStackmatTimer.setSelected(true);
                        this.timerPanel.updateTimer(false);
                        this.timerManager.setTimer(new StackmatTimer(targetDataLine, this.timerManager), MainFrame.this.categoryManager.getCurrentCategory().isBldMode());
                    } catch (LineUnavailableException e) {
                        // select the default timer
                        this.menuItemSpaceKey.setSelected(true);
                        this.timerPanel.updateTimer(false);
                        this.timerManager.setTimer(new SpaceKeyTimer(this, this.timerManager), MainFrame.this.categoryManager.getCurrentCategory().isBldMode());

                        MainFrame.this.messageManager.enqueueMessage(
                                MessageType.ERROR,
                                i18n("main.stackmat_timer_error_message"));
                    }
                } else {
                    // select the default timer
                    this.menuItemSpaceKey.setSelected(true);
                    this.timerPanel.updateTimer(false);
                    this.timerManager.setTimer(new SpaceKeyTimer(this, this.timerManager), MainFrame.this.categoryManager.getCurrentCategory().isBldMode());

                    MainFrame.this.messageManager.enqueueMessage(
                            MessageType.ERROR,
                            i18n("main.stackmat_timer_error_message"));
                }
                break;
        }

    }

    private void createComponents() {
        int menuShortcutKey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

        // menuBar
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        // menuFile
        JMenu menuFile = new JMenu(i18n("main.file"));
        menuFile.setMnemonic(KeyEvent.VK_F);
        menuBar.add(menuFile);

        // menuItemAddSolution
        this.menuItemAddSolution = new JMenuItem(i18n("main.add_solution"));
        this.menuItemAddSolution.setMnemonic(KeyEvent.VK_A);
        this.menuItemAddSolution.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, menuShortcutKey));
        menuFile.add(this.menuItemAddSolution);

        menuFile.addSeparator();

        // menuItemExit
        this.menuItemExit = new JMenuItem(i18n("main.exit"));
        this.menuItemExit.setMnemonic(KeyEvent.VK_X);
        menuFile.add(this.menuItemExit);

        // menuView
        JMenu menuView = new JMenu(i18n("main.view"));
        menuView.setMnemonic(KeyEvent.VK_V);
        menuBar.add(menuView);

        // menuItemTips
        this.menuItemTips = new JMenuItem(i18n("main.tips"));
        this.menuItemTips.setMnemonic(KeyEvent.VK_T);
        this.menuItemTips.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, menuShortcutKey | KeyEvent.ALT_MASK));
        menuView.add(this.menuItemTips);

        // menuItemScrambleQueue
        this.menuItemScrambleQueue = new JMenuItem(i18n("main.scramble_queue"));
        this.menuItemScrambleQueue.setMnemonic(KeyEvent.VK_Q);
        this.menuItemScrambleQueue.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, menuShortcutKey | KeyEvent.ALT_MASK));
        menuView.add(this.menuItemScrambleQueue);

        // menuItemHistory
        this.menuItemHistory = new JMenuItem(i18n("main.history"));
        this.menuItemHistory.setMnemonic(KeyEvent.VK_H);
        this.menuItemHistory.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, menuShortcutKey | KeyEvent.ALT_MASK));
        menuView.add(this.menuItemHistory);

        // menuItemSessionSummary
        this.menuItemSessionSummary = new JMenuItem(i18n("main.session_summary"));
        this.menuItemSessionSummary.setMnemonic(KeyEvent.VK_S);
        this.menuItemSessionSummary.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, menuShortcutKey | KeyEvent.ALT_MASK));
        menuView.add(this.menuItemSessionSummary);

        // menuItemStackmatDeveloper
        this.menuItemStackmatDeveloper = new JMenuItem(i18n("main.stackmat_developer"));
        this.menuItemStackmatDeveloper.setMnemonic(KeyEvent.VK_E);
        this.menuItemStackmatDeveloper.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, menuShortcutKey | KeyEvent.ALT_MASK));
        menuView.add(this.menuItemStackmatDeveloper);

        // menuCategory
        this.menuCategory = new JMenu(i18n("main.category"));
        this.menuCategory.setMnemonic(KeyEvent.VK_C);
        menuBar.add(this.menuCategory);

        // menuOptions
        JMenu menuOptions = new JMenu(i18n("main.options"));
        menuOptions.setMnemonic(KeyEvent.VK_O);
        menuBar.add(menuOptions);

        // menuColorScheme
        this.menuItemColorScheme = new JMenuItem(i18n("main.color_scheme"));
        this.menuItemColorScheme.setMnemonic(KeyEvent.VK_C);
        this.menuItemColorScheme.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, menuShortcutKey | KeyEvent.ALT_MASK));
        menuOptions.add(this.menuItemColorScheme);

        // menuItemInspectionTime
        this.menuItemInspectionTime = new JCheckBoxMenuItem(i18n("main.inspection_time"));
        this.menuItemInspectionTime.setMnemonic(KeyEvent.VK_I);
        this.menuItemInspectionTime.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, menuShortcutKey | KeyEvent.ALT_MASK));
        menuOptions.add(this.menuItemInspectionTime);

        // menuItemSmoothTiming
        this.menuItemSmoothTiming = new JCheckBoxMenuItem(i18n("main.smooth_timing"));
        this.menuItemSmoothTiming.setMnemonic(KeyEvent.VK_M);
        this.menuItemSmoothTiming.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, menuShortcutKey | KeyEvent.ALT_MASK));
        menuOptions.add(this.menuItemSmoothTiming);

        // menuTimerTrigger
        JMenu menuTimerTrigger = new JMenu(i18n("main.timer_trigger"));
        menuTimerTrigger.setMnemonic(KeyEvent.VK_T);
        menuOptions.add(menuTimerTrigger);
        ButtonGroup timerTriggerGroup = new ButtonGroup();

        // menuItemManualInput
        this.menuItemManualInput = new JRadioButtonMenuItem(i18n("main.manual_input"));
        this.menuItemManualInput.setMnemonic(KeyEvent.VK_N);
        this.menuItemManualInput.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, menuShortcutKey));
        menuTimerTrigger.add(this.menuItemManualInput);
        timerTriggerGroup.add(this.menuItemManualInput);

        // menuItemCtrlKeys
        this.menuItemCtrlKeys = new JRadioButtonMenuItem(i18n("main.ctrl_keys"));
        this.menuItemCtrlKeys.setMnemonic(KeyEvent.VK_C);
        this.menuItemCtrlKeys.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, menuShortcutKey));
        menuTimerTrigger.add(this.menuItemCtrlKeys);
        timerTriggerGroup.add(this.menuItemCtrlKeys);

        // menuItemSpaceKey
        this.menuItemSpaceKey = new JRadioButtonMenuItem(i18n("main.space_key"));
        this.menuItemSpaceKey.setMnemonic(KeyEvent.VK_S);
        this.menuItemSpaceKey.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, menuShortcutKey));
        menuTimerTrigger.add(this.menuItemSpaceKey);
        timerTriggerGroup.add(this.menuItemSpaceKey);

        // menuItemStackmatTimer
        this.menuItemStackmatTimer = new JRadioButtonMenuItem(i18n("main.stackmat_timer"));
        this.menuItemStackmatTimer.setMnemonic(KeyEvent.VK_T);
        this.menuItemStackmatTimer.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, menuShortcutKey));
        menuTimerTrigger.add(this.menuItemStackmatTimer);
        timerTriggerGroup.add(this.menuItemStackmatTimer);

        // menuStackmatTimerInputDevice
        this.stackmatTimerInputDevice = new JMenu(i18n("main.stackmat_timer_input_device"));
        menuTimerTrigger.setMnemonic(KeyEvent.VK_S);
        menuOptions.add(this.stackmatTimerInputDevice);
        this.stackmatTimerInputDeviceGroup = new ButtonGroup();

        // menuLookAndFeel
        this.menuLookAndFeel = new JMenu(i18n("main.look_and_feel"));
        this.menuLookAndFeel.setMnemonic(KeyEvent.VK_L);
        menuOptions.add(this.menuLookAndFeel);
        this.lookAndFeelGroup = new ButtonGroup();

        // menuItemDefaultLaF
        this.menuItemDefaultLnF = new JRadioButtonMenuItem(i18n("main.laf_system_default"));
        this.menuItemDefaultLnF.setMnemonic(KeyEvent.VK_D);
        this.menuItemDefaultLnF.setSelected(true);
        this.menuLookAndFeel.add(this.menuItemDefaultLnF);
        this.lookAndFeelGroup.add(this.menuItemDefaultLnF);

        //menuHelp
        JMenu menuHelp = new JMenu(i18n("main.help"));
        menuHelp.setMnemonic(KeyEvent.VK_H);
        menuBar.add(menuHelp);

        // menuItemFeedback
        this.menuItemFeedback = new JMenuItem(i18n("main.feedback"));
        this.menuItemFeedback.setMnemonic(KeyEvent.VK_F);
        menuHelp.add(this.menuItemFeedback);

        // menuItemAbout
        this.menuItemAbout = new JMenuItem(i18n("main.about"));
        this.menuItemAbout.setMnemonic(KeyEvent.VK_A);
        menuHelp.add(this.menuItemAbout);

        // panelMain
        JPanel panelMain = new JPanel(
                new MigLayout(
                        "fill, hidemode 1, insets 2 3 2 3",
                        "[fill]",
                        "[pref!][pref!][fill, growprio 200][pref!]"));
        panelMain.setPreferredSize(new Dimension(0, 0));
        add(panelMain);

        // labelMessage
        this.labelMessage = new JLabel();
        this.labelMessage.setPreferredSize(new Dimension());
        this.labelMessage.setOpaque(true);
        this.labelMessage.setHorizontalAlignment(JLabel.CENTER);
        this.labelMessage.setForeground(new Color(0xFF, 0xFF, 0xFF));
        this.labelMessage.setVisible(false);
        panelMain.add(this.labelMessage, "wrap");

        // panelScramble
        ScramblePanel scramblePanel = new ScramblePanel(this.scrambleManager);
        panelMain.add(scramblePanel, "wrap");

        // timer panel
        this.timerPanel = new TimerPanel(this.timerManager);
        panelMain.add(this.timerPanel, "wrap");

        // statistics panel
        StatisticsPanel statisticsPanel = new StatisticsPanel(this.sessionManager);
        statisticsPanel.setBorder(BorderFactory.createTitledBorder(i18n("main.session_statistics")));
        panelMain.add(statisticsPanel, "w 30%, growy, gapright 0, split 3");

        // times scroll pane
        TimesScrollPane timesScrollPane = new TimesScrollPane(this.solutionManager, this.sessionManager);
        timesScrollPane.setBorder(BorderFactory.createTitledBorder(i18n("main.times")));
        timesScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        panelMain.add(timesScrollPane, "w 40%, growy, gapright 0");

        // scramble viewer panel
        this.scrambleViewerPanel = new ScrambleViewerPanel(
                this.scramblerProvider,
                this.colorManager,
                this.scrambleManager);
        scrambleViewerPanel.setBorder(BorderFactory.createTitledBorder(i18n("main.scramble")));
        panelMain.add(scrambleViewerPanel, "w 30%, growy");

        scramblePanel.setScrambleViewerPanel(scrambleViewerPanel);

        Image icon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/com/puzzletimer/resources/icon.png"));

        // tips frame
        this.tipsFrame = new TipsFrame(
                this.puzzleProvider,
                this.tipProvider,
                this.scramblerProvider,
                this.categoryManager,
                this.scrambleManager);
        this.tipsFrame.setLocationRelativeTo(null);
        this.tipsFrame.setIconImage(icon);

        // scramble queue frame
        this.scrambleQueueFrame = new ScrambleQueueFrame(
                this.scrambleParserProvider,
                this.scramblerProvider,
                this.categoryManager,
                this.scrambleManager);
        this.scrambleQueueFrame.setLocationRelativeTo(null);
        this.scrambleQueueFrame.setIconImage(icon);

        // history frame
        this.historyFrame = new HistoryFrame(
                this.categoryManager,
                this.solutionManager,
                this.sessionManager);
        this.historyFrame.setLocationRelativeTo(null);
        this.historyFrame.setIconImage(icon);

        // session summary frame
        this.sessionSummaryFrame = new SessionSummaryFrame(
                this.categoryManager,
                this.sessionManager);
        this.sessionSummaryFrame.setLocationRelativeTo(null);
        this.sessionSummaryFrame.setIconImage(icon);

        // stackmat developer frame
        this.stackmatDeveloperFrame = new StackmatDeveloperFrame(this.timerManager);
        this.stackmatDeveloperFrame.setLocationRelativeTo(null);
        this.stackmatDeveloperFrame.setIconImage(icon);

        // category manager dialog
        this.categoryManagerDialog = new CategoryManagerFrame(
                this.puzzleProvider,
                this.scramblerProvider,
                this.categoryManager,
                this.tipProvider);
        this.categoryManagerDialog.setLocationRelativeTo(null);
        this.categoryManagerDialog.setIconImage(icon);

        // color scheme frame
        this.colorSchemeFrame = new ColorSchemeFrame(
                this.puzzleProvider,
                this.colorManager,
                this.scrambleViewerPanel);
        this.colorSchemeFrame.setLocationRelativeTo(null);
        this.colorSchemeFrame.setIconImage(icon);
    }
}
