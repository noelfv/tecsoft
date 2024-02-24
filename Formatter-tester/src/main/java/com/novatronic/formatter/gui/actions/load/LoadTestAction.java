/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui.actions.load;

import com.novatronic.formatter.gui.Components;
import com.novatronic.formatter.gui.ComponentsID;
import com.novatronic.formatter.gui.Datas;
import com.novatronic.formatter.gui.actions.Action;
import com.novatronic.formatter.gui.util.TreeUtil;
import com.novatronic.formatter.tester.beans.FormatterTest;
import com.novatronic.formatter.tester.beans.TestSuite;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import org.apache.log4j.Logger;

/**
 *
 * @author Omar
 */
public class LoadTestAction extends Action<JButton> implements ActionListener {

    private static final Logger log = Logger.getLogger(LoadTestAction.class);
    private static final String EMPTY = "";
    private static final int ITEM_NOT_SELECTED = -1;
    private JList listTest = Components.getInstance().get(ComponentsID.LT_TEST_GROUP, JList.class);
    private JTextArea taTramaTest = Components.getInstance().get(ComponentsID.TA_FRAME_TEST, JTextArea.class);
    private JTree treIntFrmTest = Components.getInstance().get(ComponentsID.TR_INTFORMAT_TEST, JTree.class);
    private JTextField txFormatterId = Components.getInstance().get(ComponentsID.TF_FORMAT_ID, JTextField.class);
    private JTextArea taTestDescription = Components.getInstance().get(ComponentsID.TA_TEST_DESC, JTextArea.class);
    private TestSuite testSuite;

    @Override
    public void subscribe(JButton component) {
        component.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int indexTest;
        FormatterTest actualTest;

        //Mostrando informacion del Test
        indexTest = listTest.getSelectedIndex();
        if (indexTest == ITEM_NOT_SELECTED) {
            log.info("No se ha seleccionado ningun test");
            return;
        }
        testSuite = Datas.getInstance().get(Datas.TEST_SUITE, TestSuite.class);
        actualTest = testSuite.getTest(indexTest);
        txFormatterId.setText(actualTest.getFormatId());
        taTestDescription.setText(actualTest.getDescription());

        //Mostrando Trama Test
        taTramaTest.setText(EMPTY);
        taTramaTest.setText("[" + actualTest.getFrameTest().toString() + "]");

        //Mostrando Formato Interno Test
        treIntFrmTest.setModel(null);
        treIntFrmTest.setModel(TreeUtil.getTreeModelFromFormatterTest(actualTest, false));

        //Guardando la data
        Datas.getInstance().add(Datas.ACTUAL_TEST, actualTest);

        nextState();
        log.info("Trama y Objeto de pruebas cargados en la interfaz");
    }
}
