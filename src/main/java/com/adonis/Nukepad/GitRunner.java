/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.adonis.Nukepad;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

/**
 *
 * @author croco
 */
public class GitRunner {
    private final JTextArea terminalArea;
    private final JTabbedPane bottomTabs;
    
    public GitRunner(JTextArea terminalArea, JTabbedPane bottomTabs) {
        this.terminalArea = terminalArea;
        this.bottomTabs = bottomTabs;
    }
    
    public void run(File workDir, String... args) {
        String[] cmd = new String[args.length + 1];
        cmd[0] = "git";
        System.arraycopy(args, 0, cmd, 1, args.length);
        
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(workDir);
        pb.redirectErrorStream(true);
        
        new SwingWorker<Integer, String>() {
            @Override
            protected Integer doInBackground() throws Exception {
                Process proc = pb.start();
                BufferedReader reader = new BufferedReader(
                new InputStreamReader(proc.getInputStream())
                );
                String line;
                while((line = reader.readLine()) != null) publish(line);
                return proc.waitFor();
            }
            @Override
            protected void process(java.util.List<String> chunks) {
                for(String line : chunks) {
                    terminalArea.append(line + "\n");
                    terminalArea.setCaretPosition(terminalArea.getDocument().getLength());
                    
                }
            }
            @Override
            protected void done() {
                try {
                    int code = get();
                    terminalArea.append("\n--- git exited with code" + code + "---\n");
                    bottomTabs.setSelectedIndex(0);
                    
                } catch (Exception ex) {
                    terminalArea.append("Git error: " + ex.getMessage() + "\n");
            
            }
            
        }
    }.execute();
    }
    
}
