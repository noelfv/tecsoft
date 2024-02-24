/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui.actions.format;

import com.novatronic.formatter.gui.Datas;
import com.novatronic.formatter.gui.actions.Action;
import com.novatronic.formatter.tester.beans.FormatterTest;
import com.novatronic.formatter.test.CompareFrame;
import com.novatronic.formatter.test.CompareFrameResult;
import com.novatronic.formatter.util.VariableByteBuffer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import org.apache.log4j.Logger;

/**
 *
 * @author Omar
 */
public class CompareFramesAction extends Action<JButton> implements ActionListener {
    private static final Logger log = Logger.getLogger(CompareFramesAction.class);

    @Override
    public void subscribe(JButton component) {
        component.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        FormatterTest test = Datas.getInstance().get(Datas.ACTUAL_TEST, FormatterTest.class);
        VariableByteBuffer frameExpected = test.getFrameExp();
        VariableByteBuffer frameTested = test.getFrameGenerated();
        
        CompareFrameResult result = CompareFrame.compareFrames(frameTested, frameExpected);
        log.info("Resultado [" + result.getReason() + "]");
        log.info("Tamaño de Trama esperada [" + result.getExpectedSize() + "]");
        log.info("Tamaño de Trama prueba [" + result.getTestedSize() + "]");
        log.info("Posicion [" + result.getPosition() + "]");
        
        nextState();
    }
}
