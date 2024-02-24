/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui.actions.tools;

import com.novatronic.formatter.gui.actions.load.LoadTestAction;
import com.novatronic.formatter.gui.ComponentsID;
import com.novatronic.formatter.gui.Components;
import com.novatronic.formatter.gui.actions.Action;
import com.novatronic.formatter.test.LogToXml;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JTextArea;
import org.apache.log4j.Logger;

/**
 *
 * @author Omar
 */
public class LogToXmlAction extends Action<JButton> implements ActionListener {

    private static final Logger log = Logger.getLogger(LoadTestAction.class);

    @Override
    public void subscribe(JButton component) {
        component.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String result;
        JTextArea taIntFormatLog = Components.getInstance().get(ComponentsID.TA_INTFORMAT_LOG, JTextArea.class);
        JTextArea taIntFormatXml = Components.getInstance().get(ComponentsID.TA_INTFORMAT_XML, JTextArea.class);
        
        result = taIntFormatLog.getText().trim();
        if(result.isEmpty()){
            log.info("No se tienen datos por convertir");
            return;
        }
        log.info("Realizando conversion de la entrada...");
        result = LogToXml.toXml(result);
        taIntFormatXml.setText(result);
        log.info("Conversion terminada");
    }
    
}
