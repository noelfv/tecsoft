/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui.states;

import com.novatronic.formatter.gui.ComponentsID;
import com.novatronic.formatter.gui.Components;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import org.apache.log4j.Logger;

/**
 *
 * @author Omar
 */
public class TestGroupLoadedState extends State{
    private static final Logger log = Logger.getLogger(TestGroupLoadedState.class);
    
    public static final String EMPTY = "";
    
    @Override
    public void execute() {
        JButton button;
        JTextArea textArea;
        JTextField text;
        JTree tree;
        Components componentsManager = Components.getInstance();
        
        button = componentsManager.get(ComponentsID.BT_LOAD_SELECT_FILE, JButton.class);
        button.setEnabled(true);
        button = componentsManager.get(ComponentsID.BT_SELECT_TEST, JButton.class);
        button.setEnabled(true);
        button = componentsManager.get(ComponentsID.BT_SAVE_FRAME_RESULT, JButton.class);
        button.setEnabled(false);
        button = componentsManager.get(ComponentsID.BT_SAVE_FRAME_TEST, JButton.class);
        button.setEnabled(false);
        button = componentsManager.get(ComponentsID.BT_TO_FRAME, JButton.class);
        button.setEnabled(false);
        button = componentsManager.get(ComponentsID.BT_TO_INTFORMAT, JButton.class);
        button.setEnabled(false);
        button = componentsManager.get(ComponentsID.BT_COMPARE_FRAME, JButton.class);
        button.setEnabled(false);
        button = componentsManager.get(ComponentsID.BT_COMPARE_INTFORMAT, JButton.class);
        button.setEnabled(false);
        
        textArea = componentsManager.get(ComponentsID.TA_TEST_DESC, JTextArea.class);
        textArea.setText(EMPTY);
        text = componentsManager.get(ComponentsID.TF_FORMAT_ID, JTextField.class);
        text.setText(EMPTY);
        
        textArea = componentsManager.get(ComponentsID.TA_FRAME_RESULT, JTextArea.class);
        textArea.setText(EMPTY);
        textArea = componentsManager.get(ComponentsID.TA_FRAME_TEST, JTextArea.class);
        textArea.setText(EMPTY);
        tree = componentsManager.get(ComponentsID.TR_INTFORMAT_RESULT, JTree.class);
        tree.setModel(null);
        tree = componentsManager.get(ComponentsID.TR_INTFORMAT_TEST, JTree.class);
        tree.setModel(null);
        
        log.debug("Estado TestGroupLoadedState ejecutado");
    }
    
}
