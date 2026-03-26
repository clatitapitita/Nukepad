/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.adonis.Nukepad;

import org.fife.ui.rsyntaxtextarea.parser.AbstractParser;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParseResult;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParserNotice;
import org.fife.ui.rsyntaxtextarea.parser.ParseResult;
import org.fife.ui.rsyntaxtextarea.parser.ParserNotice;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatDarculaLaf;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

/**
 *
 * @author croco
 */
class Nukepad extends JFrame implements ActionListener{
    private InteractiveTerminal interactiveTerminal;
    private CombinedProvider sharedProvider;
    private JSplitPane verticalSplit;
    private boolean terminalVisible = true;
    private int lastDividerLocation = 500;
    private GitRunner gitRunner;
    private GitPanel gitPanel;

    private DefaultTreeModel openedProjectsTreeModel = new DefaultTreeModel(new DefaultMutableTreeNode("Projects"));
    public static Nukepad getInstance() {
        return instance;
    }
    private JTabbedPane tabs;
    RSyntaxTextArea text;
    JFrame frame;
    private File currentFile;
    private JTabbedPane bottomTabs;
    private JTextArea terminalArea;
    private javax.swing.table.DefaultTableModel problemsModel;
    
    Nukepad(File projectRoot) {
        try {
            ThemeManager.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
        frame = new JFrame("Editor");
        ImageIcon icon = new ImageIcon(getClass().getResource("/icons/nukepadlogo.png"));
        frame.setIconImage(icon.getImage());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        tabs = new JTabbedPane();
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
       
        text = new RSyntaxTextArea();
        text.setCodeFoldingEnabled(true);
        text.setAntiAliasingEnabled(true);
        sharedProvider = new CombinedProvider(text);
        AutoCompletion ac = new AutoCompletion(sharedProvider);
        ac.setAutoActivationEnabled(true);
        ac.setAutoActivationDelay(300);
        ac.install(text);
        applyEditorTheme(text);
        installLiveErrorParser(text);
        RTextScrollPane scroll = new RTextScrollPane(text);
        scroll.setRowHeaderView(new LineNumberPanel(text));
        tabs.addTab("Untitled", scroll);
        
        
        JMenuBar menb = new JMenuBar();
        JMenu men1 = new JMenu("File");
        
        JMenuItem menit1 = new JMenuItem("New");
        JMenuItem menit2 = new JMenuItem("Open");
        JMenuItem menit3 = new JMenuItem("Save");
        JMenuItem menit4 = new JMenuItem("Print");
        JMenuItem menit5 = new JMenuItem("Quit");
        
        menit1.addActionListener(this);
        menit2.addActionListener(this);
        menit3.addActionListener(this);
        menit3.addActionListener(this);
        menit4.addActionListener(this);
        menit5.addActionListener(this);
        
        men1.add(menit1);
        men1.add(menit2);
        men1.add(menit3);
        men1.add(menit4);
        men1.add(menit5);
        
        JMenu men2 = new JMenu("Edit");
        JMenuItem menit6 = new JMenuItem("Cut");
        JMenuItem menit7 = new JMenuItem("Copy");
        JMenuItem menit8 = new JMenuItem("Paste");
        
        menit6.addActionListener(this);
        menit7.addActionListener(this);
        menit8.addActionListener(this);
        
        JMenu men3 = new JMenu("View");
        JMenuItem darkTheme = new JMenuItem("Dark Theme");
        JMenuItem lightTheme = new JMenuItem("Light Theme");
        
        darkTheme.addActionListener(e -> {
           try {
             
               ThemeManager.save("dark");
               clearThemeOverrides();
               UIManager.setLookAndFeel(new FlatDarculaLaf());
               applyThemeToAllTabs();
               applyTerminalTheme();
               interactiveTerminal.applyTheme(true);
               SwingUtilities.updateComponentTreeUI(frame);
               frame.repaint();
              
           } catch (Exception ex) {
               ex.printStackTrace();
           }
        });
        
        lightTheme.addActionListener(e -> {
            try {
                
                ThemeManager.save("light");
                clearThemeOverrides();
                UIManager.setLookAndFeel(new FlatIntelliJLaf());
                applyThemeToAllTabs();
                applyTerminalTheme();
                interactiveTerminal.applyTheme(true);
                SwingUtilities.updateComponentTreeUI(frame);
                frame.repaint();
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        });
        men3.add(darkTheme);
        men3.add(lightTheme);
        menb.add(men3);
        
        JButton button1 = new JButton("Compile");
        button1.addActionListener(this);
        
        JButton button2 = new JButton("Run");
        button2.addActionListener(this);
        
        JButton button3 = new JButton("Author's signature");
        button3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    java.awt.Desktop.getDesktop().browse(URI.create("https://github.com/alexandru-andoni"));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        
        JButton buttonTerminal = new JButton("Terminal");
        buttonTerminal.addActionListener(e -> toggleTerminal());
        men2.add(menit6);
        men2.add(menit7);
        men2.add(menit8);
        
        JMenu gitMenu = new JMenu("Git");
        String[][] gitActions = {
              {"Init",   "init"},
              {"Status", "status"},
              {"Pull",   "pull"},
              {"Push",   "push"},
              {"Log",    "log", "--oneline", "-20"},
              {"Diff",   "diff"},
                          
        };
        for(String[] action : gitActions) {
            JMenuItem item = new JMenuItem(action[0]);
            String[] args = Arrays.copyOfRange(action, 1, action.length);
            item.addActionListener(e -> {
                File dir = currentFile != null ? currentFile.getParentFile()
                        : new File(System.getProperty("user.home"));
                gitRunner.run(dir, args);
            });
            gitMenu.add(item);
        }
        
        JMenu branchMenu = new JMenu("Branch");
        JMenuItem newBranch = new JMenu("New branch...");
        newBranch.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(frame, "Branch name:");
            if (name != null && !name.isBlank())
                gitRunner.run(currentFile != null ? currentFile.getParentFile()
                        : new File(System.getProperty("user.home")),
                        "checkout", "-b", name);
        });
        branchMenu.add(newBranch);
        gitMenu.add(branchMenu);
        menb.add(gitMenu);
        
        menb.add(men1);
        menb.add(men2);
        menb.add(button1);
        menb.add(button2);
        menb.add(button3);
        menb.add(buttonTerminal);
        
        frame.setJMenuBar(menb);
        RTextScrollPane scroll2 = new RTextScrollPane(text);
        scroll2.setRowHeaderView(new LineNumberPanel(text));
        tabs = new JTabbedPane();
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabs.addTab("Untitled", scroll2);
        
        setupDragAndDrop(tabs);
        setupDragAndDrop(scroll2);
        setupDragAndDrop(text);
        
        JLabel loadingLabel = new JLabel("Loading...", SwingConstants.CENTER);
        verticalSplit = new JSplitPane (
                JSplitPane.VERTICAL_SPLIT,
                tabs,
                buildBottomPanel()
        );
        gitRunner = new GitRunner(terminalArea, bottomTabs);
        gitPanel = new GitPanel(gitRunner);
        verticalSplit.setResizeWeight(0.75);
        
        JSplitPane mainSplit = new JSplitPane (
            JSplitPane.HORIZONTAL_SPLIT,
            loadingLabel,
            verticalSplit
        );
        mainSplit.setDividerLocation(280);
        frame.add(mainSplit, BorderLayout.CENTER);
        instance = this;
        
        frame.setSize(1280, 720);
        frame.setVisible(true);
        new javax.swing.SwingWorker<JTabbedPane, Void>() {
            @Override
            protected JTabbedPane doInBackground() {
                File rootDir = new File(System.getProperty("user.home"));
                DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(rootDir);
                rootNode.add(new DefaultMutableTreeNode("Loading..."));
                DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
                JTree tree = new JTree(treeModel);
                tree.setCellRenderer(new FileTreeCellRenderer());
                tree.setRootVisible(true);
                tree.setToggleClickCount(2);
                javax.swing.SwingUtilities.invokeLater(() -> tree.collapseRow(0));
                JScrollPane treeScroll = new JScrollPane(tree);
                
                
                tree.addTreeExpansionListener(new javax.swing.event.TreeExpansionListener() {
                    @Override
                    public void treeExpanded(TreeExpansionEvent event) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                                event.getPath().getLastPathComponent();
                        if(node.getChildCount() == 1 && node.getFirstChild().toString().equals("Loading...")) {
                            node.removeAllChildren();
                            File folder = (File) node.getUserObject();
                            File[] children = folder.listFiles();
                            if(children != null) {
                                java.util.Arrays.sort(children, (a,b) -> {
                                    if (a.isDirectory() && !b.isDirectory()) return -1;
                                    if(!a.isDirectory() && b.isDirectory()) return 1;
                                    return a.getName().compareToIgnoreCase(b.getName());
                                });
                                for (File child: children) {
                                    DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);
                                    if(child.isDirectory()) {
                                        childNode.add(new DefaultMutableTreeNode("Loading..."));
                                    }
                                    node.add(childNode);
                                }
                            }
                            treeModel.reload(node);
                        }
                    }

                    @Override
                    public void treeCollapsed(javax.swing.event.TreeExpansionEvent event) {
                        
                    }
                    
                });
                
                JPanel categoriesPanel = buildCategoriesPanel();
                JScrollPane categoriesScroll = new JScrollPane(categoriesPanel);
                
               
                
                JTree openedTree = new JTree(openedProjectsTreeModel);
                openedTree.setCellRenderer(new FileTreeCellRenderer());
                openedTree.setRootVisible(false);
                openedTree.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                      if(e.getClickCount() == 2) {
                          int row = openedTree.getRowForLocation(e.getX(), e.getY());
                          if(row < 0) return;
                          javax.swing.tree.TreePath treePath = openedTree.getPathForRow(row);
                          if (treePath == null) return;
                          DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
                          Object userObj = node.getUserObject();
                          if (!(userObj instanceof File)) return;
                          File clicked = (File) userObj;
                          if(clicked.isDirectory()) return;
                          try {
                              String content = new String(Files.readAllBytes(clicked.toPath()));
                              openFileInNewTab(clicked, content);
                          } catch (IOException ex) {
                              ex.printStackTrace();
                          }
                      }  
                    }
                });
               JScrollPane openedScroll = new JScrollPane(openedTree);
               openedTree.setRootVisible(false);
                openedTree.addTreeExpansionListener(new javax.swing.event.TreeExpansionListener() {
                    @Override
                    public void treeExpanded(javax.swing.event.TreeExpansionEvent event) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                                event.getPath().getLastPathComponent();
                        
                        if(node.getChildCount() == 1 && node.getFirstChild().toString().equals("Loading...")) {
                            node.removeAllChildren();
                            File folder = (File) node.getUserObject();
                            File[] children = folder.listFiles();
                            if(children != null) {
                                java.util.Arrays.sort(children, (a,b) -> {
                                    if(a.isDirectory() && !b.isDirectory()) return -1;
                                    if(!a.isDirectory() && b.isDirectory()) return 1;
                                    return a.getName().compareToIgnoreCase(b.getName());
                                });
                                for (File child : children) {
                                    DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);
                                    if(child.isDirectory()) {
                                        childNode.add(new DefaultMutableTreeNode("Loading..."));
                                    }
                                    node.add(childNode);
                                }
                            }
                            openedProjectsTreeModel.reload(node);
                        }
                    }

                    @Override
                    public void treeCollapsed(TreeExpansionEvent event) {
                    }
                });
                
                JTabbedPane leftTabs = new JTabbedPane();
                leftTabs.addTab("Files", treeScroll);
                leftTabs.addTab("Search", null);
                leftTabs.addTab("Categories", categoriesScroll);
                leftTabs.addTab("Opened Projects", openedScroll);
                leftTabs.addTab("Git", gitPanel);
                leftTabs.setPreferredSize(new Dimension(280, 0));
                return leftTabs;
               
            }
            @Override
            protected void done() {
                try {
                    JTabbedPane leftTabs = get();
                    mainSplit.setLeftComponent(leftTabs);
                    mainSplit.setDividerLocation(280);
                    
                JPanel searchPlaceholder = new JPanel(new BorderLayout());
                searchPlaceholder.add(new JLabel("Click to load search", SwingConstants.CENTER));
                leftTabs.setComponentAt(1, searchPlaceholder);
               
                leftTabs.addChangeListener(e -> {
                    if(leftTabs.getSelectedIndex() == 1 &&
                            leftTabs.getComponentAt(1) == searchPlaceholder) {
                        SearchPanel sp = new SearchPanel(new File(System.getProperty("user.home")));
                        sp.setPreferredSize(new Dimension(280, 0));
                        sp.setMinimumSize(new Dimension(100, 100));
                        leftTabs.setComponentAt(1, sp);
                        leftTabs.revalidate();
                        leftTabs.repaint();
                    }
                    
                });
                    
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            
        }.execute();
    
        
    }
    private static Nukepad instance;

    @Override
    public void actionPerformed(ActionEvent e) {
        String s = e.getActionCommand();
        switch(s) {
           case"Cut":
               text.cut();
               break;
           case"Copy":
               text.copy();
               break;
           case"Paste":
               text.paste();
               break;
           case"Save":
               JFileChooser jSave = new JFileChooser("f:");
               int rSave = jSave.showSaveDialog(null);
               
               if(rSave == JFileChooser.APPROVE_OPTION) {
                   File file = new File(jSave.getSelectedFile().getAbsolutePath());
                   try(BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
                       writer.write(text.getText());
                   } catch (Exception evt) {
                       JOptionPane.showMessageDialog(frame, evt.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                   }
               } else {
                   JOptionPane.showMessageDialog(frame, "The user has cancelled the operation!");
               }
               break;
           case "Print":
               try {
                   text.print();
               } catch(Exception evt) {
                   JOptionPane.showMessageDialog(frame, evt.getMessage());
               }
               break;
           case "Open":
               JFileChooser jOpen = new JFileChooser(System.getProperty("user.home"));
               jOpen.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
               int rOpen = jOpen.showOpenDialog(null);
               if (rOpen == JFileChooser.APPROVE_OPTION) {
                   File file = jOpen.getSelectedFile();
                   if(file.isDirectory()) {
                       addToOpenedProjects(file.getAbsolutePath());
                   } else {
                       try {
                           String content = new String(Files.readAllBytes(file.toPath()));
                           openFileInNewTab(file, content);
                       } catch (Exception evt) {
                           JOptionPane.showMessageDialog(frame,evt.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                       }
                   }
                   
               } else {
                   JOptionPane.showMessageDialog(frame, "The user has cancelled the operation!");
               }
               break;
           case"New":
               text.setText("");
               break;
           case"Quit":
               frame.setVisible(false);
               break;
           case"Compile":
               try {
                   problemsModel.setRowCount(0);
                   terminalArea.setText("");
                   
                   if(currentFile == null) {
                       terminalArea.append("ERROR: Save your file first before compiling. \n");
                       bottomTabs.setSelectedIndex(0);
                       return;
                   }
                   String ext = currentFile.getName().contains(".")
                           ? currentFile.getName().substring(currentFile.getName().lastIndexOf('.') +1 ).toLowerCase()
                           : "";
                   ProcessBuilder pbC;
                   switch(ext) {
                       case"java":
                           pbC = new ProcessBuilder("javac", currentFile.getPath());
                           break;
                       case "cpp":
                           pbC = new ProcessBuilder("g++", currentFile.getPath(), "-o",
                           currentFile.getPath().replace(".cpp", ""));
                           break;
                           case"c":
                               pbC = new ProcessBuilder("gcc", currentFile.getPath(), "-o",
                               currentFile.getPath().replace(".c", ""));
                               break;
                           case "py":
                               case"js":
                                   case"ts":
                                       case"tsx":
                                            case"jsx":
                                                terminalArea.append("ℹ️" + ext.toUpperCase() + "is interpreted, use Run instead.\n");
                                                bottomTabs.setSelectedIndex(0);
                                                return;
                                            default:
                                                terminalArea.append("ERROR: Compilation not supported for '.'" + ext + "'files.\n");
                                                bottomTabs.setSelectedIndex(0);
                                                return;   
                   }
                   
                   pbC.redirectErrorStream(true);
                   Process procC = pbC.start();
                   BufferedReader readerC = new BufferedReader(new InputStreamReader(procC.getInputStream()));
                   Pattern errorPat = Pattern.compile(".+:(\\d+): (error|warning): (.+)");
                   String lineC;
                   int errors = 0, warnings = 0;
                   while((lineC = readerC.readLine()) != null) {
                       terminalArea.append(lineC + "\n");
                       Matcher m = errorPat.matcher(lineC);
                       if (m.find()) {
                           String lineNum = m.group(1);
                           String type = m.group(2);
                           String msg = m.group(3);
                           String icon = type.equals("error") ? "❌" : "⚠️";
                           problemsModel.addRow(new Object[] {icon, msg, lineNum, currentFile.getName()});
                           if(type.equals("error")) errors++; else warnings++;
                       }
                   }
                   procC.waitFor();
                   if(procC.exitValue() == 0) {
                       terminalArea.append("\n ✅ Build successful. \n");
                       bottomTabs.setSelectedIndex(0);
                   } else {
                       terminalArea.append("\n ❌ Build failed -" + errors + "error(s), " + warnings + "warning(s)\n");
                       bottomTabs.setSelectedIndex(1);
                   }
                   
               } catch(Exception evt) {
                   terminalArea.append("Exception: " + evt.getMessage() + "\n");
                   bottomTabs.setSelectedIndex(0);
               }
               break;
               
           case "Run":
              terminalArea.setText("");
              bottomTabs.setSelectedIndex(0);

            if (currentFile == null) {
                terminalArea.append("ERROR: No file is currently open.\n");
                break;
            }

            String ext2 = currentFile.getName().contains(".")
                ? currentFile.getName().substring(currentFile.getName().lastIndexOf('.') + 1).toLowerCase()
                : "";
               String classDir = currentFile.getParent();
                String baseName = currentFile.getName().replace("." + ext2, "");

            ProcessBuilder pbR = null;
            switch (ext2) {
                case "java": pbR = new ProcessBuilder("java", "-cp", classDir, baseName); break;
                case "py":pbR = new ProcessBuilder("python3", currentFile.getPath()); break;
                case"ts":
                case"tsx":
                case"jsx":
                case "js":   pbR = new ProcessBuilder("node", currentFile.getPath()); break;
                case "cpp":
                case "c":    pbR = new ProcessBuilder(currentFile.getPath().replace("." + ext2, "")); break;
                default:
                terminalArea.append("ERROR: Run not supported for '." + ext2 + "' files.\n");
                return;
            }
            if(pbR == null) break;

            final ProcessBuilder finalPb = pbR;
            finalPb.redirectErrorStream(true);
            finalPb.directory(new File(classDir));

            new javax.swing.SwingWorker<Integer, String>() {
                @Override
            protected Integer doInBackground() throws Exception {
            Process proc = finalPb.start();
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(proc.getInputStream())
            );
            String line;
            while ((line = reader.readLine()) != null) {
                publish(line);  
            }
            return proc.waitFor();
        }
            @Override
            protected void process(java.util.List<String> chunks) {
            
            for (String line : chunks) {
                terminalArea.append(line + "\n");
                terminalArea.setCaretPosition(terminalArea.getDocument().getLength());
            }
        }
            @Override
            protected void done() {
                try {
                    int exitCode = get();
                    terminalArea.append("\n--- Process exited with code " + exitCode + " ---\n");
                } catch (Exception ex) {
                    terminalArea.append("Exception: " + ex.getMessage() + "\n");
                }
            }
        }.execute();
              break;      
                   
            default:
               System.out.println("Unknown command:" + s);
        }
    }
    public static void main(String[] args) {
        new IntroScreen();
    }
    public void openWebPage(String url) {
        try {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        } catch(java.io.IOException e) {
            System.out.println(e.getMessage());
        }
    }
    
    public void setEditorText(String textis) {
        text.setText(textis);
    }
    public File getCurrentFile() {
        return currentFile;
    }

    void setCurrentFile(File file) {
        this.currentFile = file;
    }

    public void openFileInNewTab(File file, String content) {
        RSyntaxTextArea editor = new RSyntaxTextArea();
        editor.setCodeFoldingEnabled(true);
        editor.setAntiAliasingEnabled(true);
        applyEditorTheme(editor);
        installLiveErrorParser(editor);
        String name= file.getName().toLowerCase();
        switch(name.substring(name.lastIndexOf('.') +1)) {
            case"java":
                editor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
                break;
            case"xml":
                editor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
                break;
            case"html":
                editor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
                break;
            case"js":
                editor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
                break;
            case"py":
                editor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PYTHON);
                break;
            case"cpp":
                editor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS);
                break;
            case"cs":
                editor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_CSHARP);
                break;
            case"c":
                editor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_C);
                break;
            case"tsx":
            case"ts":
            case "jsx":
                editor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_TYPESCRIPT);
                break;
            case"json":
                editor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
                break;
            case"sql":
                editor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
                break;
            case"go":
                editor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_GO);
                break;
            case"f90":
            case"f":
            case"for":
                editor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_FORTRAN);
                break;
            case"php":
                editor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PHP);
                break;
            default:
                editor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
                break;
        }
        editor.setText(content);
        CombinedProvider tabProvider = new CombinedProvider(editor);
        tabProvider.setProjectWords(sharedProvider != null
            ? ((CombinedProvider) sharedProvider).getProjectWords()  // see note below
            : Collections.emptySet());
            AutoCompletion ac = new AutoCompletion(tabProvider);
            ac.setAutoActivationEnabled(true);
            ac.setAutoActivationDelay(300);
            ac.install(editor);
        
        RTextScrollPane scroll = new RTextScrollPane(editor);
        scroll.setRowHeaderView(new LineNumberPanel(editor));
        
        tabs.addTab(file.getName(), scroll);
        tabs.setSelectedComponent(scroll);
        
        this.text = editor;
        this.currentFile = file;
        
        makeTabClosable(tabs, scroll, file.getName(), file.getAbsolutePath());
        
       addToOpenedProjects(file.getParentFile().getAbsolutePath());
       
       setupDragAndDrop(scroll);
       setupDragAndDrop(editor);
    }

   private void makeTabClosable(JTabbedPane tabs, Component tab, String title, String fullPath) {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setOpaque(false);

    JLabel label = new JLabel(title);
    JButton close = new JButton("✕");
    close.setBorder(null);
    close.setFocusable(false);
    close.setContentAreaFilled(false);

    close.addActionListener(e -> {
        int index = tabs.indexOfComponent(tab);
        if (index != -1) {
            tabs.remove(index);
        }
    });

    panel.add(label, BorderLayout.WEST);
    panel.add(close, BorderLayout.EAST);

    tabs.setTabComponentAt(tabs.indexOfComponent(tab), panel);
    }

    private JPanel buildCategoriesPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        Map<String, DefaultListModel<String>> categories = new LinkedHashMap<>();
        
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        JButton addCat = new JButton("Add Category (+)");
        JButton removeCat = new JButton("Remove Category (-)");
        toolbar.add(addCat);
        toolbar.add(removeCat);
        panel.add(toolbar);
        JLabel[] selected = {null};
        
        addCat.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(panel, "Category Name:");
            if (name == null || name.isBlank()) return;
            
            DefaultListModel<String> model = new DefaultListModel<>();
            categories.put(name, model);
            
            JPanel section = buildCategorySection(name, model, categories, panel);
            panel.add(section);
            panel.revalidate();
        });
        removeCat.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String[] names = categories.keySet().toArray(new String[0]);
                String choice = (String) JOptionPane.showInputDialog(
                        panel, "Which category to remove?", "Remove",
                        JOptionPane.PLAIN_MESSAGE, null, names,
                        names.length >0?names[0] : null);
                if(choice == null) return;
                categories.remove(choice);
                panel.removeAll();
                panel.add(toolbar);
                categories.forEach((n, m) ->
                        panel.add(buildCategorySection(n, m, categories, panel)));
                panel.revalidate();
                panel.repaint();
            }
        });
       
        return panel;
    }
    private void applyThemeToAllTabs() {
       for (int i = 0; i < tabs.getTabCount(); i++) {
           Component c = tabs.getComponentAt(i);
           if(c instanceof RTextScrollPane) {
               RSyntaxTextArea editor = (RSyntaxTextArea) ((RTextScrollPane) c).getTextArea();
               applyEditorTheme(editor);
               
           }
       }
   }
    private void clearThemeOverrides() {
        String[] keys = {
         "Panel.background", "Panel.foreground", "Label.foreground",
        "Button.background", "Button.foreground", "MenuBar.background",
        "MenuBar.foreground", "Menu.background", "Menu.foreground",
        "MenuItem.background", "MenuItem.foreground", "TabbedPane.background",
        "TabbedPane.foreground", "ScrollPane.background", "ScrollBar.background",
        "Tree.background", "Tree.foreground", "List.background", "List.foreground",
        "SplitPane.background", "TextField.background", "TextField.foreground",
        "TextArea.background", "TextArea.foreground"
        };
    for (String key : keys) UIManager.put(key, null);
    }
    private void applyEditorTheme(RSyntaxTextArea editor) {
        try {
            String themePath = ThemeManager.load().equals("dark")
                    ? "/org/fife/ui/rsyntaxtextarea/themes/monokai.xml"
                    : "/org/fife/ui/rsyntaxtextarea/themes/idea.xml";
            org.fife.ui.rsyntaxtextarea.Theme theme = 
                    org.fife.ui.rsyntaxtextarea.Theme.load(getClass().getResourceAsStream(themePath));
            theme.apply(editor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JPanel buildCategorySection(
            String name,
            DefaultListModel<String> model, 
            Map<String, DefaultListModel<String>> allCategories, 
            JPanel parent) {
        JPanel section = new JPanel(new BorderLayout());
        section.setBorder(BorderFactory.createTitledBorder(name));
        JList<String> list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        section.add(new JScrollPane(list), BorderLayout.CENTER);
        
        JPopupMenu popup = new JPopupMenu();
        JMenuItem addFile = new JMenuItem("Add file...");
        JMenuItem addFolder = new JMenuItem("Add folder...");
        JMenuItem removeItem = new JMenuItem("Remove selected");
        
        addFile.addActionListener( e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fc.setMultiSelectionEnabled(true);
            if (fc.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION)
                for(File f : fc.getSelectedFiles())
                    model.addElement(f.getAbsolutePath());
            
        });
        addFolder.addActionListener(e -> {
            JFileChooser fc2 = new JFileChooser();
            fc2.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (fc2.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
                File folder = fc2.getSelectedFile();
                addFilesFromFolder(folder, model);
            }
        });
        popup.add(addFile);
        popup.add(addFolder);
        popup.addSeparator();
        popup.add(removeItem);
        
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                maybeShow(e);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                maybeShow(e);
            }
            private void maybeShow(MouseEvent e){
                if(e.isPopupTrigger())
                    popup.show(list, e.getX(), e.getY());
            }
        });
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2) {
                    String path = list.getSelectedValue();
                    if(path == null) return;
                    File f = new File(path);
                    if(!f.exists() || f.isDirectory()) return;
                    try {
                        String content = new String(Files.readAllBytes(Paths.get(path)));
                        openFileInNewTab(f, content);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                        
                }
            }
        });
        
     return section;  
    }

    private void addFilesFromFolder(File folder, DefaultListModel<String> model) {
        addFilesFromFolderRecursive(folder, model, 0);
    }

    private void addFilesFromFolderRecursive(File folder, DefaultListModel<String> model, int depth) {
        String indent = " ".repeat(depth);
        model.addElement(indent + "[" + folder.getName() + "]");
        File[] children = folder.listFiles();
        if(children == null) return;
        java.util.Arrays.sort(children, (a,b) -> {
            if (a.isDirectory() && !b.isDirectory()) return -1;
            if(!a.isDirectory() && b.isDirectory()) return 1;
            return a.getName().compareToIgnoreCase(b.getName());
        });
        for (File child : children) {
            if (child.isDirectory()) {
                addFilesFromFolderRecursive(child, model, depth + 1);
            } else {
                model.addElement(child.getAbsolutePath());
            }
        }
    }

    public void addToOpenedProjects(String path) {
        File folder = new File(path);
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) openedProjectsTreeModel.getRoot();
        for (int i = 0; i < root.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) root.getChildAt(i);
            if (child.getUserObject().equals(folder)) return;
        }
        DefaultMutableTreeNode folderNode = new DefaultMutableTreeNode(folder);
        folderNode.add(new DefaultMutableTreeNode("Loading..."));
        root.add(folderNode);
        openedProjectsTreeModel.reload(root);
        scanProjectIntoProvider(folder, sharedProvider);
        if(interactiveTerminal != null) {
            interactiveTerminal.cdTo(new File(path));
        }
    }
    
    public void setupDragAndDrop(Component target) {
        new java.awt.dnd.DropTarget(target, new java.awt.dnd.DropTargetAdapter() {
            @Override
            public void drop(java.awt.dnd.DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(java.awt.dnd.DnDConstants.ACTION_COPY);
                    java.awt.datatransfer.Transferable transferable = evt.getTransferable();
                    java.util.List<File> files = (java.util.List<File>)
                            transferable.getTransferData(java.awt.datatransfer.DataFlavor.javaFileListFlavor);
                    
                    for (File file : files) {
                        if(file.isDirectory()) {
                            addToOpenedProjects(file.getAbsolutePath());
                        } else {
                            String content = new String(Files.readAllBytes(file.toPath()));
                            openFileInNewTab(file, content);
                        }
                    }
                    evt.dropComplete(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    evt.dropComplete(false);
                }
            }
        });
    }

    private Component buildBottomPanel() {
       bottomTabs = new JTabbedPane();
       
       terminalArea = new JTextArea();
       terminalArea.setEditable(false);
       terminalArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 13));
       applyTerminalTheme();
       JScrollPane termScroll = new JScrollPane(terminalArea);
       
       String[] cols = {
           "", "Description", "Line", "File"
       };
       problemsModel = new javax.swing.table.DefaultTableModel(cols, 0) {
           public boolean isCellEditable(int r, int c) {
               return false;
           }
       };
       javax.swing.JTable problemsTable = new javax.swing.JTable(problemsModel);
       problemsTable.getColumnModel().getColumn(0).setMaxWidth(30);
       problemsTable.getColumnModel().getColumn(2).setMaxWidth(60);
       
       problemsTable.addMouseListener(new MouseAdapter() {
           public void mouseClicked(MouseEvent e) {
               int row = problemsTable.getSelectedRow();
               if(row < 0) return;
               Object lineVal = problemsModel.getValueAt(row, 2);
               if(lineVal == null) return;
               try {
                   int line = Integer.parseInt(lineVal.toString());
                   jumpToLine(text, line);
               } catch (NumberFormatException igonored) {}
           }
           
       });
       bottomTabs.addTab("Terminal", termScroll);
       bottomTabs.addTab("Problems", new JScrollPane(problemsTable));
       
       interactiveTerminal = new InteractiveTerminal();
       bottomTabs.addTab("Shell", interactiveTerminal);
       
       JPanel wrapper = new JPanel (new BorderLayout());
       wrapper.setPreferredSize(new Dimension(0, 200));
       wrapper.add(bottomTabs);
       return wrapper;
    }

    private void applyTerminalTheme() {
       boolean isDark = ThemeManager.load().equals("dark");
       terminalArea.setBackground(isDark ? new Color(30, 30, 30) : new Color(255, 255, 255));
       terminalArea.setForeground(isDark ? new Color(200, 200, 200) : new Color(30, 30, 30));
       terminalArea.setCaretColor(isDark ? Color.WHITE : Color.BLACK);
    }
    
    private void jumpToLine(RSyntaxTextArea editor, int line) {
        try {
            int offset = editor.getLineStartOffset(line -1);
            editor.setCaretPosition(offset);
            editor.requestFocusInWindow();
            
        } catch (javax.swing.text.BadLocationException ignored) {}
    }

    private void toggleTerminal() {
        if (terminalVisible) {
            lastDividerLocation = verticalSplit.getDividerLocation();
            verticalSplit.getBottomComponent().setVisible(false);
            verticalSplit.setDividerLocation(1.0);
            terminalVisible = false;
        } else {
            verticalSplit.getBottomComponent().setVisible(true);
            verticalSplit.setDividerLocation(lastDividerLocation);
            terminalVisible = true;
        }
    }
    
    private void installLiveErrorParser(RSyntaxTextArea editor) {
        editor.addParser(new AbstractParser() {
        @Override
        public ParseResult parse(org.fife.ui.rsyntaxtextarea.RSyntaxDocument doc, String style) {
            DefaultParseResult result = new DefaultParseResult(this);
            if (currentFile == null) return result;

            String ext = currentFile.getName().contains(".")
                ? currentFile.getName().substring(currentFile.getName().lastIndexOf('.') + 1).toLowerCase()
                : "";

            // Only parse supported compiled languages
            if (!ext.equals("java") && !ext.equals("c") && !ext.equals("cpp")) return result;

            try {
                // Save current content to a temp file first
                File tempFile = currentFile;
                try (FileWriter fw = new FileWriter(tempFile)) {
                    fw.write(editor.getText());
                }

                ProcessBuilder pb;
                switch (ext) {
                    case "java": 
                        File tempOut = new File(System.getProperty("java.io.tmpdir"), "nukepad_compile_out");
                        tempOut.mkdirs();
                        pb = new ProcessBuilder("javac", "-d", tempOut.getAbsolutePath(), tempFile.getPath());
                        break;
                    case "cpp":  
                        pb = new ProcessBuilder("g++", "-fsyntax-only", tempFile.getPath()); 
                        break;
                    case "c":  
                        pb = new ProcessBuilder("gcc", "-fsyntax-only", tempFile.getPath());
                        break;
                    default: 
                        return result;
                }
                pb.redirectErrorStream(true);
                Process proc = pb.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                Pattern pat = Pattern.compile(".+:(\\d+): (error|warning): (.+)");
                String line;

                // Clear old problems
                SwingUtilities.invokeLater(() -> problemsModel.setRowCount(0));

                while ((line = reader.readLine()) != null) {
                    Matcher m = pat.matcher(line);
                    if (m.find()) {
                        int lineNum = Integer.parseInt(m.group(1));
                        String type = m.group(2);
                        String msg  = m.group(3);

                        // Add squiggle to editor
                        DefaultParserNotice notice = new DefaultParserNotice(
                            this, msg, lineNum - 1
                        );
                        notice.setLevel(type.equals("error")
                            ? ParserNotice.Level.ERROR
                            : ParserNotice.Level.WARNING);
                        result.addNotice(notice);

                        // Add to Problems table
                        String icon = type.equals("error") ? "❌" : "⚠️";
                        final String fMsg = msg;
                        final int fLine = lineNum;
                        SwingUtilities.invokeLater(() ->
                            problemsModel.addRow(new Object[]{icon, fMsg, fLine, currentFile.getName()})
                        );
                    }
                }
                proc.waitFor();

            } catch (Exception ex) {
                
            }
            File tempOut2 = new File(System.getProperty("java.io.tmpdir"), "nukepad_compile_out");
            File[] classFiles = tempOut2.listFiles((dir, name) -> name.endsWith(".class"));
            if (classFiles != null) {
                for (File cf : classFiles) cf.delete();
            }
            return result;
        }
    });
}
    private void scanProjectIntoProvider(File projectDir, CombinedProvider provider) {
        new SwingWorker<Set<String>, Void>() {
            @Override
            protected Set<String> doInBackground() throws Exception {
                Set<String> words = new HashSet<>();
                java.util.regex.Pattern p =
                        java.util.regex.Pattern.compile("\\\\b[a-zA-Z_][a-zA-Z0-9_]{2,}\\\\b");
                scanDir(projectDir, p, words, 0);
                return words;
            }
            private void scanDir(File dir, java.util.regex.Pattern p,
                    Set<String> words, int depth) throws Exception {
                if(depth > 5) return;
                File[] files = dir.listFiles();
                if(files == null) return;
                for (File f : files) {
                    if(f.isDirectory()) {
                        scanDir(f, p, words, depth + 1);
                    } else if (isSource(f)) {
                        String content = new String (
                        java.nio.file.Files.readAllBytes(f.toPath()));
                        java.util.regex.Matcher mat = p.matcher(content);
                        while(mat.find()) words.add(mat.group());
                    }
                }
            }
            private boolean isSource(File f) {
                String n = f.getName();
                 return n.endsWith(".java") || n.endsWith(".py")
                || n.endsWith(".js")   || n.endsWith(".ts")
                || n.endsWith(".cpp")  || n.endsWith(".c");
                
            }
            @Override
            protected void done() {
                try {
                    provider.setProjectWords(get());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            
        }.execute();
    }   
    
}
