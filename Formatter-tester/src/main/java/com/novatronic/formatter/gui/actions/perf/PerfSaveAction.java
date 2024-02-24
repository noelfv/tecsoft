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
import javax.swing.JComboBox;
import javax.swing.JTextField;
import org.apache.log4j.Logger;

/**
 *
 * @author Omar
 */
public class PerfSaveAction extends Action<JButton> implements ActionListener {

    private static Logger log = Logger.getLogger(PerfSaveAction.class);
    private JTextField txPerfGraphrefresh = Components.getInstance().get(ComponentsID.TF_PERF_GRAPH_REFRESH, JTextField.class);
    private JTextField txPerfThreadSize = Components.getInstance().get(ComponentsID.TF_PERF_THREAD_SIZE, JTextField.class);
    private JTextField txPerfExecByThread = Components.getInstance().get(ComponentsID.TF_PERF_EXEC_BY_THREAD, JTextField.class);
    private JTextField txPerfThreadDelay = Components.getInstance().get(ComponentsID.TF_PERF_THREAD_DELAY, JTextField.class);
    private JComboBox cbPerfExecDirect = Components.getInstance().get(ComponentsID.CB_DIRECTION_EXEC, JComboBox.class);
    private JButton button;
    private String grapRefreshValue;
    private String threadsNumber;
    private String execPerThread;
    private String delayThread;
    private String execDirection;
    private boolean isSavable = true;
    private Datas dataManager = Datas.getInstance();
    private Components componentsManager = Components.getInstance();

    @Override
    public void subscribe(JButton component) {
        component.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        grapRefreshValue = txPerfGraphrefresh.getText();
        threadsNumber = txPerfThreadSize.getText();
        execPerThread = txPerfExecByThread.getText();
        delayThread = txPerfThreadDelay.getText();
        execDirection = (String)cbPerfExecDirect.getSelectedItem();
        
        if (isSavable()) {
            dataManager.add(Datas.PERF_GRAPH_REFRESH, grapRefreshValue);
            dataManager.add(Datas.PERF_THREADS_NUMBER, threadsNumber);
            dataManager.add(Datas.PERF_EXEC_PER_THREAD, execPerThread);
            dataManager.add(Datas.PERF_THREAD_DELAY, delayThread);
            dataManager.add(Datas.PERF_EXEC_DIRECTION, execDirection);
            log.info("La configuración ha sido guardada");
            
            txPerfGraphrefresh.setEditable(false);
            txPerfThreadSize.setEditable(false);
            txPerfExecByThread.setEditable(false);
            txPerfThreadDelay.setEditable(false);
            cbPerfExecDirect.setEnabled(false);
            
            button = componentsManager.get(ComponentsID.BT_PERF_EDIT_CONFIG, JButton.class);
            button.setEnabled(true);
            button = componentsManager.get(ComponentsID.BT_PERF_SAVE_CONFIG, JButton.class);
            button.setEnabled(false);
            button = componentsManager.get(ComponentsID.BT_PERF_CANCEL_CONFIG, JButton.class);
            button.setEnabled(false);
        }
    }
    
    private boolean isSavable(){
        isSavable = true;
        if (grapRefreshValue.isEmpty()) {
            log.warn("El tiempo de actualización de las gráficas está vacío");
            isSavable = false;
        } else if (threadsNumber.isEmpty()) {
            log.warn("La cantidad de hilos está vacío");
            isSavable = false;
        } else if (execPerThread.isEmpty()) {
            log.warn("Las ejecuciones por hilo está vacío");
            isSavable = false;
        } else if (delayThread.isEmpty()) {
            log.warn("El tiempo entre hilos está vacío");
            isSavable = false;
        }
        
        return isSavable;
    }
}
