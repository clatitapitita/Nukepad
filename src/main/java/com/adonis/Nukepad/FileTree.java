/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.adonis.Nukepad;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

/**
 *
 * @author croco
 */
public class FileTree extends JTree {
    public FileTree(File root) {
        super((TreeModel) new FileTreeModel(root));
        setRootVisible(false);
        setShowsRootHandles(true);
        addTreeSelectionListener(new FileOpenListener());
    }

    private class FileOpenListener implements TreeSelectionListener {
        @Override
        public void valueChanged(TreeSelectionEvent e) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) getLastSelectedPathComponent();
            if (node == null)
                return;
            File file = (File) node.getUserObject();
            if (file.isFile()) {
                try {
                    String content = Files.readString(file.toPath());
                    String name = file.getName().toLowerCase();

                    switch (name.substring(name.lastIndexOf('.') + 1)) {
                        case "java":
                            Nukepad.getInstance().text.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
                            break;
                        case "xml":
                            Nukepad.getInstance().text.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
                            break;
                        case "html":
                            Nukepad.getInstance().text.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_HTML);
                            break;
                        case "js":
                            Nukepad.getInstance().text.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
                            break;
                        case "py":
                            Nukepad.getInstance().text.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PYTHON);
                            break;
                        case "cpp":
                            Nukepad.getInstance().text.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS);
                            break;
                        case "cs":
                            Nukepad.getInstance().text.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_CSHARP);
                            break;
                        case "c":
                            Nukepad.getInstance().text.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_C);
                            break;
                        case "tsx":
                        case "ts":
                        case "jsx":
                            Nukepad.getInstance().text.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_TYPESCRIPT);
                            break;
                        case "json":
                            Nukepad.getInstance().text.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
                            break;
                        case "f":
                        case "f90":
                        case "for":
                            Nukepad.getInstance().text.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_FORTRAN);
                            break;
                        case "sql":
                            Nukepad.getInstance().text.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
                            break;
                        case "go":
                            Nukepad.getInstance().text.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_GO);
                            break;
                        case "php":
                            Nukepad.getInstance().text.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PHP);
                            break;

                        default:
                            Nukepad.getInstance().text.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
                            break;
                    }

                    Nukepad.getInstance().openFileInNewTab(file, content);
                    Nukepad.getInstance().setCurrentFile(file);

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

}
