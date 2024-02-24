/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui.perf;

import com.novatronic.formatter.gui.Config;
import com.novatronic.formatter.gui.Datas;
import com.novatronic.formatter.tester.beans.FormatterTest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

/**
 *
 * @author Omar
 */
public class PerfRunnable implements Runnable{
    private static final Logger log = Logger.getLogger(PerfRunnable.class);
    
    private CountDownLatch doneSignal;
    private int execPerThread;
    private String direction;
    private FormatterTest formatterTest;
    private List<Long> result;

    public PerfRunnable(CountDownLatch doneSignal, int execPerThread, String direction, FormatterTest formatterTest) {
        this.doneSignal = doneSignal;
        this.execPerThread = execPerThread;
        this.direction = direction;
        this.formatterTest = formatterTest;
        result = new ArrayList<Long>();
    }
    
    @Override
    public void run() {
        long timeInit;
        long timeEnd;
        
        LogMF.info(log, "Hilo:{0}- Ejecutando {1} veces, formato ID={2}, sentido={3}", 
                Thread.currentThread().getName(), execPerThread, 
                formatterTest.getFormat().getId(), direction);
        
        if(direction.equals(Config.PERF_DIREC_IF_TO_FRAME)){
            for (int i = 0; i < execPerThread; i++) {
                timeInit = System.nanoTime();
                formatterTest.toFrame();
                timeEnd = System.nanoTime();
                result.add(timeEnd - timeInit);
            }
        }else{
            for (int i = 0; i < execPerThread; i++) {
                timeInit = System.nanoTime();
                formatterTest.toIntFormat();
                timeEnd = System.nanoTime();
                result.add(timeEnd - timeInit);
            }
        }
        doneSignal.countDown();
        LogMF.info(log, "Hilo:{0}-Ejecucion terminada",Thread.currentThread().getName());
        
        LogMF.trace(log,"Resultados({0}):{1}", result.size(), result);
        updatePerfResults();
    }
    
    private void updatePerfResults(){
        List<List<Long>> results;
        
        results = (List<List<Long>>)Datas.getInstance().get(Datas.PERF_RESULTS);
        synchronized(PerfRunnable.class){
            results.add(result);
        }
        
        log.debug("Datos guardados");
    }
    
}
