/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui.actions.support;

import com.novatronic.formatter.gui.actions.Action;
import com.novatronic.formatter.gui.util.ValidateUtil;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JTextField;
import org.apache.log4j.Logger;

/**
 *
 * @author Omar
 */
public class ValidateNumberAction extends Action<JTextField> implements KeyListener {
    private static Logger log = Logger.getLogger(ValidateNumberAction.class);
    private static final String EMPTY = "";
    
    @Override
    public void subscribe(JTextField component) {
        component.addKeyListener(this);
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {
        JTextField text = (JTextField)e.getComponent();
        String textValue;
        textValue = text.getText();
        
        if(textValue.equals(EMPTY)){
            return;
        }
        
        if (!ValidateUtil.isNumber(textValue)){
            textValue = text.getText();
            textValue = textValue.substring(0, textValue.length()-1);
            text.setText(textValue);
        }
        
    }
    
}
