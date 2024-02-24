/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui.actions.perf;

import com.novatronic.formatter.gui.ComponentsID;
import com.novatronic.formatter.gui.Components;
import com.novatronic.formatter.gui.Datas;
import com.novatronic.formatter.gui.actions.Action;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import org.apache.log4j.Logger;

/**
 *
 * @author Omar
 */
public class PerfCleanAction extends Action<JButton> implements ActionListener  {
    private static final Logger log = Logger.getLogger(PerfCleanAction.class);
    
    @Override
    public void subscribe(JButton component) {
        component.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JButton button;
        boolean existData;
        Components componentsManager = Components.getInstance();
        Datas dataManager = Datas.getInstance();
        
        //Aca limpiamos los datos
        button = componentsManager.get(ComponentsID.BT_PERF_REPORT, JButton.class);
        button.setEnabled(false);
        button = componentsManager.get(ComponentsID.BT_PERF_CLEAN_TEST, JButton.class);
        button.setEnabled(false);
        dataManager.add(Datas.PERF_EXIST_DATA, false);
        log.info("Datos del Test limpiados");
        
    }
    
}
