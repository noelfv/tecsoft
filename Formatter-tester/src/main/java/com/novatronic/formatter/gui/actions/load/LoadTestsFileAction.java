/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui.actions.load;

import com.novatronic.formatter.gui.Components;
import com.novatronic.formatter.gui.ComponentsID;
import com.novatronic.formatter.gui.Config;
import com.novatronic.formatter.gui.Datas;
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
public class LoadTestsFileAction extends Action<JButton> implements ActionListener{
    private static final Logger log = Logger.getLogger(LoadTestsFileAction.class);
    
    @Override
    public void subscribe(JButton component) {
        component.addActionListener(this);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        log.info("Leyendo directorio de test...");
        JComboBox testFiles = Components.getInstance().get(ComponentsID.CB_TESTS_FILES, JComboBox.class);
        
        testFiles.removeAllItems();
        testFiles.setModel(new DefaultComboBoxModel(
                DirReader.readDirectory(Config.TESTS_DIR, Config.TESTFILE_PREFIX)));
        
        //Borrando si algun test anterior existio
        Datas.getInstance().remove(Datas.ACTUAL_TEST);
        
        nextState();
        log.info("Directorio leido");
    }
}
