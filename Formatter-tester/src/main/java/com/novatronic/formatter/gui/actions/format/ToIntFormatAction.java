/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui.actions.format;

import com.novatronic.formatter.exception.FormatterException;
import com.novatronic.formatter.gui.Components;
import com.novatronic.formatter.gui.ComponentsID;
import com.novatronic.formatter.gui.Datas;
import com.novatronic.formatter.gui.actions.Action;
import com.novatronic.formatter.gui.util.TreeUtil;
import com.novatronic.formatter.tester.beans.FormatterTest;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JTree;
import org.apache.log4j.Logger;

/**
 *
 * @author Omar
 */
public class ToIntFormatAction extends Action<JButton> implements ActionListener, Runnable {

    private static final Logger log = Logger.getLogger(ToIntFormatAction.class);
    private JTree treIntFrmGenerado = Components.getInstance().get(ComponentsID.TR_INTFORMAT_RESULT, JTree.class);
    private JButton button = Components.getInstance().get(ComponentsID.BT_TO_INTFORMAT, JButton.class);
    
    @Override
    public void subscribe(JButton component) {
        component.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        FormatterTest actualTest = Datas.getInstance().get(Datas.ACTUAL_TEST, FormatterTest.class);

        treIntFrmGenerado.setModel(null);
        try {
            log.info("Generando Formato Interno ...");
            button.setEnabled(false);
            actualTest.toIntFormat();
            treIntFrmGenerado.setModel(TreeUtil.getTreeModelFromFormatterTest(actualTest, true));
            button.setEnabled(true);
            nextState();
            log.info("Objeto generado");
        } catch (FormatterException ex) {
            log.error("Hubo un problema al generar el Objeto", ex);
            String message = (ex.getCause() == null) ? ex.getMessage() : ex.getCause().getMessage();
            log.info("Hubo un problema al generar el Objeto:" + message + ", se agrega el objeto incompleto");
            button.setEnabled(true);
            actualTest.setIntFormatGenerated(ex.getIntFormat());
            treIntFrmGenerado.setModel(TreeUtil.getTreeModelFromFormatterTest(actualTest, true));
            nextState();
        }
    }
}
