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
import java.util.Timer;
import javax.swing.JButton;
import org.apache.log4j.Logger;
import org.jfree.data.time.TimeSeries;

/**
 *
 * @author Omar
 */
public class PerfStopGraphAction extends Action<JButton> implements ActionListener {
    private static final Logger log = Logger.getLogger(PerfStopGraphAction.class);
    private JButton btPerfStartGraph = Components.getInstance().get(ComponentsID.BT_PERF_START_GRAPH, JButton.class);
    private JButton btPerfStopGraph = Components.getInstance().get(ComponentsID.BT_PERF_STOP_GRAPH, JButton.class);
    private JButton btPerfStartTest = Components.getInstance().get(ComponentsID.BT_PERF_PLAY, JButton.class);
    private JButton btPerfStopTest = Components.getInstance().get(ComponentsID.BT_PERF_STOP, JButton.class);
    
    @Override
    public void subscribe(JButton component) {
        component.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Timer timer;
        TimeSeries data;
        
        log.debug("Ejecutando stop...");
        btPerfStartGraph.setEnabled(true);
        btPerfStopGraph.setEnabled(false);
        btPerfStartTest.setEnabled(false);
        btPerfStopTest.setEnabled(false);

        timer = Datas.getInstance().get(Datas.PERF_TIMER_GRAPH, Timer.class);
        timer.cancel();
        data = Datas.getInstance().get(Datas.MEMORY_DATA_TOTAL, TimeSeries.class);
        data.clear();
        data = Datas.getInstance().get(Datas.MEMORY_DATA_USED, TimeSeries.class);
        data.clear();
        data = Datas.getInstance().get(Datas.CPU_USAGE, TimeSeries.class);
        data.clear();
        log.info("Gráficas detenidas");
        
    }
    
}
