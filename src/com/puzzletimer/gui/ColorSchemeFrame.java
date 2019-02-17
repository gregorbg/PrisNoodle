package com.puzzletimer.gui;

import com.puzzletimer.managers.ColorManager;
import com.puzzletimer.models.ColorScheme;
import com.puzzletimer.models.ColorScheme.FaceColor;
import com.puzzletimer.puzzles.Puzzle;
import com.puzzletimer.puzzles.PuzzleProvider;
import net.gnehzr.tnoodle.scrambles.InvalidScrambleException;
import net.miginfocom.swing.MigLayout;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.svg.SVGDocument;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.StringReader;

import static com.puzzletimer.Internationalization.i18n;

@SuppressWarnings("serial")
public class ColorSchemeFrame extends JFrame {
    private final SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(XMLResourceDescriptor.getXMLParserClassName());

    private JComboBox<Puzzle> comboBoxPuzzle;
    private JSVGCanvas panelSVG;
    private MainFrame.ScrambleViewerPanel scrambleViewerPanel;
    private JTable table;
    private JButton buttonEdit;
    private JButton buttonDefault;
    private JButton buttonOk;

    public ColorSchemeFrame(final PuzzleProvider puzzleProvider, final ColorManager colorManager, MainFrame.ScrambleViewerPanel scrambleViewerPanel) {
        super();

        this.scrambleViewerPanel = scrambleViewerPanel;

        setMinimumSize(new Dimension(480, 600));

        setTitle(i18n("color_scheme.color_scheme"));

        createComponents();
        pack();

        // combo box
        Puzzle defaultPuzzle = null;
        for (Puzzle puzzle : puzzleProvider.getAllNoEmpty()) {
            if (puzzle.getPuzzleId().equals(Puzzle.THREE)) {
                defaultPuzzle = puzzle;
            }

            this.comboBoxPuzzle.addItem(puzzle);
        }

        this.comboBoxPuzzle.addActionListener(event -> {
            Puzzle puzzle =
                (Puzzle) ColorSchemeFrame.this.comboBoxPuzzle.getSelectedItem();
            ColorScheme colorScheme =
                colorManager.getColorScheme(puzzle.getPuzzleId());

            update(puzzle, colorScheme);
        });
        this.comboBoxPuzzle.setSelectedItem(defaultPuzzle);

        // editing buttons
        this.buttonEdit.setEnabled(false);
        this.buttonDefault.setEnabled(false);
        this.table.getSelectionModel().addListSelectionListener(
                event -> {
                    int nSelected = ColorSchemeFrame.this.table.getSelectedRowCount();
                    ColorSchemeFrame.this.buttonEdit.setEnabled(nSelected == 1);
                    ColorSchemeFrame.this.buttonDefault.setEnabled(nSelected > 0);
                });

        this.buttonEdit.addActionListener(event -> {
            Puzzle puzzle =
                (Puzzle) ColorSchemeFrame.this.comboBoxPuzzle.getSelectedItem();
            ColorScheme colorScheme =
                colorManager.getColorScheme(puzzle.getPuzzleId());
            FaceColor faceColor =
                colorScheme.getFaceColors()[ColorSchemeFrame.this.table.getSelectedRow()];

            Color color = JColorChooser.showDialog(
                ColorSchemeFrame.this,
                String.format(i18n("color_scheme.face_color"), faceColor.getFaceDescription()),
                faceColor.getColor());
            if (color != null) {
                colorManager.setColorScheme(
                    colorScheme.setFaceColor(
                        faceColor.setColor(color)));
            }
        });

        this.buttonDefault.addActionListener(event -> {
            Puzzle puzzle =
                (Puzzle) ColorSchemeFrame.this.comboBoxPuzzle.getSelectedItem();
            ColorScheme colorScheme =
                colorManager.getColorScheme(puzzle.getPuzzleId());

            for (int index : ColorSchemeFrame.this.table.getSelectedRows()) {
                FaceColor faceColor = colorScheme.getFaceColors()[index];
                colorScheme = colorScheme.setFaceColor(faceColor.setColorToDefault());
            }

            colorManager.setColorScheme(colorScheme);
        });

        // ok button
        this.setDefaultCloseOperation(HIDE_ON_CLOSE);
        this.buttonOk.addActionListener(event -> ColorSchemeFrame.this.setVisible(false));

        // update on colors updated events
        colorManager.addListener(new ColorManager.Listener() {
            @Override
            public void colorSchemeUpdated(ColorScheme colorScheme) {
                Puzzle puzzle =
                    (Puzzle) ColorSchemeFrame.this.comboBoxPuzzle.getSelectedItem();

                if (puzzle.getPuzzleId().equals(colorScheme.getPuzzleId())) {
                    update(puzzle, colorScheme);
                    try {
                        scrambleViewerPanel.redrawScramble();
                    } catch (InvalidScrambleException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // esc key closes window
        this.getRootPane().registerKeyboardAction(
                arg0 -> ColorSchemeFrame.this.setVisible(false),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void createComponents() {
        setLayout(
            new MigLayout(
                "fill",
                "[grow][pref!]",
                "[pref!][pref!][pref!]12[pref!][]16[pref!]"));

        // labelPuzzle
        add(new JLabel(i18n("color_scheme.puzzle")), "growx, span, wrap");

        // comboBoxPuzzle
        this.comboBoxPuzzle = new JComboBox<>();
        add(this.comboBoxPuzzle, "growx, span, wrap");

        // panel3D
        this.panelSVG = new JSVGCanvas();
        this.panelSVG.setMinimumSize(new Dimension(300, 300));
        this.panelSVG.setPreferredSize(this.panelSVG.getMinimumSize());
        this.panelSVG.setBackground(getBackground());
        add(this.panelSVG, "growx, span, wrap");

        // labelColors
        add(new JLabel(i18n("color_scheme.colors")), "growx, span, wrap");

        // table
        this.table = new JTable();
        this.table.setShowVerticalLines(false);

        JScrollPane scrollPane = new JScrollPane(this.table);
        this.table.setFillsViewportHeight(true);
        scrollPane.setPreferredSize(new Dimension(0, 0));
        add(scrollPane, "grow");

        // buttonEdit
        this.buttonEdit = new JButton(i18n("color_scheme.edit"));
        add(this.buttonEdit, "growx, top, split, flowy");

        // buttonDefault
        this.buttonDefault = new JButton(i18n("color_scheme.default"));
        add(this.buttonDefault, "growx, top, wrap");

        // buttonOk
        this.buttonOk = new JButton(i18n("color_scheme.ok"));
        add(this.buttonOk, "tag ok, span");
    }

    private class ColorRenderer extends JLabel implements TableCellRenderer {
        public ColorRenderer() {
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object color, boolean isSelected, boolean hasFocus, int row, int column) {
            // foreground
            setBackground((Color) color);

            // background
            Color backgroundColor = isSelected ? table.getSelectionBackground() : table.getBackground();
            setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, backgroundColor));

            return this;
        }
    }

    private void update(Puzzle puzzle, ColorScheme colorScheme) {
        try {
            StringReader reader = new StringReader(puzzle.getScrambleSVG("", colorScheme));
            SVGDocument doc = f.createSVGDocument("", reader);
            panelSVG.setSVGDocument(doc);
        } catch (IOException | InvalidScrambleException e) {
            e.printStackTrace();
        }

        // color table
        this.table.setDefaultRenderer(Color.class, new ColorRenderer());

        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override
            public Class<?> getColumnClass(int c) {
                return getValueAt(0, c).getClass();
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableModel.addColumn(i18n("color_scheme.face"));
        tableModel.addColumn(i18n("color_scheme.color"));

        for (FaceColor faceColor : colorScheme.getFaceColors()) {
            tableModel.addRow(new Object[] {
                faceColor.getFaceDescription(),
                faceColor.getColor(),
            });
        }

        this.table.setModel(tableModel);
    }
}