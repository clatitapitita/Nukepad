/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.adonis.Nukepad;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

/**
 *
 * @author croco
 */
public class GitPanel extends JPanel {
    private final GitRunner runner;
    private File repoDir;
    private final DefaultListModel<String> changedModel = new DefaultListModel<>();
    private final JLabel branchLabel = new JLabel("No repo");
    private final JTextField commitMsg = new JTextField();
    private final JList<String> changedList = new JList<>(changedModel);
    
    public GitPanel(GitRunner runner) {
        this.runner = runner;
        setLayout(new BorderLayout(4, 4));
        setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        
        JPanel top = new JPanel(new BorderLayout(4, 0));
        branchLabel.setFont(branchLabel.getFont().deriveFont(Font.BOLD));
        JButton refresh = new JButton("↻");
        refresh.addActionListener(e -> refresh());
        top.add(branchLabel, BorderLayout.CENTER);
        top.add(refresh, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);
        
        changedList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        add(new JScrollPane(changedList), BorderLayout.CENTER);
        
        JPanel bottom = new JPanel(new BorderLayout(4, 4));
        commitMsg.setToolTipText("Commit message");
        JPanel buttons = new JPanel(new GridLayout(1, 2, 4, 0));
        JButton stageAll = new JButton("Stage all");
        JButton commit = new JButton("Commit");
        stageAll.addActionListener(e -> stageAll());
        commit.addActionListener(e -> commit());
        buttons.add(stageAll);
        buttons.add(commit);
        bottom.add(commitMsg, BorderLayout.CENTER);
        bottom.add(buttons, BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);
    }
    public void setRepoDir(File dir) {
        this.repoDir = dir;
        refresh();
    }

    private void refresh() {
        if (repoDir == null) return;
        try {
           Process p = new ProcessBuilder("git", "rev-parse", "--abbrev-ref", "HEAD")
                .directory(repoDir).start();
            String branch = new BufferedReader(
                new InputStreamReader(p.getInputStream())).readLine();
            branchLabel.setText("⎇  " + (branch != null ? branch : "unknown"));
        } catch (Exception ex) {
            branchLabel.setText("Not a git repo");
    }
        changedModel.clear();
        try {
            Process p = new ProcessBuilder("git", "status", "--short")
                    .directory(repoDir).start();
            BufferedReader r = new BufferedReader (
                    new InputStreamReader(p.getInputStream()));
            String line;
            while((line = r.readLine()) !=null) changedModel.addElement(line);
            
        } catch (Exception ex) {
            
        }
        
    }
    private void stageAll() {
        if (repoDir == null) return;
        runner.run(repoDir, "add", "-A");
        SwingUtilities.invokeLater(this::refresh);
    }

    private void commit() {
        if(repoDir == null) return;
        String msg = commitMsg.getText().trim();
        if(msg.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a commit message");
            return;
        }
        runner.run(repoDir, "commit", "-m", msg);
        commitMsg.setText("");
        SwingUtilities.invokeLater(this::refresh);
    }
    
}
