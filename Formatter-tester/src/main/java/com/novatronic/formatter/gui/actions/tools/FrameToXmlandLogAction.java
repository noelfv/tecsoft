/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui.actions.tools;

import com.novatronic.formatter.FormatterFactory;
import com.novatronic.formatter.gui.Components;
import com.novatronic.formatter.gui.ComponentsID;
import com.novatronic.formatter.gui.Datas;
import com.novatronic.formatter.gui.actions.Action;
import com.novatronic.formatter.internal.InternalFormat;
import com.novatronic.formatter.test.LogToXml;
import com.novatronic.formatter.test.ParseValue;
import com.novatronic.formatter.util.VariableByteBuffer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextArea;
import org.apache.log4j.Logger;

/**
 *
 * @author ofernandez
 */
public class FrameToXmlandLogAction extends Action<JButton> implements ActionListener {

    private static final Logger log = Logger.getLogger(FrameToXmlandLogAction.class);

    @Override
    public void subscribe(JButton component) {
        component.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String fmtId;
        String fmtXmlLog;
        String fmtLog;
        JComboBox fmtIds;
        String frame;
        FormatterFactory factory;
        InternalFormat intFmt;
        JTextArea taIntFormatLog;
        JTextArea taIntFormatXml;
        JTextArea taFrame;
        VariableByteBuffer buffer;

        factory = Datas.getInstance().get("LOG_FACTORY", FormatterFactory.class);
        taIntFormatLog = Components.getInstance().get(ComponentsID.TA_FMTLOG, JTextArea.class);
        taIntFormatXml = Components.getInstance().get(ComponentsID.TA_FMTXML, JTextArea.class);
        taFrame = Components.getInstance().get(ComponentsID.TA_FRAME, JTextArea.class);
        fmtIds = Components.getInstance().get(ComponentsID.CB_FMTID, JComboBox.class);

        frame = taFrame.getText();
        if (frame.isEmpty()) {
            log.info("No se tiene Trama por convertir a XML y Log");
        } else {
            if (fmtIds.getSelectedIndex() >= 0) {
                fmtId = (String) fmtIds.getSelectedItem();
                buffer = ParseValue.parse(frame);
                log.info("Se ha leido [" + buffer.getLength() + "] bytes");
                intFmt = factory.getFormatter(fmtId)
                        .createInternalFormatFromFrame(buffer);
                fmtLog = intFmt.toString().trim();
                taIntFormatLog.setText(fmtLog);
                fmtXmlLog = LogToXml.toXml(fmtLog);
                taIntFormatXml.setText(fmtXmlLog);
            } else {
                log.warn("No se ha seleccionado un Id de formato");
            }
        }
    }
}
