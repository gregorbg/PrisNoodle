package com.puzzletimer.gui;

import com.puzzletimer.categories.Category;
import com.puzzletimer.puzzles.Puzzle;
import com.puzzletimer.scramblers.Scrambler;
import com.puzzletimer.tips.Tip;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

import static com.puzzletimer.Internationalization.i18n;

@SuppressWarnings("serial")
public class CategoryEditorDialog extends JDialog {
    private JTextField textFieldDescription;
    private JComboBox<Puzzle> comboBoxPuzzle;
    private JComboBox<Scrambler> comboBoxScrambler;
    private JCheckBox checkBoxBldMode;
    private JSpinner spinnerPhases;
    private JComboBox<Tip> comboBoxTips;
    private JButton buttonAdd;
    private JList<Tip> listTips;
    private JButton buttonUp;
    private JButton buttonDown;
    private JButton buttonRemove;
    private JButton buttonOk;
    private JButton buttonCancel;

    public CategoryEditorDialog(
            JFrame owner,
            boolean modal,
            final Scrambler[] scramblers,
            final Puzzle[] puzzles,
            final Tip[] tips,
            final Category category,
            boolean isEditable,
            final CategoryManagerFrame.CategoryEditorListener listener) {
        super(owner, modal);

        setTitle(i18n("category_editor.category_editor"));
        setMinimumSize(new Dimension(480, 300));

        createComponents();
        pack();

        // set category description
        this.textFieldDescription.setText(category.getDescription());

        // fill puzzles combo box
        for (Puzzle puzzle : puzzles) {
            this.comboBoxPuzzle.addItem(puzzle);
        }

        this.checkBoxBldMode.setSelected(category.isForceStart());

        this.spinnerPhases.setValue(category.getPhases());

        // fill combo boxes on puzzle selection
        this.comboBoxPuzzle.addActionListener(event -> {
            Puzzle selectedPuzzle = (Puzzle) CategoryEditorDialog.this.comboBoxPuzzle.getSelectedItem();

            // scramblers
            CategoryEditorDialog.this.comboBoxScrambler.removeAllItems();
            for (Scrambler scrambler : scramblers) {
                if (scrambler.getPuzzle().getPuzzleId().equals(selectedPuzzle.getPuzzleId())) {
                    CategoryEditorDialog.this.comboBoxScrambler.addItem(scrambler);
                }
            }

            // tips
            CategoryEditorDialog.this.comboBoxTips.removeAllItems();
            for (Tip tip : tips) {
                if (tip.getPuzzleId().equals(selectedPuzzle.getPuzzleId())) {
                    CategoryEditorDialog.this.comboBoxTips.addItem(tip);
                }
            }

            // selected tips
            DefaultListModel<Tip> listModel = (DefaultListModel<Tip>) CategoryEditorDialog.this.listTips.getModel();
            listModel.removeAllElements();
            for (String categoryTipId : category.getTipIds()) {
                for (Tip tip : tips) {
                    if (categoryTipId.equals(tip.getTipId())) {
                        listModel.addElement(tip);
                        break;
                    }
                }
            }
        });

        // set add button behavior
        this.buttonAdd.addActionListener(event -> {
            Tip selectedTip = (Tip) CategoryEditorDialog.this.comboBoxTips.getSelectedItem();
            if (selectedTip == null) {
                return;
            }

            DefaultListModel<Tip> listModel = (DefaultListModel<Tip>) CategoryEditorDialog.this.listTips.getModel();
            if (!listModel.contains(selectedTip)) {
                listModel.addElement(selectedTip);
            }
        });

        // set up button behavior
        this.buttonUp.addActionListener(event -> {
            JList<Tip> listTips1 = CategoryEditorDialog.this.listTips;

            DefaultListModel<Tip> model = (DefaultListModel<Tip>) listTips1.getModel();

            int selectedIndex = listTips1.getSelectedIndex();
            if (selectedIndex > 0) {
                // swap
                Tip selectedValue = model.getElementAt(selectedIndex);
                model.insertElementAt(selectedValue, selectedIndex - 1);
                model.removeElementAt(selectedIndex + 1);

                // fix selection
                listTips1.addSelectionInterval(selectedIndex - 1, selectedIndex - 1);
            }
        });

        // set down button behavior
        this.buttonDown.addActionListener(event -> {
            JList<Tip> listTips1 = CategoryEditorDialog.this.listTips;

            DefaultListModel<Tip> model = (DefaultListModel<Tip>) listTips1.getModel();

            int selectedIndex = listTips1.getSelectedIndex();
            if (selectedIndex >= 0 && selectedIndex < model.getSize() - 1) {
                // swap
                Tip selectedValue = model.getElementAt(selectedIndex);
                model.insertElementAt(selectedValue, selectedIndex + 2);
                model.removeElementAt(selectedIndex);

                // fix selection
                listTips1.addSelectionInterval(selectedIndex + 1, selectedIndex + 1);
            }
        });

        // set remove button behavior
        this.buttonRemove.addActionListener(event -> {
            DefaultListModel model = (DefaultListModel) CategoryEditorDialog.this.listTips.getModel();

            int selectedIndex = CategoryEditorDialog.this.listTips.getSelectedIndex();
            if (selectedIndex >= 0) {
                model.removeElementAt(selectedIndex);
            }
        });

        // set puzzle/scrambler combo boxes editable
        this.comboBoxPuzzle.setEnabled(isEditable);
        this.comboBoxScrambler.setEnabled(isEditable);

        // set ok button behavior
        this.buttonOk.addActionListener(event -> {
            // scrambler
            String scramblerId =
                    ((Scrambler) CategoryEditorDialog.this.comboBoxScrambler.getSelectedItem()).getScramblerId();

            // description
            String description =
                    CategoryEditorDialog.this.textFieldDescription.getText();

            // bld mode
            boolean isBldMode = CategoryEditorDialog.this.checkBoxBldMode.isSelected();

            int phases = (Integer) CategoryEditorDialog.this.spinnerPhases.getValue();

            // tip ids
            ListModel listModel = CategoryEditorDialog.this.listTips.getModel();

            String[] tipIds = new String[listModel.getSize()];
            for (int i = 0; i < tipIds.length; i++) {
                tipIds[i] = ((Tip) listModel.getElementAt(i)).getTipId();
            }

            listener.categoryEdited(
                    category
                            .setScramblerId(scramblerId)
                            .setDescription(description)
                            .setTipIds(tipIds)
                            .setForceStart(isBldMode)
							.setPhases(phases));

            dispose();
        });

        // set cancel button behavior
        this.buttonCancel.addActionListener(event -> dispose());

        // select puzzle
        Scrambler categoryScrambler = null;
        for (Scrambler scrambler : scramblers) {
            if (scrambler.getScramblerId().equals(category.getScramblerId())) {
                categoryScrambler = scrambler;
                break;
            }
        }

        for (int i = 0; i < this.comboBoxPuzzle.getItemCount(); i++) {
            Puzzle puzzle = this.comboBoxPuzzle.getItemAt(i);
            if (puzzle.getPuzzleId().equals(categoryScrambler == null ? "EMPTY" : categoryScrambler.getPuzzle().getPuzzleId())) {
                this.comboBoxPuzzle.setSelectedIndex(i);
                break;
            }
        }

        // select scrambler
        for (int i = 0; i < this.comboBoxScrambler.getItemCount(); i++) {
            Scrambler scrambler = this.comboBoxScrambler.getItemAt(i);
            if (scrambler.getScramblerId().equals(categoryScrambler == null ? "EMPTY" : categoryScrambler.getPuzzle().getPuzzleId())) {
                this.comboBoxScrambler.setSelectedIndex(i);
                break;
            }
        }

        // esc key closes window
        this.getRootPane().registerKeyboardAction(
                arg0 -> CategoryEditorDialog.this.setVisible(false),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void createComponents() {
        setLayout(
                new MigLayout(
                        "fill",
                        "[pref!][fill][pref!]",
                        "[pref!]8[pref!]8[pref!]8[pref!]8[pref!][grow]16[pref!]"));

        add(new JLabel(i18n("category_editor.description")));

        // textFieldDescription
        this.textFieldDescription = new JTextField();
        add(this.textFieldDescription, "span 2, wrap");

        add(new JLabel(i18n("category_editor.puzzle")));

        // comboBoxPuzzle
        this.comboBoxPuzzle = new JComboBox<>();
        add(this.comboBoxPuzzle, "span 2, wrap");

        add(new JLabel(i18n("category_editor.scrambler")));

        // comboBoxScrambler
        this.comboBoxScrambler = new JComboBox<>();
        add(this.comboBoxScrambler, "span 2, wrap");

        add(new JLabel(i18n("category_editor.force_start")));

        // comboBoxBldMode
        this.checkBoxBldMode = new JCheckBox();
        add(this.checkBoxBldMode, "span 2, wrap");

        add(new JLabel(i18n("category_editor.phase_count")));

		// spinnerPhases
		this.spinnerPhases = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
		add(this.spinnerPhases, "span 2, wrap");

		add(new JLabel(i18n("category_editor.tips")));

        // comboBoxTips
        this.comboBoxTips = new JComboBox<>();
        add(this.comboBoxTips);

        // buttonAdd
        this.buttonAdd = new JButton(i18n("category_editor.add"));
        add(this.buttonAdd, "sizegroup button, wrap");

        // listTips
        this.listTips = new JList<>(new DefaultListModel<>());
        this.listTips.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(this.listTips);
        scrollPane.setPreferredSize(new Dimension(0, 0));
        add(scrollPane, "grow, skip");

        // buttonUp
        this.buttonUp = new JButton(i18n("category_editor.up"));
        add(this.buttonUp, "sizegroup button, top, split 3, flowy");

        // buttonDown
        this.buttonDown = new JButton(i18n("category_editor.down"));
        add(this.buttonDown, "sizegroup button");

        // buttonRemove
        this.buttonRemove = new JButton(i18n("category_editor.remove"));
        add(this.buttonRemove, "sizegroup button, wrap");

        // buttonOk
        this.buttonOk = new JButton(i18n("category_editor.ok"));
        add(this.buttonOk, "tag ok, span 3, split");

        // buttonCancel
        this.buttonCancel = new JButton(i18n("category_editor.cancel"));
        add(this.buttonCancel, "tag cancel");
    }
}