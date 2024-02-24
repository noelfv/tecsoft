/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui.actions.tools;

import com.novatronic.formatter.gui.Components;
import com.novatronic.formatter.gui.ComponentsID;
import com.novatronic.formatter.gui.Config;
import com.novatronic.formatter.gui.actions.Action;
import com.novatronic.formatter.tester.reader.DirReader;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import org.apache.log4j.Logger;

/**
 *
 * @author Omar
 */
public class LoadFmtConfigAction extends Action<JButton> implements ActionListener {
    private static final Logger log = Logger.getLogger(LoadFmtConfigAction.class);
    @Override
    public void subscribe(JButton component) {
        component.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JComboBox fmtConfigs;
        JComboBox fmtIds;
        log.info("Leyendo directorio de Formatos...");
        fmtConfigs = Components.getInstance().get(ComponentsID.CB_FMTCFG, JComboBox.class);
        fmtIds = Components.getInstance().get(ComponentsID.CB_FMTID, JComboBox.class);
        
        fmtIds.removeAllItems();
        fmtConfigs.removeAllItems();
        fmtConfigs.setModel(new DefaultComboBoxModel(
                DirReader.readDirectory(Config.FORMATS_DIR, "")));
        
        log.info("Directorio leido");
    }
    
}
