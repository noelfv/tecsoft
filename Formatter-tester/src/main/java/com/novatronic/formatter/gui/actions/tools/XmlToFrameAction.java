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
import com.novatronic.formatter.gui.util.UnParse;
import com.novatronic.formatter.internal.InternalFormat;
import com.novatronic.formatter.test.IntFormatReader;
import com.novatronic.formatter.test.ParseValue;
import com.novatronic.formatter.util.VariableByteBuffer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Reader;
import java.io.StringReader;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextArea;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 *
 * @author Omar
 */
public class XmlToFrameAction extends Action<JButton> implements ActionListener {

    private static final Logger log = Logger.getLogger(XmlToFrameAction.class);

    @Override
    public void subscribe(JButton component) {
        component.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JComboBox fmtIds;
        JTextArea taIntFormatXml;
        JTextArea taFrame;
        String fmtId;
        String xmlIntFmt;
        FormatterFactory factory;
        InternalFormat intFmt;
        VariableByteBuffer buffer;

        taIntFormatXml = Components.getInstance().get(ComponentsID.TA_FMTXML, JTextArea.class);
        taFrame = Components.getInstance().get(ComponentsID.TA_FRAME, JTextArea.class);
        xmlIntFmt = taIntFormatXml.getText();
        if (xmlIntFmt.isEmpty()) {
            log.info("No se tiene XML por convertir a FI");
        } else {
            fmtIds = Components.getInstance().get(ComponentsID.CB_FMTID, JComboBox.class);
            if (fmtIds.getSelectedIndex() >= 0) {
                fmtId = (String) fmtIds.getSelectedItem();
                factory = Datas.getInstance().get("LOG_FACTORY", FormatterFactory.class);
                intFmt = readXmlString(xmlIntFmt);
                if (intFmt != null) {
                    buffer = factory.getFormatter(fmtId).getFrames(intFmt);
                    log.info("Trama creada de longitud=[" + buffer.getLength() + "]");
                    taFrame.setText(UnParse.unparse(buffer.getByteArray()));
                }
            } else {
                log.warn("No se ha seleccionado un Id de formato");
            }
        }
    }

    private InternalFormat readXmlString(String xmlBody) {
        String header;
        String xml;
        Reader in;
        Document doc;
        Element root;

        header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        xml = header + xmlBody;
        in = new StringReader(xml);
        try {
            SAXBuilder builder = new SAXBuilder();
            doc = builder.build(in);
            root = doc.getRootElement();
            return IntFormatReader.fromDom(root);
        } catch (Exception ex) {
            log.error("No fue posible leer el FI XML", ex);
            return null;
        }
    }
}
