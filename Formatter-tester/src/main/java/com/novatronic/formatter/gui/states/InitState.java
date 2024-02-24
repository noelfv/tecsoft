/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui.states;

import com.novatronic.formatter.gui.ComponentsID;
import com.novatronic.formatter.gui.Components;
import com.novatronic.formatter.gui.Datas;
import javax.swing.*;
import org.apache.log4j.Logger;

/**
 *
 * @author Omar
 */
public class InitState extends State{
    private static final Logger log = Logger.getLogger(InitState.class);
    
    public static final String EMPTY = "";
    
    @Override
    public void execute() {
        JButton button;
        JList list;
        JTextArea textArea;
        JTextField text;
        JComboBox combo;
        JTree tree;
        Components componentsManager = Components.getInstance();
        
        button = componentsManager.get(ComponentsID.BT_LOAD_SELECT_FILE, JButton.class);
        button.setEnabled(false);
        button = componentsManager.get(ComponentsID.BT_SAVE_FRAME_RESULT, JButton.class);
        button.setEnabled(false);
        button = componentsManager.get(ComponentsID.BT_SAVE_FRAME_TEST, JButton.class);
        button.setEnabled(false);
        button = componentsManager.get(ComponentsID.BT_SELECT_TEST, JButton.class);
        button.setEnabled(false);
        button = componentsManager.get(ComponentsID.BT_TO_FRAME, JButton.class);
        button.setEnabled(false);
        button = componentsManager.get(ComponentsID.BT_TO_INTFORMAT, JButton.class);
        button.setEnabled(false);
        button = componentsManager.get(ComponentsID.BT_COMPARE_FRAME, JButton.class);
        button.setEnabled(false);
        button = componentsManager.get(ComponentsID.BT_COMPARE_INTFORMAT, JButton.class);
        button.setEnabled(false);
        
        list = componentsManager.get(ComponentsID.LT_TEST_GROUP, JList.class);
        list.setModel(new DefaultListModel()); 
        textArea = componentsManager.get(ComponentsID.TA_TEST_DESC, JTextArea.class);
        textArea.setText(EMPTY);
        text = componentsManager.get(ComponentsID.TF_TEST_FILE, JTextField.class);
        text.setText(EMPTY);
        text = componentsManager.get(ComponentsID.TF_FORMAT_ID, JTextField.class);
        text.setText(EMPTY);
        
        textArea = componentsManager.get(ComponentsID.TA_FRAME_RESULT, JTextArea.class);
        textArea.setText(EMPTY);
        textArea = componentsManager.get(ComponentsID.TA_FRAME_TEST, JTextArea.class);
        textArea.setText(EMPTY);
        
        tree = componentsManager.get(ComponentsID.TR_INTFORMAT_RESULT, JTree.class);
        tree.setModel(null);
        tree = componentsManager.get(ComponentsID.TR_INTFORMAT_TEST, JTree.class);
        tree.setModel(null);
        
        text = componentsManager.get(ComponentsID.TF_PERF_EXEC_BY_THREAD, JTextField.class);
        text.setText("100");
        Datas.getInstance().add(Datas.PERF_EXEC_PER_THREAD, "100");
        text = componentsManager.get(ComponentsID.TF_PERF_THREAD_SIZE, JTextField.class);
        text.setText("10");
        Datas.getInstance().add(Datas.PERF_THREADS_NUMBER, "10");
        text = componentsManager.get(ComponentsID.TF_PERF_THREAD_DELAY, JTextField.class);
        text.setText("10");
        Datas.getInstance().add(Datas.PERF_THREAD_DELAY, "10");
        text = componentsManager.get(ComponentsID.TF_PERF_GRAPH_REFRESH, JTextField.class);
        text.setText("1000");
        Datas.getInstance().add(Datas.PERF_GRAPH_REFRESH, "1000");
        combo = componentsManager.get(ComponentsID.CB_DIRECTION_EXEC, JComboBox.class);
        combo.setSelectedItem("FI a Trama");
        Datas.getInstance().add(Datas.PERF_EXEC_DIRECTION, "FI a Trama");
        Datas.getInstance().add(Datas.PERF_IS_PLAY, false);
        Datas.getInstance().add(Datas.PERF_EXIST_DATA, false);
        
        log.debug("Estado INIT ejecutado");
    }
    
}
