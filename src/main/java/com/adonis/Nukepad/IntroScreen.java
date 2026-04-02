/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.adonis.Nukepad;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author croco
 */
public class IntroScreen {
    private JFrame introFrame;
    private static final File RECENTS_FILE = new File(
            System.getProperty("user.home") + "/.nukepad_recents.txt");
    private static final int MAX_RECENTS = 8;

    public IntroScreen() {
        try {
            String savedTheme = loadTheme();
            if (savedTheme.equals("dark")) {
                UIManager.setLookAndFeel(new FlatDarculaLaf());
            } else {
                UIManager.setLookAndFeel(new FlatIntelliJLaf());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        introFrame = new JFrame("Welcome to Nukepad!");
        ImageIcon icon = new ImageIcon(getClass().getResource("/icons/nukepadlogo.png"));
        introFrame.setIconImage(icon.getImage());
        introFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        introFrame.setSize(700, 600);
        introFrame.setLocationRelativeTo(null);
        introFrame.setResizable(false);

        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBorder(new EmptyBorder(40, 40, 32, 40));
        main.setBackground(Color.WHITE);
        introFrame.add(main);
        introFrame.setVisible(true);
        new Thread(this::playOpenSound).start();

        JLabel logo;
        try {
            java.awt.Image scaled = icon.getImage().getScaledInstance(80, 80, java.awt.Image.SCALE_SMOOTH);
            logo = new JLabel(new ImageIcon(scaled));
        } catch (Exception e) {
            logo = new JLabel("N");
            logo.setFont(new Font("SansSerif", Font.BOLD, 36));
            logo.setOpaque(true);
            logo.setBackground(new Color(240, 240, 240));
            logo.setPreferredSize(new Dimension(80, 80));
            logo.setMaximumSize(new Dimension(80, 80));
            logo.setHorizontalAlignment(SwingConstants.CENTER);
        }
        logo.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Nukepad");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Open a file or project to get started");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(Color.GRAY);
        subtitle.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.setBackground(Color.WHITE);
        buttons.setAlignmentX(JPanel.CENTER_ALIGNMENT);
        buttons.setMaximumSize(new Dimension(440, 40));

        JButton themeToggle = new JButton(loadTheme().equals("dark") ? "☀ Light Theme" : " 🌙 Dark Theme");
        JPanel recentsPanel = new JPanel();
        recentsPanel.setLayout(new BoxLayout(recentsPanel, BoxLayout.Y_AXIS));
        recentsPanel.setBackground(UIManager.getColor("Panel.background"));
        recentsPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        recentsPanel.setMaximumSize(new Dimension(520, 999));

        themeToggle.setAlignmentX(JButton.CENTER_ALIGNMENT);
        themeToggle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        themeToggle.addActionListener(e -> {
            String current = loadTheme();
            String next = current.equals("dark") ? "light" : "dark";
            saveTheme(next);
            try {
                if (next.equals("dark")) {
                    UIManager.setLookAndFeel(new FlatDarculaLaf());
                    themeToggle.setText("☀ Light Theme");
                } else {
                    UIManager.setLookAndFeel(new FlatIntelliJLaf());
                    themeToggle.setText("🌙 Dark Theme");
                }
                SwingUtilities.updateComponentTreeUI(introFrame);

                Color bg = UIManager.getColor("Panel.background");
                main.setBackground(bg);
                buttons.setBackground(bg);
                recentsPanel.setBackground(bg);
                introFrame.repaint();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        JButton openFile = new JButton("Open File");
        JButton openProject = new JButton("Open Project / Folder");
        openFile.setPreferredSize(new Dimension(200, 38));
        openProject.setPreferredSize(new Dimension(200, 38));
        openFile.setMaximumSize(new Dimension(200, 38));
        openProject.setMaximumSize(new Dimension(200, 38));
        openFile.setFont(new Font("SansSerif", Font.PLAIN, 13));
        openProject.setFont(new Font("SansSerif", Font.PLAIN, 13));

        openFile.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (chooser.showOpenDialog(introFrame) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                openFile.setEnabled(false);
                openFile.setText("Opening...");
                new Thread(() -> {
                    try {
                        String content = new String(java.nio.file.Files.readAllBytes(file.toPath()));
                        saveRecent(file.getAbsolutePath());
                        javax.swing.SwingUtilities.invokeLater(() -> {
                            introFrame.dispose();
                            Nukepad editor = new Nukepad(file.getParentFile());
                            editor.openFileInNewTab(file, content);
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        javax.swing.SwingUtilities.invokeLater(() -> {
                            openFile.setEnabled(true);
                            openFile.setText("Open File");
                        });
                    }
                }).start();
            }

        });
        openProject.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(introFrame) == JFileChooser.APPROVE_OPTION) {
                File folder = chooser.getSelectedFile();
                openProject.setEnabled(false);
                openProject.setText("Opening...");
                new Thread(() -> {
                    saveRecent(folder.getAbsolutePath());
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        introFrame.dispose();
                        Nukepad editor = new Nukepad(folder);
                        editor.addToOpenedProjects(folder.getAbsolutePath());
                    });
                }).start();
            }
        });
        buttons.add(openFile);
        buttons.add(Box.createHorizontalStrut(12));
        buttons.add(openProject);

        main.add(buttons);
        main.add(Box.createVerticalStrut(10));
        main.add(themeToggle);
        main.add(Box.createVerticalStrut(28));
        main.add(new JSeparator(SwingConstants.HORIZONTAL));

        JPanel recentsPanel1 = new JPanel();
        recentsPanel1.setLayout(new BoxLayout(recentsPanel1, BoxLayout.Y_AXIS));
        recentsPanel1.setBackground(Color.WHITE);
        recentsPanel1.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        recentsPanel1.setMaximumSize(new Dimension(520, 999));

        JLabel recentsTitle = new JLabel("Recent");
        recentsTitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        recentsTitle.setForeground(Color.GRAY);
        recentsTitle.setAlignmentX(JLabel.LEFT_ALIGNMENT);

        recentsPanel1.add(recentsTitle);
        recentsPanel1.add(Box.createVerticalStrut(8));

        List<String> recents = loadRecents();
        if (recents.isEmpty()) {
            JLabel none = new JLabel("No recent files yet.");
            none.setFont(new Font("SansSerif", Font.PLAIN, 12));
            none.setForeground(Color.LIGHT_GRAY);
            none.setAlignmentX(JLabel.LEFT_ALIGNMENT);
            recentsPanel1.add(none);
        } else {
            for (String path : recents) {
                recentsPanel1.add(buildRecentRow(path));
                recentsPanel1.add(Box.createVerticalStrut(2));
            }
        }

        main.add(logo);
        main.add(Box.createVerticalStrut(14));
        main.add(title);
        main.add(Box.createVerticalStrut(6));
        main.add(subtitle);
        main.add(Box.createVerticalStrut(28));
        main.add(buttons);
        main.add(Box.createVerticalStrut(28));
        main.add(new JSeparator(SwingConstants.HORIZONTAL));
        main.add(Box.createVerticalStrut(16));
        main.add(recentsPanel);
        main.setBackground(UIManager.getColor("Panel.background"));
        buttons.setBackground(UIManager.getColor("Panel.background"));
        recentsPanel.setBackground(UIManager.getColor("Panel.background"));

        introFrame.add(main);
        introFrame.setVisible(true);
    }

    private JPanel buildRecentRow(String path) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        row.setMaximumSize(new Dimension(520, 36));
        row.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        row.setBackground(UIManager.getColor("Panel.background"));

        File f = new File(path);
        boolean isDir = f.isDirectory();

        JLabel nameLabel = new JLabel(f.getName());
        nameLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JLabel pathLabel = new JLabel(path);
        pathLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        pathLabel.setForeground(Color.GRAY);

        row.add(nameLabel);
        row.add(Box.createHorizontalStrut(12));
        row.add(pathLabel);
        row.add(Box.createHorizontalGlue());

        row.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                row.setBackground(new Color(245, 245, 245));
                nameLabel.setBackground(new Color(245, 245, 245));
                row.setBackground(UIManager.getColor("Panel.background").darker());
                nameLabel.setBackground(UIManager.getColor("Panel.background").darker());

            }

            @Override
            public void mouseExited(MouseEvent e) {
                row.setBackground(Color.WHITE);
                row.setBackground(UIManager.getColor("Panel background"));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (!f.exists())
                    return;
                saveRecent(path);
                introFrame.dispose();
                if (isDir) {
                    new Thread(() -> {
                        javax.swing.SwingUtilities.invokeLater(() -> {
                            introFrame.dispose();
                            Nukepad editor = new Nukepad(f);
                            editor.addToOpenedProjects(f.getAbsolutePath());
                        });
                    }).start();
                } else {
                    new Thread(() -> {
                        try {
                            String content = new String(java.nio.file.Files.readAllBytes(f.toPath()));
                            javax.swing.SwingUtilities.invokeLater(() -> {
                                introFrame.dispose();
                                Nukepad editor = new Nukepad(f.getParentFile());
                                editor.openFileInNewTab(f, content);
                            });
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }).start();
                }

            }
        });
        return row;
    }

    private void saveRecent(String path) {
        List<String> recents = loadRecents();
        recents.remove(path);
        recents.add(0, path);
        if (recents.size() > MAX_RECENTS) {
            recents = recents.subList(0, MAX_RECENTS);

        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(RECENTS_FILE))) {
            for (String p : recents) {
                writer.write(p);
                writer.newLine();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private List<String> loadRecents() {
        List<String> recents = new ArrayList<>();
        if (!RECENTS_FILE.exists())
            return recents;
        try (BufferedReader reader = new BufferedReader(new FileReader(RECENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank())
                    recents.add(line.trim());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return recents;
    }

    private void playOpenSound() {
        try {
            java.net.URL soundURL = getClass().getResource("/assets/open.wav");
            System.out.println("Sound URL:" + soundURL);
            if (soundURL == null)
                return;
            AudioInputStream audio = AudioSystem.getAudioInputStream(soundURL);
            Clip clip = AudioSystem.getClip();
            clip.open(audio);
            clip.start();

        } catch (Exception ex) {
            System.out.println("Could not play sound: " + ex.getMessage());
        }
    }

    private static final File THEME_FILE = new File(
            System.getProperty("user.home") + "/.nukepad_theme.txt");

    private void saveTheme(String theme) {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(THEME_FILE))) {
            w.write(theme);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private String loadTheme() {
        if (!THEME_FILE.exists())
            return "light";
        try (BufferedReader r = new BufferedReader(new FileReader(THEME_FILE))) {
            String line = r.readLine();
            return (line != null && !line.isBlank()) ? line.trim() : "light";
        } catch (IOException ex) {
            return "light";
        }
    }

}
