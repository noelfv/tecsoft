/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui.actions.perf;

import com.novatronic.formatter.gui.actions.Action;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import org.apache.log4j.Logger;

/**
 *
 * @author Omar
 */
public class SavePerfConfigAction extends Action<JButton> implements ActionListener {
    private static final Logger log = Logger.getLogger(SavePerfConfigAction.class);
    
    @Override
    public void subscribe(JButton component) {
        component.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
