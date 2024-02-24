/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui.actions.tester;

import com.novatronic.formatter.gui.ComponentsID;
import com.novatronic.formatter.gui.Components;
import com.novatronic.formatter.gui.actions.Action;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import javax.swing.JTextArea;
import org.apache.log4j.Logger;

/**
 *
 * @author Omar
 */
public class CleanLogAction extends Action<JMenuItem> implements ActionListener{
    private static final Logger log = Logger.getLogger(CleanLogAction.class);
    private static final String EMPTY = "";
    
    @Override
    public void subscribe(JMenuItem component) {
        component.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JTextArea logTest = Components.getInstance().get(ComponentsID.TA_TEST_LOG, JTextArea.class);
        
        logTest.setText(EMPTY);
    }
    
}
