/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.adonis.Nukepad;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 *
 * @author croco
 */
public class InteractiveTerminal extends JPanel {
     private final JTextArea display;
    private final JTextField input;
    private Process shellProcess;
    private PrintWriter shellInput;
    private final List<String> history = new ArrayList<>();
    private int historyIndex = -1;

    public InteractiveTerminal() {
        setLayout(new BorderLayout());

        display = new JTextArea();
        display.setEditable(false);
        display.setFont(new Font("Monospaced", Font.PLAIN, 13));
        display.setBackground(new Color(20, 20, 20));
        display.setForeground(new Color(220, 220, 220));
        display.setCaretColor(Color.WHITE);

        JScrollPane scroll = new JScrollPane(display);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, BorderLayout.CENTER);

        JPanel inputRow = new JPanel(new BorderLayout(4, 0));
        inputRow.setBackground(new Color(30, 30, 30));
        inputRow.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

        JLabel prompt = new JLabel("❯");
        prompt.setForeground(new Color(100, 220, 100));
        prompt.setFont(new Font("Monospaced", Font.BOLD, 13));

        input = new JTextField();
        input.setBackground(new Color(30, 30, 30));
        input.setForeground(new Color(220, 220, 220));
        input.setCaretColor(Color.WHITE);
        input.setFont(new Font("Monospaced", Font.PLAIN, 13));
        input.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));

        inputRow.add(prompt, BorderLayout.WEST);
        inputRow.add(input, BorderLayout.CENTER);
        add(inputRow, BorderLayout.SOUTH);

        input.addActionListener(e -> sendCommand(input.getText()));

        input.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    if (historyIndex < history.size() - 1) {
                        historyIndex++;
                        input.setText(history.get(history.size() - 1 - historyIndex));
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (historyIndex > 0) {
                        historyIndex--;
                        input.setText(history.get(history.size() - 1 - historyIndex));
                    } else {
                        historyIndex = -1;
                        input.setText("");
                    }
                }
            }
        });

        startShell();
    }

    private void startShell() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;
            if (os.contains("win")) {
                pb = new ProcessBuilder("cmd.exe");
            } else if (os.contains("mac")) {
                pb = new ProcessBuilder("/bin/zsh");
            } else {
                pb = new ProcessBuilder("/bin/bash");
            }

            pb.redirectErrorStream(true);
            pb.directory(new File(System.getProperty("user.home")));
            pb.environment().putAll(System.getenv());

            shellProcess = pb.start();
            shellInput = new PrintWriter(
                new BufferedWriter(new OutputStreamWriter(shellProcess.getOutputStream())),
                true  
            );

            Thread reader = new Thread(() -> {
                try {
                    BufferedReader br = new BufferedReader(
                        new InputStreamReader(shellProcess.getInputStream()));
                    char[] buf = new char[1024];
                    int n;
                    while ((n = br.read(buf, 0, buf.length)) != -1) {
                        String chunk = new String(buf, 0, n);
                        SwingUtilities.invokeLater(() -> {
                            display.append(chunk);
                            display.setCaretPosition(display.getDocument().getLength());
                        });
                    }
                } catch (IOException ex) {
                    SwingUtilities.invokeLater(() ->
                        display.append("\n[Shell process ended]\n"));
                }
            });
            reader.setDaemon(true);  
            reader.start();

        } catch (Exception ex) {
            display.append("Failed to start shell: " + ex.getMessage() + "\n");
        }
    }

    private void sendCommand(String cmd) {
        if (cmd == null || cmd.isBlank()) return;

        
        if (history.isEmpty() || !history.get(history.size() - 1).equals(cmd)) {
            history.add(cmd);
        }
        historyIndex = -1;
        input.setText("");

        if (shellInput != null) {
            shellInput.println(cmd); 
        }
    }

    
    public void cdTo(File dir) {
        if (dir != null && dir.isDirectory()) {
            sendCommand("cd \"" + dir.getAbsolutePath() + "\"");
        }
    }

    
    public void restart() {
        if (shellProcess != null && shellProcess.isAlive()) {
            shellProcess.destroyForcibly();
        }
        display.setText("");
        startShell();
    }

    public void applyTheme(boolean dark) {
        Color bg = dark ? new Color(20, 20, 20) : new Color(255, 255, 255);
        Color fg = dark ? new Color(220, 220, 220) : new Color(30, 30, 30);
        display.setBackground(bg);
        display.setForeground(fg);
        display.setCaretColor(dark ? Color.WHITE : Color.BLACK);

        Color inputBg = dark ? new Color(30, 30, 30) : new Color(245, 245, 245);
        input.setBackground(inputBg);
        input.setForeground(fg);
        input.getParent().setBackground(inputBg);
    }
}
