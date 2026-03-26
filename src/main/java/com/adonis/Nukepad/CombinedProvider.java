/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.adonis.Nukepad;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.text.JTextComponent;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Token;

/**
 *
 * @author croco
 */
public class CombinedProvider extends DefaultCompletionProvider {
    private final RSyntaxTextArea editor;
    private final Set<String> projectWords = ConcurrentHashMap.newKeySet();
    
    public CombinedProvider(RSyntaxTextArea editor) {
        this.editor = editor;
        setAutoActivationRules(true, null);
    }
    public void setProjectWords(Set<String> words) {
        projectWords.clear();
        projectWords.addAll(words);
    }
    @Override
    public List<Completion> getCompletions(JTextComponent comp) {
        clear();
        
        Set<String> seen = new HashSet<>();
        
        RSyntaxDocument doc = (RSyntaxDocument) editor.getDocument();
        for(int i = 0; i < editor.getLineCount(); i++) {
            Token tk = doc.getTokenListForLine(i);
            while(tk != null && tk.isPaintable()) {
                int type = tk.getType();
                if (type == Token.RESERVED_WORD
                        || type == Token.RESERVED_WORD_2
                        || type == Token.FUNCTION
                        || type == Token.IDENTIFIER) {
                    String word = tk.getLexeme();
                    if(word.length() > 1 && seen.add(word))
                        addCompletion(new BasicCompletion(this, word));
                }
                tk = tk.getNextToken();
            }
        }
        for (String word : projectWords) {
            if(seen.add(word))
                addCompletion(new BasicCompletion(this, word));
        }
        return super.getCompletions(comp);
        
    }

    public Set<String> getProjectWords() {
        return Collections.unmodifiableSet(projectWords);
    }
    
}
