package com.puzzletimer;

import com.puzzletimer.categories.Category;
import com.puzzletimer.categories.CategoryProvider;
import com.puzzletimer.database.*;
import com.puzzletimer.gui.MainFrame;
import com.puzzletimer.managers.*;
import com.puzzletimer.managers.MessageManager.MessageType;
import com.puzzletimer.models.ColorScheme;
import com.puzzletimer.models.ConfigurationEntry;
import com.puzzletimer.models.Solution;
import com.puzzletimer.models.Timing;
import com.puzzletimer.parsers.ScrambleParserProvider;
import com.puzzletimer.puzzles.PuzzleProvider;
import com.puzzletimer.scramblers.ScramblerProvider;
import com.puzzletimer.statistics.Best;
import com.puzzletimer.statistics.BestAverage;
import com.puzzletimer.statistics.BestMean;
import com.puzzletimer.statistics.StatisticalMeasure;
import com.puzzletimer.timer.Timer;
import com.puzzletimer.tips.TipProvider;
import org.h2.tools.RunScript;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;

import static com.puzzletimer.Internationalization.i18n;

public class Main {
    private ConfigurationDAO configurationDAO;
    private SolutionDAO solutionDAO;
    private ColorDAO colorDAO;
    private CategoryDAO categoryDAO;

    private MessageManager messageManager;
    private ConfigurationManager configurationManager;
    private TimerManager timerManager;
    private PuzzleProvider puzzleProvider;
    private ScrambleParserProvider scrambleParserProvider;
    private ScramblerProvider scramblerProvider;
    private CategoryManager categoryManager;
    private ColorManager colorManager;
    private TipProvider tipProvider;
    private ScrambleManager scrambleManager;
    private SolutionManager solutionManager;
    private SessionManager sessionManager;

    public Main() {
        // create initial database if necessary
        File databaseFile = new File("puzzletimerWCA.mv.db");
        if (!databaseFile.exists()) {
            try {
                Connection connection = DriverManager.getConnection("jdbc:h2:./puzzletimerWCA");
                Reader script = new InputStreamReader(
                        getClass().getResourceAsStream("/com/puzzletimer/resources/database/puzzletimerWCA.sql"));
                RunScript.execute(connection, script);
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
                JFrame frame = new JFrame();
                JOptionPane.showMessageDialog(
                        frame,
                        String.format(i18n("main.database_error_message"), e.getMessage()),
                        i18n("main.prisma_puzzle_timer"),
                        JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
        }

        // connect to database
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:h2:./puzzletimerWCA;IFEXISTS=TRUE");
        } catch (SQLException e) {
            e.printStackTrace();
            JFrame frame = new JFrame();
            JOptionPane.showMessageDialog(
                    frame,
                    i18n("main.concurrent_database_access_error_message"),
                    i18n("main.prisma_puzzle_timer"),
                    JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

        // message manager
        this.messageManager = new MessageManager();

        // puzzle provider
        this.puzzleProvider = new PuzzleProvider();

        // color DAO
        this.colorDAO = new ColorDAO(connection);

        // color manager
        this.colorManager = new ColorManager(this.colorDAO.getAll());
        this.colorManager.loadTNoodleDefaults(this.puzzleProvider);
        this.colorManager.addListener(new ColorManager.Listener() {
            @Override
            public void colorSchemeUpdated(ColorScheme colorScheme) {
                try {
                    Main.this.colorDAO.update(colorScheme);
                } catch (DatabaseException e) {
                    Main.this.messageManager.enqueueMessage(
                            MessageType.ERROR,
                            String.format(i18n("main.database_error_message"), e.getMessage()));
                }
            }
        });

        // configuration DAO
        this.configurationDAO = new ConfigurationDAO(connection);

        // configuration manager
        this.configurationManager = new ConfigurationManager(this.configurationDAO.getAll());
        this.configurationManager.addListener(new ConfigurationManager.Listener() {
            @Override
            public void configurationEntryUpdated(String key, String value) {
                try {
                    Main.this.configurationDAO.update(new ConfigurationEntry(key, value));
                } catch (DatabaseException e) {
                    Main.this.messageManager.enqueueMessage(
                            MessageType.ERROR,
                            String.format(i18n("main.database_error_message"), e.getMessage()));
                }
            }
        });

        // tip provider
        this.tipProvider = new TipProvider();

        // scramble parser provider
        this.scrambleParserProvider = new ScrambleParserProvider();

        // scrambler provider
        this.scramblerProvider = new ScramblerProvider();

        this.categoryDAO = new CategoryDAO(connection);

        // category provider
        CategoryProvider categoryProvider = new CategoryProvider();
        categoryProvider.loadCustom(categoryDAO.getAll());

        UUID currentCategoryId = UUID.fromString(
                this.configurationManager.getConfiguration("CURRENT-CATEGORY"));
        Category currentCategory = null;
        for (Category category : categoryProvider.getAll())
            if (category.getCategoryId().equals(currentCategoryId)) currentCategory = category;

        this.categoryManager = new CategoryManager(categoryProvider, currentCategory);
        this.categoryManager.addListener(new CategoryManager.Listener() {
            @Override
            public void currentCategoryChanged(Category oldCategory, Category newCategory) {
                Main.this.configurationManager.setConfiguration("CURRENT-CATEGORY", newCategory.getCategoryId().toString());

                Main.this.timerManager.getTimer().setInspectionEnabled(Main.this.timerManager.isInspectionEnabled() && !newCategory.isBldMode());

                try {
                    Main.this.solutionManager.loadSolutions(
                            Main.this.solutionDAO.getAll(newCategory));
                    Main.this.sessionManager.clearSession(!oldCategory.equals(newCategory));
                } catch (DatabaseException e) {
                    Main.this.messageManager.enqueueMessage(
                            MessageType.ERROR,
                            String.format(i18n("main.database_error_message"), e.getMessage()));
                }
            }

            @Override
            public void categoryAdded(Category category) {
                try {
                    Main.this.categoryDAO.insert(category);
                } catch (DatabaseException e) {
                    Main.this.messageManager.enqueueMessage(
                            MessageType.ERROR,
                            String.format(i18n("main.database_error_message"), e.getMessage()));
                }
            }

            @Override
            public void categoryRemoved(Category category) {
                try {
                    Main.this.categoryDAO.delete(category);
                } catch (DatabaseException e) {
                    Main.this.messageManager.enqueueMessage(
                            MessageType.ERROR,
                            String.format(i18n("main.database_error_message"), e.getMessage()));
                }
            }

            @Override
            public void categoryUpdated(Category category) {
                try {
                    Main.this.categoryDAO.update(category);
                } catch (DatabaseException e) {
                    Main.this.messageManager.enqueueMessage(
                            MessageType.ERROR,
                            String.format(i18n("main.database_error_message"), e.getMessage()));
                }
            }
        });

        // timer manager
        this.timerManager = new TimerManager();
        this.timerManager.setInspectionEnabled(this.configurationManager.getBooleanConfiguration("INSPECTION-TIME-ENABLED"), this.categoryManager.getCurrentCategory().isBldMode());
        this.timerManager.setSmoothTimingEnabled(this.configurationManager.getBooleanConfiguration("SMOOTH-TIMING-ENABLED"));
        this.timerManager.addListener(new TimerManager.Listener() {
            @Override
            public void solutionFinished(Timing timing, String penalty) {
                // add solution
                Main.this.solutionManager.addSolution(
                        new Solution(
                                UUID.randomUUID(),
                                Main.this.categoryManager.getCurrentCategory().getCategoryId(),
                                Main.this.sessionManager.getSessionID(),
                                Main.this.scrambleManager.getCurrentScramble(),
                                timing,
                                penalty));

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

                Solution[] solutions = Main.this.solutionManager.getSolutions();
                Solution[] sessionSolutions = Main.this.sessionManager.getSolutions();

                for (int i = 0; i < measures.length; i++) {
                    if (sessionSolutions.length < measures[i].getMinimumWindowSize()) continue;

                    measures[i].setSolutions(solutions);
                    long allTimeBest = measures[i].getValue();

                    measures[i].setSolutions(sessionSolutions);
                    long sessionBest = measures[i].getValue();

                    if (measures[i].getWindowPosition() == 0 && sessionBest <= allTimeBest) {
                        Main.this.messageManager.enqueueMessage(
                                MessageType.INFORMATION,
                                String.format(i18n("main.personal_record_message"),
                                        Main.this.categoryManager.getCurrentCategory().getDescription(),
                                        measures[i].toFormatString(),
                                        descriptions[i]));
                    }
                }

                // generate next scramble
                Main.this.scrambleManager.changeScramble();
            }

            @Override
            public void timerChanged(Timer timer) {
                Main.this.configurationManager.setConfiguration(
                        "TIMER-TRIGGER", timer.getTimerId());
            }

            @Override
            public void inspectionEnabledSet(boolean inspectionEnabled) {
                Main.this.configurationManager.setBooleanConfiguration(
                        "INSPECTION-TIME-ENABLED", inspectionEnabled);
            }

            @Override
            public void smoothTimingSet(boolean smoothTimingEnabled) {
                Main.this.configurationManager.setBooleanConfiguration(
                        "SMOOTH-TIMING-ENABLED", smoothTimingEnabled);
            }
        });

        // scramble manager
        this.scrambleManager = new ScrambleManager(this.scramblerProvider,
                this.scramblerProvider.get(currentCategory == null ? "EMPTY" : currentCategory.getScramblerId()));
        this.categoryManager.addListener(new CategoryManager.Listener() {
            @Override
            public void currentCategoryChanged(Category oldCategory, Category newCategory) {
                Main.this.scrambleManager.setCategory(newCategory);
            }
        });

        // solution DAO
        this.solutionDAO = new SolutionDAO(connection, this.scramblerProvider, this.scrambleParserProvider);

        // solution manager
        this.solutionManager = new SolutionManager();
        this.solutionManager.addListener(new SolutionManager.Listener() {
            @Override
            public void solutionAdded(Solution solution) {
                Main.this.sessionManager.addSolution(solution);

                try {
                    Main.this.solutionDAO.insert(solution);
                } catch (DatabaseException e) {
                    Main.this.messageManager.enqueueMessage(MessageType.ERROR,
                            String.format(i18n("main.database_error_message"), e.getMessage()));
                }
            }

            @Override
            public void solutionUpdated(Solution solution) {
                Main.this.sessionManager.updateSolution(solution);

                try {
                    Main.this.solutionDAO.update(solution);
                } catch (DatabaseException e) {
                    Main.this.messageManager.enqueueMessage(MessageType.ERROR,
                            String.format(i18n("main.database_error_message"), e.getMessage()));
                }
            }

            @Override
            public void solutionRemoved(Solution solution) {
                Main.this.sessionManager.removeSolution(solution);

                try {
                    Main.this.solutionDAO.delete(solution);
                } catch (DatabaseException e) {
                    Main.this.messageManager.enqueueMessage(MessageType.ERROR,
                            String.format(i18n("main.database_error_message"), e.getMessage()));
                }
            }
        });

        // session manager
        this.sessionManager = new SessionManager();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Main main = new Main();

                String laf = main.configurationManager.getConfiguration("LOOK-AND-FEEL");
                if (laf == null)
                    laf = UIManager.getSystemLookAndFeelClassName();
                try {
                    UIManager.setLookAndFeel(laf);
                } catch (Exception ignored){
                }

                Image icon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/com/puzzletimer/resources/icon.png"));

                // main frame
                MainFrame mainFrame = new MainFrame(
                        main.messageManager,
                        main.configurationManager,
                        main.timerManager,
                        main.puzzleProvider,
                        main.colorManager,
                        main.scrambleParserProvider,
                        main.scramblerProvider,
                        main.tipProvider,
                        main.categoryManager,
                        main.scrambleManager,
                        main.solutionManager,
                        main.sessionManager);
                mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                mainFrame.setLocationRelativeTo(null);
                mainFrame.setIconImage(icon);

                Category current = main.categoryManager.getCurrentCategory();
                main.categoryManager.setCurrentCategory(current);

                mainFrame.setVisible(true);
            }
        });
    }
}
