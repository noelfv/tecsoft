/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui.actions.tools;

import com.novatronic.formatter.FormatterFactory;
import com.novatronic.formatter.gui.Components;
import com.novatronic.formatter.gui.ComponentsID;
import com.novatronic.formatter.gui.Config;
import com.novatronic.formatter.gui.Datas;
import com.novatronic.formatter.gui.actions.Action;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 *
 * @author Omar
 */
public class LoadFmtIdsAction extends Action<JButton> implements ActionListener {

    private static final Logger log = Logger.getLogger(LoadFmtIdsAction.class);

    @Override
    public void subscribe(JButton component) {
        component.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JComboBox fmtConfigs;
        JComboBox fmtIds;
        String fmtConfig;
        FormatterFactory factory;
        File fmtCfgFile;

        fmtConfigs = Components.getInstance().get(ComponentsID.CB_FMTCFG, JComboBox.class);
        fmtIds = Components.getInstance().get(ComponentsID.CB_FMTID, JComboBox.class);
        fmtConfig = Config.FORMATS_DIR + File.separator + fmtConfigs.getSelectedItem();
        log.debug("Configuracion leida:" + fmtConfig + ", seleccionado=" + fmtConfigs.getSelectedIndex());
        fmtCfgFile = new File(fmtConfig);
        if (fmtConfigs.getSelectedIndex() >= 0) {
            log.info("Se eligio:" + fmtCfgFile);
            fmtIds.removeAllItems();
            fmtIds.setModel(new DefaultComboBoxModel(readFmtIdds(fmtCfgFile)));
            factory = new FormatterFactory(fmtCfgFile);
            Datas.getInstance().add("LOG_FACTORY", factory);
        }
    }

    private String[] readFmtIdds(File path) {
        Document doc;
        Element root;
        Element element;
        List<Element> elementIds;
        String[] ids;
        
        try {
            doc = (new SAXBuilder()).build(path);
            root = doc.getRootElement();
            element = root.getChild("formatters");
            elementIds = element.getChildren("formatter");
            ids = new String[elementIds.size()];
            for (int i = 0; i < elementIds.size(); i++) {
                element = elementIds.get(i);
                ids[i] = element.getAttributeValue("id");
            }
            return ids;
        } catch (Exception ex) {
            log.warn("No fue posible cargar el recurso:" + path + ", debido a:" + ex.getMessage());
            return new String[0];
        }
    }
}
