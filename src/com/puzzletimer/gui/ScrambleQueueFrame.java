package com.puzzletimer.gui;

import com.puzzletimer.categories.Category;
import com.puzzletimer.managers.CategoryManager;
import com.puzzletimer.managers.ScrambleManager;
import com.puzzletimer.models.Scramble;
import com.puzzletimer.parsers.ScrambleParser;
import com.puzzletimer.parsers.ScrambleParserProvider;
import com.puzzletimer.scramblers.Scrambler;
import com.puzzletimer.scramblers.ScramblerProvider;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

import static com.puzzletimer.Internationalization.i18n;

@SuppressWarnings("serial")
public class ScrambleQueueFrame extends JFrame {
    private JTable table;
    private JButton buttonUp;
    private JButton buttonDown;
    private JButton buttonAdd;
    private JButton buttonRemove;
    private JButton buttonImportFromFile;
    private JButton buttonExport;
    private JComboBox<Scrambler> comboBoxScrambler;
    private JSpinner spinnerNumberOfScrambles;
    private JButton buttonImportFromScrambler;
    private JButton buttonOk;

    public ScrambleQueueFrame(
            final ScrambleParserProvider scrambleParserProvider,
            final ScramblerProvider scramblerProvider,
            final CategoryManager categoryManager,
            final ScrambleManager scrambleManager) {
        super();

        setMinimumSize(new Dimension(640, 480));

        createComponents();
        pack();

        // on category change
        categoryManager.addListener(new CategoryManager.Listener() {
            @Override
            public void categoriesUpdated(Category[] categories, Category currentCategory) {
                // title
                setTitle(
                        String.format(
                                i18n("scramble_queue.scramble_queue-category"),
                                currentCategory.getDescription()));

                // scrambler combobox
                ScrambleQueueFrame.this.comboBoxScrambler.removeAllItems();

                Scrambler currentScrambler = scramblerProvider.get(
                        categoryManager.getCurrentCategory().getScramblerId());
                String puzzleId = currentScrambler.getPuzzle().getPuzzleId();

                for (Scrambler scrambler : scramblerProvider.getAll())
                    if (scrambler.getPuzzle().getPuzzleId().equals(puzzleId))
                        ScrambleQueueFrame.this.comboBoxScrambler.addItem(scrambler);
            }
        });
        categoryManager.notifyListeners();

        // on queue update
        scrambleManager.addListener(new ScrambleManager.Listener() {
            @Override
            public void scrambleQueueUpdated(Scramble[] queue) {
                updateTable(queue);
                updateButtons(ScrambleQueueFrame.this.table);
            }
        });

        // enable/disable buttons
        this.table.getSelectionModel().addListSelectionListener(
                event -> updateButtons(ScrambleQueueFrame.this.table));

        // up button
        this.buttonUp.addActionListener(event -> {
            JTable table1 = ScrambleQueueFrame.this.table;
            int[] selectedRows = table1.getSelectedRows();

            // move scrambles
            scrambleManager.moveScramblesUp(selectedRows);

            // fix selection
            table1.removeRowSelectionInterval(0, selectedRows.length - 1);
            for (int selectedRow : selectedRows) table1.addRowSelectionInterval(selectedRow - 1, selectedRow - 1);

            // request focus
            ScrambleQueueFrame.this.buttonUp.requestFocusInWindow();
        });

        // down button
        this.buttonDown.addActionListener(event -> {
            JTable table1 = ScrambleQueueFrame.this.table;
            int[] selectedRows = table1.getSelectedRows();

            // move scrambles
            scrambleManager.moveScramblesDown(selectedRows);

            // fix selection
            table1.removeRowSelectionInterval(0, selectedRows.length - 1);
            for (int selectedRow : selectedRows) table1.addRowSelectionInterval(selectedRow + 1, selectedRow + 1);

            // request focus
            ScrambleQueueFrame.this.buttonDown.requestFocusInWindow();
        });

        // add button
        this.buttonAdd.addActionListener(event -> {
            String scramble = JOptionPane.showInputDialog(ScrambleQueueFrame.this, i18n("scramble_queue.scramble_manual_input"));

            if (scramble == null || scramble.length() == 0) return;

            Category category = categoryManager.getCurrentCategory();
            Scrambler scrambler = scramblerProvider.get(category.getScramblerId());
            String puzzleId = scrambler.getPuzzle().getPuzzleId();
            ScrambleParser scrambleParser = scrambleParserProvider.get(puzzleId);

            String[] manualSequence = scrambleParser.parse(scramble);
            if (manualSequence.length > 0) {
                Scramble manual = new Scramble(category.getScramblerId(), manualSequence);
                scrambleManager.addScrambles(new Scramble[]{manual}, false);
            } else {
                JOptionPane.showMessageDialog(ScrambleQueueFrame.this, i18n("scramble_queue.manual_input_error"), i18n("scramble_queue.error"), JOptionPane.ERROR_MESSAGE);
            }

            ScrambleQueueFrame.this.buttonAdd.requestFocusInWindow();
        });

        // remove button
        this.buttonRemove.addActionListener(event -> {
            // remove scrambles
            scrambleManager.removeScrambles(
                    ScrambleQueueFrame.this.table.getSelectedRows());

            // request focus
            ScrambleQueueFrame.this.buttonRemove.requestFocusInWindow();
        });

        // import from file
        this.buttonImportFromFile.addActionListener(event -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter(i18n("scramble_queue.scramble_file_description"), "txt"));
            int action = fileChooser.showOpenDialog(ScrambleQueueFrame.this);
            if (action != JFileChooser.APPROVE_OPTION) return;

            Category category = categoryManager.getCurrentCategory();
            Scrambler scrambler = scramblerProvider.get(category.getScramblerId());
            String puzzleId = scrambler.getPuzzle().getPuzzleId();
            ScrambleParser scrambleParser = scrambleParserProvider.get(puzzleId);

            Scramble[] scrambles;
            try {
                scrambles = loadScramblesFromFile(
                        fileChooser.getSelectedFile(),
                        puzzleId + "-IMPORTER",
                        scrambleParser);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(
                        ScrambleQueueFrame.this,
                        String.format(
                                i18n("scramble_queue.file_opening_error"),
                                fileChooser.getSelectedFile().getAbsolutePath()),
                        i18n("scramble_queue.error"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            scrambleManager.addScrambles(scrambles, false);
        });

        // export to file
        this.buttonExport.addActionListener(event -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File(i18n("scramble_queue.default_file_name")));
            fileChooser.setFileFilter(new FileNameExtensionFilter(i18n("scramble_queue.scramble_file_description"), "txt"));
            int action = fileChooser.showSaveDialog(ScrambleQueueFrame.this);
            if (action != JFileChooser.APPROVE_OPTION) return;

            Scramble[] scrambles = scrambleManager.getQueue();
            Scramble[] selectedScrambles;

            int[] selectedRows = ScrambleQueueFrame.this.table.getSelectedRows();
            if (selectedRows.length <= 0) selectedScrambles = scrambles;
            else {
                selectedScrambles = new Scramble[selectedRows.length];
                for (int i = 0; i < selectedScrambles.length; i++) selectedScrambles[i] = scrambles[selectedRows[i]];
            }

            try {
                saveScramblesToFile(
                        selectedScrambles,
                        fileChooser.getSelectedFile());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(
                        ScrambleQueueFrame.this,
                        String.format(
                                i18n("scramble_queue.file_opening_error"),
                                fileChooser.getSelectedFile().getAbsolutePath()),
                        i18n("scramble_queue.error"),
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // import from scrambler
        this.buttonImportFromScrambler.addActionListener(event -> {
            Scrambler scrambler =
                    (Scrambler) ScrambleQueueFrame.this.comboBoxScrambler.getSelectedItem();

            Scramble[] scrambles = new Scramble[
                    (Integer) ScrambleQueueFrame.this.spinnerNumberOfScrambles.getValue()];
            for (int i = 0; i < scrambles.length; i++) scrambles[i] = scrambler.getNextScramble();

            scrambleManager.addScrambles(scrambles, false);
        });

        // ok button
        this.setDefaultCloseOperation(HIDE_ON_CLOSE);
        this.buttonOk.addActionListener(event -> ScrambleQueueFrame.this.setVisible(false));

        // esc key closes window
        this.getRootPane().registerKeyboardAction(
                arg0 -> ScrambleQueueFrame.this.setVisible(false),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void createComponents() {
        setLayout(
                new MigLayout(
                        "fill",
                        "[fill][pref!]",
                        "[pref!][]12[pref!][pref!]16[pref!]"));

        // labelQueue
        add(new JLabel(i18n("scramble_queue.queue")), "span, wrap");

        // table
        this.table = new JTable();
        this.table.setShowVerticalLines(false);

        JScrollPane scrollPane = new JScrollPane(this.table);
        this.table.setFillsViewportHeight(true);
        scrollPane.setPreferredSize(new Dimension(0, 0));
        add(scrollPane, "grow");

        // buttonUp
        this.buttonUp = new JButton(i18n("scramble_queue.up"));
        this.buttonUp.setEnabled(false);
        add(this.buttonUp, "top, growx, split 6, flowy");

        // buttonDown
        this.buttonDown = new JButton(i18n("scramble_queue.down"));
        this.buttonDown.setEnabled(false);
        add(this.buttonDown, "top, growx");

        // buttonAdd
        this.buttonAdd = new JButton(i18n("scramble_queue.add"));
        add(this.buttonAdd, "top, growx");

        // buttonRemove
        this.buttonRemove = new JButton(i18n("scramble_queue.remove"));
        this.buttonRemove.setEnabled(false);
        add(this.buttonRemove, "top, growx");

        // buttonImportFromFile
        this.buttonImportFromFile = new JButton(i18n("scramble_queue.import_from_file"));
        add(this.buttonImportFromFile, "gaptop 20, top, growx");

        // buttonExport
        this.buttonExport = new JButton(i18n("scramble_queue.export_to_file"));
        this.buttonExport.setEnabled(false);
        add(this.buttonExport, "top, growx, wrap");

        // labelImportFromScrambler
        add(new JLabel(i18n("scramble_queue.import_from_scrambler")), "span, wrap");

        // comboBoxScrambler
        this.comboBoxScrambler = new JComboBox<>();
        add(this.comboBoxScrambler, "growx, span, split 3");

        // spinnerNumberOfScrambles
        this.spinnerNumberOfScrambles = new JSpinner(new SpinnerNumberModel(12, 1, 1000, 1));
        add(this.spinnerNumberOfScrambles, "");

        // buttonImportFromScrambler
        this.buttonImportFromScrambler = new JButton(i18n("scramble_queue.import"));
        add(this.buttonImportFromScrambler, "wrap");

        // buttonOK
        this.buttonOk = new JButton(i18n("scramble_queue.ok"));
        add(this.buttonOk, "tag ok, span");
    }

    private void updateTable(Scramble[] queue) {
        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableModel.addColumn(i18n("scramble_queue.#"));
        tableModel.addColumn(i18n("scramble_queue.scramble"));

        this.table.setModel(tableModel);

        this.table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        TableColumn indexColumn = this.table.getColumnModel().getColumn(0);
        indexColumn.setPreferredWidth(50);

        TableColumn scrambleColumn = this.table.getColumnModel().getColumn(1);
        scrambleColumn.setPreferredWidth(1000);

        for (int i = 0; i < queue.length; i++)
            tableModel.addRow(new Object[]{
                    i + 1,
                    queue[i].getRawSequence(),
            });
    }

    private void updateButtons(JTable table) {
        int[] selectedRows = table.getSelectedRows();
        int nRows = table.getRowCount();

        // up button
        this.buttonUp.setEnabled(
                selectedRows.length > 0 &&
                        selectedRows[0] != 0);

        // down button
        this.buttonDown.setEnabled(
                selectedRows.length > 0 &&
                        selectedRows[selectedRows.length - 1] != nRows - 1);

        // add button
        this.buttonAdd.setEnabled(selectedRows.length == 0);

        // remove button
        this.buttonRemove.setEnabled(selectedRows.length > 0);

        // export button
        this.buttonExport.setEnabled(nRows > 0);
    }

    private Scramble[] loadScramblesFromFile(File file, String scramblerId, ScrambleParser scrambleParser) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        Scanner scanner = new Scanner(fileInputStream, "UTF-8");

        ArrayList<Scramble> scrambles = new ArrayList<>();
        while (scanner.hasNextLine()) scrambles.add(new Scramble(
                scramblerId,
                scrambleParser.parse(scanner.nextLine().trim())));

        scanner.close();

        Scramble[] scrambleArray = new Scramble[scrambles.size()];
        scrambles.toArray(scrambleArray);
        return scrambleArray;
    }

    private void saveScramblesToFile(Scramble[] scrambles, File file) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        OutputStreamWriter writer = new OutputStreamWriter(fileOutputStream, "UTF-8");

        for (Scramble scramble : scrambles) {
            writer.append(scramble.getRawSequence());
            writer.append("\r\n");
        }

        writer.close();
    }
}
