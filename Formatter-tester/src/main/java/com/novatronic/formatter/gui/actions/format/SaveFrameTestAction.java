/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui.actions.format;

import com.novatronic.formatter.gui.Datas;
import com.novatronic.formatter.gui.actions.Action;
import com.novatronic.formatter.gui.util.FileUtil;
import com.novatronic.formatter.tester.beans.FormatterTest;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JButton;
import org.apache.log4j.Logger;

/**
 *
 * @author Omar
 */
public class SaveFrameTestAction extends Action<JButton> implements ActionListener {

    private static final Logger log = Logger.getLogger(SaveFrameTestAction.class);
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
    
    @Override
    public void subscribe(JButton component) {
        component.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        FormatterTest actualTest = Datas.getInstance().get(Datas.ACTUAL_TEST, FormatterTest.class);
        
        log.debug("Guardando datos de Trama Test...");
        String fileName = "[FRAME.TEST]"  + actualTest.getId() + "." 
                + actualTest.getFormatId() + "-" + DATE_FORMAT.format(new Date())
                + ".txt";
        if (FileUtil.SaveFrame(fileName, actualTest.getFrameTest())) {
            log.info("Trama Guardada en:" + fileName);
        } else {
            log.info("No fue posible guardar la trama en el archivo:" + fileName
                    + ", ver el log");
        }
    }
}
