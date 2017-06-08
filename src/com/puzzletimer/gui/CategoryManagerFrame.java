package com.puzzletimer.gui;

import com.puzzletimer.categories.Category;
import com.puzzletimer.categories.WcaCategory;
import com.puzzletimer.managers.CategoryManager;
import com.puzzletimer.puzzles.Puzzle;
import com.puzzletimer.puzzles.PuzzleProvider;
import com.puzzletimer.scramblers.Scrambler;
import com.puzzletimer.scramblers.ScramblerProvider;
import com.puzzletimer.tips.TipProvider;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.UUID;

import static com.puzzletimer.Internationalization.i18n;

@SuppressWarnings("serial")
public class CategoryManagerFrame extends JFrame {
    public class CategoryEditorListener {
        void categoryEdited(Category category) {}
    }

    private JTable table;
    private JButton buttonAdd;
    private JButton buttonEdit;
    private JButton buttonRemove;
    private JButton buttonOk;

    public CategoryManagerFrame(
            final PuzzleProvider puzzleProvider,
            final ScramblerProvider scramblerProvider,
            final CategoryManager categoryManager,
            final TipProvider tipProvider) {
        setTitle(i18n("category_manager.category_manager"));
        setMinimumSize(new Dimension(640, 480));

        createComponents();
        pack();

        categoryManager.addListener(new CategoryManager.Listener() {
            @Override
            public void categoriesUpdated(Category[] categories, Category currentCategory) {
                // set table data
                DefaultTableModel tableModel = new DefaultTableModel() {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };
                tableModel.addColumn(i18n("category_manager.description"));
                tableModel.addColumn(i18n("category_manager.puzzle"));
                tableModel.addColumn(i18n("category_manager.scrambler"));

                for (Category category : categories) {
                    Scrambler scrambler =
                        scramblerProvider.get(category.getScramblerId());
                    Puzzle puzzle =
                        scrambler.getPuzzle();

                    tableModel.addRow(new Object[] {
                        category.getDescription(),
                        puzzle.getDescription(),
                        scrambler.getDescription(),
                    });
                }

                CategoryManagerFrame.this.table.setModel(tableModel);
            }
        });

        // set table selection behavior
        CategoryManagerFrame.this.table.getSelectionModel().addListSelectionListener(
                event -> {
                    int selectedIndex = CategoryManagerFrame.this.table.getSelectedRow();

                    if (selectedIndex < 0) {
                        CategoryManagerFrame.this.buttonEdit.setEnabled(false);
                        CategoryManagerFrame.this.buttonRemove.setEnabled(false);
                    } else {
                        Category category = categoryManager.getCategories()[selectedIndex];
                        Category currentCategory = categoryManager.getCurrentCategory();

                        CategoryManagerFrame.this.buttonEdit.setEnabled(
                            category != currentCategory && category.isCustom());
                        CategoryManagerFrame.this.buttonRemove.setEnabled(
                            category != currentCategory && category.isCustom());
                    }
                });

        // set add button behavior
        this.buttonAdd.addActionListener(event -> {
            Category category = new WcaCategory(UUID.randomUUID(), "EMPTY", i18n("category_manager.new_category"), new String[0], '0', '0', 1, false, false, true);

            CategoryEditorListener listener = new CategoryEditorListener() {
                @Override
                public void categoryEdited(Category category) {
                    categoryManager.addCategory(category);
                }
            };

            CategoryEditorDialog dialog = new CategoryEditorDialog(
                CategoryManagerFrame.this,
                true,
                scramblerProvider.getAll(),
                puzzleProvider.getAll(),
                tipProvider.getAll(),
                category,
                true,
                listener);
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
        });

        // set edit button behavior
        this.buttonEdit.addActionListener(event -> {
            int selectedIndex = CategoryManagerFrame.this.table.getSelectedRow();
            Category category = categoryManager.getCategories()[selectedIndex];

            CategoryEditorListener listener = new CategoryEditorListener() {
                @Override
                public void categoryEdited(Category category) {
                    categoryManager.updateCategory(category);
                }
            };

            CategoryEditorDialog dialog = new CategoryEditorDialog(
                CategoryManagerFrame.this,
                true,
                scramblerProvider.getAll(),
                puzzleProvider.getAll(),
                tipProvider.getAll(),
                category,
                false,
                listener);
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
        });

        // set remove button behavior
        this.buttonRemove.addActionListener(event -> {
            int result = JOptionPane.showConfirmDialog(
                CategoryManagerFrame.this,
                i18n("category_manager.category_removal_confirmation_message"),
                i18n("category_manager.remove_category"),
                JOptionPane.YES_NO_CANCEL_OPTION);
            if (result != JOptionPane.YES_OPTION) {
                return;
            }

            int selectedIndex = CategoryManagerFrame.this.table.getSelectedRow();
            Category category = categoryManager.getCategories()[selectedIndex];
            categoryManager.removeCategory(category);
        });

        // set ok button behavior
        this.buttonOk.addActionListener(event -> setVisible(false));

        // esc key closes window
        this.getRootPane().registerKeyboardAction(
                arg0 -> CategoryManagerFrame.this.setVisible(false),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void createComponents() {
        setLayout(new MigLayout("fill", "[grow][pref!]", "[pref!]8[grow]16[pref!]"));

        // labelCategories
        JLabel labelCategories = new JLabel(i18n("category_manager.categories"));
        add(labelCategories, "span 2, wrap");

        // table
        this.table = new JTable();
        this.table.setShowVerticalLines(false);
        this.table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(this.table);
        this.table.setFillsViewportHeight(true);
        scrollPane.setPreferredSize(new Dimension(0, 0));
        add(scrollPane, "grow");

        // buttonAdd
        this.buttonAdd = new JButton(i18n("category_manager.add"));
        add(this.buttonAdd, "top, growx, split, flowy");

        // buttonEdit
        this.buttonEdit = new JButton(i18n("category_manager.edit"));
        this.buttonEdit.setEnabled(false);
        add(this.buttonEdit, "growx");

        // buttonRemove
        this.buttonRemove = new JButton(i18n("category_manager.remove"));
        this.buttonRemove.setEnabled(false);
        add(this.buttonRemove, "growx, wrap");

        // buttonOk
        this.buttonOk = new JButton(i18n("category_manager.ok"));
        add(this.buttonOk, "tag ok, span 2");
    }
}
