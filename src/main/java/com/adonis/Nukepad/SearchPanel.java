/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.adonis.Nukepad;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.AdjustmentEvent;
import javax.swing.SwingUtilities;
import javax.swing.JLabel;

/**
 *
 * @author croco
 */
public class SearchPanel extends JPanel {
    private final JTextField searchField = new JTextField();
    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JList<String> resultsList = new JList<>(listModel);
    private final FileIndex index = new FileIndex();
    private final JLabel statusLabel = new JLabel("Type to search...");
    private volatile boolean loading = false;

    public SearchPanel(File projectRoot) {
        setLayout(new BorderLayout(0, 6));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JLabel loadingLabel = new JLabel("Indexing files, please wait...");
        loadingLabel.setForeground(Color.GRAY);

        addPlaceholder(searchField, "Search...");

        new Thread(() -> {
            index.init(projectRoot);
            index.indexNextBatch(500);
            SwingUtilities.invokeLater(() -> statusLabel.setText(index.getIndexedCount() + " files indexed so far..."));

        }).start();
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                runSearch();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                runSearch();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                runSearch();
            }

        });
        resultsList.setCellRenderer(new FileListRenderer());
        resultsList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2)
                    openSelected();
            }
        });
        resultsList.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "open");
        resultsList.getActionMap().put("open", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                openSelected();
            }
        });
        JScrollPane scrollPane = new JScrollPane(resultsList);
        scrollPane.getVerticalScrollBar().addAdjustmentListener((AdjustmentEvent e) -> {
            javax.swing.JScrollBar bar = scrollPane.getVerticalScrollBar();
            int max = bar.getMaximum() - bar.getVisibleAmount();
            int current = bar.getValue();
            if (current >= max - 50 && !loading && index.hasMore()) {
                loading = true;
                new Thread(() -> {
                    index.indexNextBatch(500);
                    SwingUtilities.invokeLater(() -> {
                        runSearch();
                        if (!index.hasMore()) {
                            statusLabel
                                    .setText("Index complete." + index.getIndexedCount() + "files indexed so far...");
                            return;
                        } else {
                            statusLabel.setText(index.getIndexedCount() + "files indexed so far...");
                        }
                        loading = false;
                    });
                }).start();
            }

        });
        add(searchField, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
        add(searchField, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }

    private void runSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty() || query.equals("Search...")) {
            listModel.clear();
            statusLabel.setText(index.getIndexedCount() + "files indexed so far...");
            return;
        }
        listModel.clear();
        List<String> matches = BinarySearcher.searchByPrefix(index.getSortedPaths(), query);
        System.out.println("Query: [" + query + "] Matches: " + matches.size());
        statusLabel.setText(matches.size() + "matches");
        for (String path : matches)
            listModel.addElement(path);
    }

    private void openSelected() {
        String path = resultsList.getSelectedValue();
        if (path == null)
            return;
        File file = new File(path);

        if (file.isDirectory()) {
            try {
                java.awt.Desktop.getDesktop().open(file);
            } catch (Exception ex) {
                System.out.println("Could not open folder:" + ex.getMessage());
            }
            return;
        }
        try {
            String content = new String(java.nio.file.Files.readAllBytes(file.toPath()));
            Nukepad editor = Nukepad.getInstance();
            if (editor != null) {
                editor.openFileInNewTab(file, content);
            }

        } catch (Exception ex) {
            System.out.println("Could not open file:" + ex.getMessage());
        }
    }

    public static void addPlaceholder(JTextField field, String placeholder) {
        field.setForeground(Color.GRAY);
        field.setText(placeholder);
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }

            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setForeground(Color.GRAY);
                    field.setText(placeholder);
                }
            }
        });
    }

}
