/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.generator;

import com.novatronic.generator.exception.FrameGeneratorXMLException;
import com.novatronic.generator.xml.ClientElement;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.Format.TextMode;
import org.jdom.output.XMLOutputter;

/**
 *
 * @author ofernandez
 * @version 1.0
 * @since 1.0, 12/11/2010
 */
public class SimpleFrameGenerator extends FrameGenerator{
    private static final Logger log = Logger.getLogger(SimpleFrameGenerator.class);

    private static final String ROOT = "transactions";
    private static final String REPORT_FILE_NAME = "transactions.xml";

    @Override
    protected void readCustomConfiguration(Element configuration) {
        /*Sin configuracion especial*/
    }

    /**
     * 
     */
    @Override
    public String generateFrames(){
        log.debug("Generando transacciones por cliente...");
        File file;
        
        try {
            Element root = new Element(ROOT);
            List<Client> clients = this.getClientsAsList();
            log.info("Generando tramas para clientes:" + clients);
            for (int i = 0; i < clients.size(); i++) {
                Client client = clients.get(i);
                root.addContent(new ClientElement(client));
                log.debug("Cliente agregado: "+ client);
            }

            /*Generando la salida*/
            file = new File(REPORT_FILE_NAME);
            Format format = Format.getPrettyFormat();
            format.setTextMode(TextMode.PRESERVE);
            XMLOutputter out = new XMLOutputter(format);
            FileOutputStream fileOS = new FileOutputStream(new File(REPORT_FILE_NAME));
            out.output(new Document(root), fileOS);
            fileOS.flush();
            fileOS.close();
            log.debug("Transacciones generadas en :" + file);
            return file.getAbsolutePath();
        } catch (Exception ex) {
            throw new FrameGeneratorXMLException("No fue posible generar las "
                    + "transacciones XML", ex);
        }
    }

    
}
