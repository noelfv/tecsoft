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
public class ToFrameState extends State{
    private static final Logger log = Logger.getLogger(ToFrameState.class);
    
    @Override
    public void execute() {
        JButton button;
        Components componentsManager = Components.getInstance();
        
        button = componentsManager.get(ComponentsID.BT_SAVE_FRAME_RESULT, JButton.class);
        button.setEnabled(true);
        button = componentsManager.get(ComponentsID.BT_COMPARE_FRAME, JButton.class);
        button.setEnabled(true);
        
        log.debug("Estado ToFrameState ejecutado");
    }
    
}
