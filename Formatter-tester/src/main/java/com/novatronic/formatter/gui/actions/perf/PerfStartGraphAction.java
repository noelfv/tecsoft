/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui.actions.perf;

import com.novatronic.formatter.gui.ComponentsID;
import com.novatronic.formatter.gui.Components;
import com.novatronic.formatter.gui.Datas;
import com.novatronic.formatter.gui.actions.Action;
import com.novatronic.formatter.gui.perf.GraphRefreshTask;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import javax.swing.JButton;
import org.apache.log4j.Logger;

/**
 *
 * @author Omar
 */
public class PerfStartGraphAction extends Action<JButton> implements ActionListener {

    private static final Logger log = Logger.getLogger(PerfStartGraphAction.class);
    private Timer timer;
    private JButton btPerfStartGraph = Components.getInstance().get(ComponentsID.BT_PERF_START_GRAPH, JButton.class);
    private JButton btPerfStopGraph = Components.getInstance().get(ComponentsID.BT_PERF_STOP_GRAPH, JButton.class);
    private JButton btPerfStartTest = Components.getInstance().get(ComponentsID.BT_PERF_PLAY, JButton.class);
    private JButton btPerfReport = Components.getInstance().get(ComponentsID.BT_PERF_REPORT, JButton.class);
    private JButton btPerfClean = Components.getInstance().get(ComponentsID.BT_PERF_CLEAN_TEST, JButton.class);
    //private JButton btPerfStopTest = Components.getInstance().get(ComponentsID.BT_PERF_STOP, JButton.class);
    private GraphRefreshTask graphRefreshTask;
    private static final long DELAY_GRAPH = 200L;

    @Override
    public void subscribe(JButton component) {
        component.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String refreshValue;
        long refresh;
        Datas dataManager = Datas.getInstance();

        btPerfStartGraph.setEnabled(false);
        btPerfStopGraph.setEnabled(true);
        btPerfStartTest.setEnabled(true);
        btPerfReport.setEnabled(false);
        btPerfClean.setEnabled(false);

        refreshValue = dataManager.get(Datas.PERF_GRAPH_REFRESH, String.class);
        refresh = Long.parseLong(refreshValue);

        timer = new Timer();
        graphRefreshTask = new GraphRefreshTask();
        timer.schedule(graphRefreshTask, DELAY_GRAPH, refresh);
        dataManager.add(Datas.PERF_TIMER_GRAPH, timer);

        log.info("Grafica iniciada");
    }
}
