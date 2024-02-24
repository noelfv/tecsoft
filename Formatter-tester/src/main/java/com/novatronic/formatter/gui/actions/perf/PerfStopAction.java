/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui.actions.perf;

import com.novatronic.formatter.gui.Components;
import com.novatronic.formatter.gui.ComponentsID;
import com.novatronic.formatter.gui.Datas;
import com.novatronic.formatter.gui.actions.Action;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.jfree.data.time.TimeSeries;

/**
 *
 * @author Omar
 */
public class PerfStopAction extends Action<JButton> implements ActionListener {

    private static final Logger log = Logger.getLogger(PerfStopAction.class);
    private JButton btPerfStartTest = Components.getInstance().get(ComponentsID.BT_PERF_PLAY, JButton.class);
    private JButton btPerfStopTest = Components.getInstance().get(ComponentsID.BT_PERF_STOP, JButton.class);
    private JButton btPerfReport = Components.getInstance().get(ComponentsID.BT_PERF_REPORT, JButton.class);
    private JButton btPerfCleanReport = Components.getInstance().get(ComponentsID.BT_PERF_CLEAN_TEST, JButton.class);

    @Override
    public void subscribe(JButton component) {
        component.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        TimeSeries serie;
        Integer memEnd;
        Integer cpuEnd;
        
        log.debug("Ejecutando stop...");
        btPerfStartTest.setEnabled(true);
        btPerfStopTest.setEnabled(false);
        btPerfReport.setEnabled(true);
        btPerfCleanReport.setEnabled(true);
        
        serie = Datas.getInstance().get(Datas.MEMORY_DATA_USED, TimeSeries.class);
        memEnd = serie.getItemCount();
        serie = Datas.getInstance().get(Datas.CPU_USAGE, TimeSeries.class);
        cpuEnd = serie.getItemCount();
        
        LogMF.trace(log, "Memory.end={0}, CPU.end={1}", memEnd, cpuEnd);
        
        Datas.getInstance().add(Datas.PERF_IS_PLAY, false);
        Datas.getInstance().add(Datas.PERF_EXIST_DATA, true);
        log.info("Test de Performance detenido");

    }
}
