/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui.actions.format;

import com.novatronic.formatter.gui.Datas;
import com.novatronic.formatter.gui.actions.Action;
import com.novatronic.formatter.internal.InternalFormat;
import com.novatronic.formatter.tester.beans.FormatterTest;
import com.novatronic.formatter.test.CompareInternal;
import com.novatronic.formatter.test.CompareInternalResult;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import org.apache.log4j.Logger;

/**
 *
 * @author Omar
 */
public class CompareIFsAction extends Action<JButton> implements ActionListener {
    private static final Logger log = Logger.getLogger(CompareIFsAction.class);

    @Override
    public void subscribe(JButton component) {
        component.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        FormatterTest test = Datas.getInstance().get(Datas.ACTUAL_TEST, FormatterTest.class);
        InternalFormat IFExpected = test.getIntFormatExp();
        InternalFormat IFTested = test.getIntFormatGenerated();
        
        CompareInternalResult result = CompareInternal.compareInternalFormat(IFTested, IFExpected, null);
        log.info("Resultado [" + result.isSuccess() + "]");
        log.info("Descripcion [" + result.getReason() + "]");
        log.info("Campo [" + result.getFieldID() + "]");
        
        nextState();
    }
    
}
