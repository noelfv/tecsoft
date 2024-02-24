/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui.actions.format;

import com.novatronic.formatter.exception.FormatterException;
import com.novatronic.formatter.gui.ComponentsID;
import com.novatronic.formatter.gui.Components;
import com.novatronic.formatter.gui.Datas;
import com.novatronic.formatter.gui.actions.Action;
import com.novatronic.formatter.tester.beans.FormatterTest;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JTextArea;
import org.apache.log4j.Logger;

/**
 *
 * @author Omar
 */
public class ToFrameAction extends Action<JButton> implements ActionListener, Runnable {

    private static final Logger log = Logger.getLogger(ToFrameAction.class);

    @Override
    public void subscribe(JButton component) {
        component.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        String frame;
        FormatterTest actualTest = Datas.getInstance().get(Datas.ACTUAL_TEST, FormatterTest.class);
        JTextArea taTramaGenerada = Components.getInstance().get(ComponentsID.TA_FRAME_RESULT, JTextArea.class);
        JButton button = Components.getInstance().get(ComponentsID.BT_TO_FRAME, JButton.class);

        try {
            log.info("Creando la Trama...");
            button.setEnabled(false);
            frame = "[" + actualTest.toFrame() + "]";
            taTramaGenerada.setText(frame);
            
            button.setEnabled(true);
            nextState();
            log.info("Trama generada");
        } catch (FormatterException ex) {
            log.error("Hubo un problema al generar la trama", ex);
            String message = (ex.getCause() == null) ? ex.getMessage() : ex.getCause().getMessage();
            log.info("Hubo un problema al generar la trama:" + message + ", se agrega la trama incompleta");
            button.setEnabled(true);
            frame = "[" + ex.getFrame().toString() + "|...";
            taTramaGenerada.setText(frame);
            nextErrorState();
        }
    }
}
