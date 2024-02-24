/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui.states;

import com.novatronic.formatter.gui.ComponentsID;
import com.novatronic.formatter.gui.Components;
import javax.swing.JButton;
import org.apache.log4j.Logger;

/**
 *
 * @author Omar
 */
public class ToIntFormatState extends State{
    private static final Logger log = Logger.getLogger(ToIntFormatState.class);

    @Override
    public void execute() {
        JButton button;
        Components componentsManager = Components.getInstance();
        
        button = componentsManager.get(ComponentsID.BT_COMPARE_INTFORMAT, JButton.class);
        button.setEnabled(true);
        
        log.debug("Estado ToIntFormatState ejecutado");
    }
    
}
