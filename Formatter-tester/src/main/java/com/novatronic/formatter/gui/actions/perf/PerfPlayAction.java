/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui.actions.perf;

import com.novatronic.formatter.gui.Components;
import com.novatronic.formatter.gui.ComponentsID;
import com.novatronic.formatter.gui.Datas;
import com.novatronic.formatter.gui.actions.Action;
import com.novatronic.formatter.gui.perf.PerfRunnable;
import com.novatronic.formatter.tester.beans.FormatterTest;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JButton;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.jfree.data.time.TimeSeries;

/**
 *
 * @author Omar
 */
public class PerfPlayAction extends Action<JButton> implements ActionListener {

    private static final Logger log = Logger.getLogger(PerfPlayAction.class);
    private JButton btPerfStartTest = Components.getInstance().get(ComponentsID.BT_PERF_PLAY, JButton.class);
    private JButton btPerfStopTest = Components.getInstance().get(ComponentsID.BT_PERF_STOP, JButton.class);
    private JButton btPerfReport = Components.getInstance().get(ComponentsID.BT_PERF_REPORT, JButton.class);
    private JButton btPerfClean = Components.getInstance().get(ComponentsID.BT_PERF_CLEAN_TEST, JButton.class);
    private int threadsNumber;
    private int execPerThread;
    private String direction;
    private FormatterTest formatterTest;
    private ExecutorService execService;

    public PerfPlayAction() {
        execService = Executors.newCachedThreadPool();
        Datas.getInstance().add(Datas.PERF_SERVICE, execService);
    }

    @Override
    public void subscribe(JButton component) {
        component.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Thread exec;
        exec = new Thread(){

            @Override
            public void run() {
                TimeSeries menSerie;
                TimeSeries cpuSerie;
                Integer memCount;
                Integer cpuCount;

                btPerfStartTest.setEnabled(false);
                btPerfStopTest.setEnabled(true);
                btPerfReport.setEnabled(false);
                btPerfClean.setEnabled(false);

                menSerie = Datas.getInstance().get(Datas.MEMORY_DATA_USED, TimeSeries.class);
                cpuSerie = Datas.getInstance().get(Datas.CPU_USAGE, TimeSeries.class);
                memCount = menSerie.getItemCount();
                cpuCount = cpuSerie.getItemCount();
                threadsNumber = Integer.parseInt((String) Datas.getInstance().get(Datas.PERF_THREADS_NUMBER));
                execPerThread = Integer.parseInt((String) Datas.getInstance().get(Datas.PERF_EXEC_PER_THREAD));
                formatterTest = Datas.getInstance().get(Datas.ACTUAL_TEST, FormatterTest.class);
                direction = Datas.getInstance().get(Datas.PERF_EXEC_DIRECTION, String.class);

                LogMF.info(log, "Memory.Init={0}, CPU.Init={1}", memCount, cpuCount);

                Datas.getInstance().add(Datas.PERF_MEM_INIT_POS, memCount);
                Datas.getInstance().add(Datas.PERF_CPU_INIT_POS, cpuCount);
                Datas.getInstance().add(Datas.PERF_IS_PLAY, true);

                log.info("Ejecutando Test de Performance");
                Datas.getInstance().add(Datas.PERF_TIME_INIT, new Date());
                execPerformanceTest();
                Datas.getInstance().add(Datas.PERF_TIME_END, new Date());
                
                memCount = menSerie.getItemCount();
                cpuCount = cpuSerie.getItemCount();
                LogMF.info(log, "Memory.End={0}, CPU.End={1}", memCount, cpuCount);
                
                Datas.getInstance().add(Datas.PERF_MEM_END_POS, memCount);
                Datas.getInstance().add(Datas.PERF_CPU_END_POS, cpuCount);
                Datas.getInstance().add(Datas.PERF_IS_PLAY, false);

                btPerfStartTest.setEnabled(true);
                btPerfStopTest.setEnabled(false);
                btPerfReport.setEnabled(true);
                btPerfClean.setEnabled(true);
            }
        };
        exec.start();
    }

    private void execPerformanceTest() {
        PerfRunnable perfRun;
        List<List<Long>> results;
        CountDownLatch doneSignal;

        doneSignal = new CountDownLatch(threadsNumber);
        results = new ArrayList<List<Long>>();
        Datas.getInstance().add(Datas.PERF_RESULTS, results);
        for (int i = 0; i < threadsNumber; i++) {
            perfRun = new PerfRunnable(doneSignal, execPerThread, direction, formatterTest);
            execService.execute(perfRun);
        }
        try {
            log.info("Esperando por el fin de las ejecuciones...");
            doneSignal.await();
            log.info("Todos los hilos terminaron su ejecucion");
        } catch (InterruptedException ex) {
            log.error("Ejecucion interrumpida", ex);
        }
    }
}
