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
import com.novatronic.formatter.tester.beans.FormatterTest;
import com.novatronic.formatter.tester.beans.TestSuite;
import com.novatronic.formatter.tester.reader.TestSuiteBuilder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import javax.swing.*;
import org.apache.log4j.Logger;

/**
 *
 * @author Omar
 */
public class LoadTestGroupAction extends Action<JButton> implements ActionListener, Runnable {

    private static final Logger log = Logger.getLogger(LoadTestsFileAction.class);

    /**
     * //TODO refactorizar
     * @param e 
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void subscribe(JButton component) {
        component.addActionListener(this);
    }

    @Override
    public void run() {
        TestSuite testSuite;
        List<FormatterTest> actualTests;
        DefaultListModel listModel;
        String selecFile;
        log.debug("Leyendo archivo de Test");
        
        JComboBox testFiles = Components.getInstance().get(ComponentsID.CB_TESTS_FILES, JComboBox.class);
        JList tests = Components.getInstance().get(ComponentsID.LT_TEST_GROUP, JList.class);
        JTextField txFormatterConfig = Components.getInstance().get(ComponentsID.TF_TEST_FILE, JTextField.class);
        JButton button = Components.getInstance().get(ComponentsID.BT_LOAD_SELECT_FILE, JButton.class);
        
        button.setEnabled(false);
        tests.setModel(new DefaultListModel());
        selecFile = (String) testFiles.getSelectedItem();
        selecFile = Config.TESTS_DIR + File.separator + selecFile;
        log.info("Cargando test=" + selecFile + "...");

        try {
            /*1. Se crea el test suite para el archivo seleccionado*/
            testSuite = TestSuiteBuilder.newTestSuite(selecFile, Config.FORMATS_DIR);
            actualTests = testSuite.getTests();
            log.debug("Test Suite = " + testSuite);

            /*2. Se cargan los test disponible en el combo*/
            txFormatterConfig.setText(testSuite.getFormatConfigFileName());
            listModel = new DefaultListModel();
            for (FormatterTest formatterTest : actualTests) {
                listModel.addElement(formatterTest.getId());
                log.debug(formatterTest.getId());
            }
            tests.setModel(listModel);
            
            /*3. Guardamos los datos*/
            Datas.getInstance().add(Datas.TEST_GROUP, actualTests);
            Datas.getInstance().add(Datas.TEST_SUITE, testSuite);
            
            button.setEnabled(true);
            nextState();
            log.info("Grupo de Test cargado");

        } catch (Exception ex) {
            log.error("No se pudieron iniciar los test", ex);
            String message = (ex.getCause() == null) ? ex.getMessage() : ex.getCause().getMessage();
            log.info("No se pudieron iniciar los test:" + message);
        }
    }
}
