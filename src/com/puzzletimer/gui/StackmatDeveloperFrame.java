package com.puzzletimer.gui;

import com.puzzletimer.managers.TimerManager;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import static com.puzzletimer.Internationalization.i18n;

@SuppressWarnings("serial")
public class StackmatDeveloperFrame extends JFrame {
    private JTextArea textAreaSummary;
    private JButton buttonCopyToClipboard;
    private JButton buttonOk;
    private StackmatGraphPanel graphPanel;

    public StackmatDeveloperFrame(TimerManager timerManager) {
        super();

        setMinimumSize(new Dimension(800, 600));

        createComponents();
        pack();
        setTitle(i18n("stackmat_developer.title"));
        
        timerManager.addListener(new TimerManager.Listener() {
            @Override
            public void dataNotReceived(byte[] data) {
                updateSummary(data);
            }
        });

        // copy to clipboard
        this.buttonCopyToClipboard.addActionListener(event -> {
            StringSelection contents =
                new StringSelection(StackmatDeveloperFrame.this.textAreaSummary.getText());
            Clipboard clipboard =
                Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(contents, contents);
        });

        // ok button
        this.setDefaultCloseOperation(HIDE_ON_CLOSE);
        this.buttonOk.addActionListener(event -> StackmatDeveloperFrame.this.setVisible(false));

        // esc key closes window
        this.getRootPane().registerKeyboardAction(
                arg0 -> StackmatDeveloperFrame.this.setVisible(false),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void createComponents() {
        setLayout(
            new MigLayout(
                "fill",
                "",
                "[pref!][pref!]16[pref!][][pref!]16[pref!]"));
        
        // labelGraph
        add(new JLabel(i18n("stackmat_developer.graph")), "span, wrap");

        // Graph
        this.graphPanel = new StackmatGraphPanel();
        this.graphPanel.setBackground(this.getBackground());
        add(this.graphPanel, "growx, height 90, span, wrap");

        // labelRawData
        add(new JLabel(i18n("stackmat_developer.raw_data")), "wrap");

        // textAreaContents
        this.textAreaSummary = new JTextArea();
        this.textAreaSummary.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(this.textAreaSummary);
        scrollPane.setPreferredSize(new Dimension(0, 0));
        add(scrollPane, "grow, wrap");

        // button copy to clipboard
        this.buttonCopyToClipboard = new JButton(i18n("stackmat_developer.copy_to_clipboard"));
        add(this.buttonCopyToClipboard, "width 150, right, wrap");

        // buttonOk
        this.buttonOk = new JButton(i18n("stackmat_developer.ok"));
        add(this.buttonOk, "tag ok");
    }

    public void updateSummary(byte[] data) {
        String text = "";
        if(data != null){
            for (byte aData : data) {
                text = text + aData + " ";
            }
	        this.graphPanel.setData(data);
	        this.textAreaSummary.setText(text);
        } else {
        	this.textAreaSummary.setText(i18n("stackmat_developer.error"));
        }
    }
}
