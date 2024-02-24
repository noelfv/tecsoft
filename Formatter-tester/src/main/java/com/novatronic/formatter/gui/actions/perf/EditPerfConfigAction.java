/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui.actions.perf;

import com.novatronic.formatter.gui.ComponentsID;
import com.novatronic.formatter.gui.Components;
import com.novatronic.formatter.gui.actions.Action;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import org.apache.log4j.Logger;

/**
 *
 * @author Omar
 */
public class EditPerfConfigAction extends Action<JButton> implements ActionListener  {
    private static final Logger log = Logger.getLogger(EditPerfConfigAction.class);
    
    @Override
    public void subscribe(JButton component) {
        component.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JTextField text;
        JButton button;
        JComboBox combo;
        Components componentsManager = Components.getInstance();
        
        text = componentsManager.get(ComponentsID.TF_PERF_THREAD_SIZE, JTextField.class);
        text.setEditable(true);
        text = componentsManager.get(ComponentsID.TF_PERF_EXEC_BY_THREAD, JTextField.class);
        text.setEditable(true);
        text = componentsManager.get(ComponentsID.TF_PERF_THREAD_DELAY, JTextField.class);
        text.setEditable(true);
        text = componentsManager.get(ComponentsID.TF_PERF_GRAPH_REFRESH, JTextField.class);
        text.setEditable(true);
        combo = componentsManager.get(ComponentsID.CB_DIRECTION_EXEC, JComboBox.class);
        combo.setEnabled(true);
        
        button = componentsManager.get(ComponentsID.BT_PERF_EDIT_CONFIG, JButton.class);
        button.setEnabled(false);
        button = componentsManager.get(ComponentsID.BT_PERF_SAVE_CONFIG, JButton.class);
        button.setEnabled(true);
        button = componentsManager.get(ComponentsID.BT_PERF_CANCEL_CONFIG, JButton.class);
        button.setEnabled(true);
        
    }
    
}
