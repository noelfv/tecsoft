/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui.perf;

import com.novatronic.formatter.gui.Datas;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;

/**
 *
 * @author Omar
 */
public class GraphRefreshTask extends TimerTask{
    private static final Logger log = Logger.getLogger(GraphRefreshTask.class);
    
    private TimeSeries total;
    private TimeSeries used;
    private double usedMemory;
    private double totalMemory;
    private double freeMemory;
    private int availableProcessors;
    
    private TimeSeries cpuUsage;
    private ThreadMXBean threadMXBean;
    private long currentTimeAsked;
    private long currentTimeCpuUsage;
    private long realTimePeriod;
    private long lastTimeAsked;
    private long lastTimeCpuUsage;
    private double cpuPercentUsage;
    private List<Long> threadCpuUsage;
    private long[] allThreadIds;
    
    private static final double MEGABYTES = 1048576d;
    
    public GraphRefreshTask(){
        total = Datas.getInstance().get(Datas.MEMORY_DATA_TOTAL, TimeSeries.class);
        used = Datas.getInstance().get(Datas.MEMORY_DATA_USED, TimeSeries.class);
        cpuUsage = Datas.getInstance().get(Datas.CPU_USAGE, TimeSeries.class);
        threadMXBean = ManagementFactory.getThreadMXBean();
        
        threadCpuUsage = new ArrayList<Long>();
    }
    
    @Override
    public void run() {
        checkMemoryUsage();
        checkCpuUsage();
    }
    
    private void checkMemoryUsage() {
        totalMemory = new Long(Runtime.getRuntime().totalMemory()).doubleValue();
        freeMemory = new Long(Runtime.getRuntime().freeMemory()).doubleValue();
        
        usedMemory = totalMemory - freeMemory;
        LogMF.trace(log,"BYTES:totalMemory={0},usedMemory={1}",totalMemory, usedMemory);
        usedMemory = usedMemory/MEGABYTES;
        totalMemory = totalMemory/MEGABYTES;
        LogMF.trace(log,"MBYTES:totalMemory={0},usedMemory={1}",totalMemory, usedMemory);
        
        total.add(new Millisecond(), totalMemory);
        used.add(new Millisecond(), usedMemory);
    }

    private void checkCpuUsage() {
        log.debug("-------------- check ENTER");
        String info = "";
        currentTimeAsked = System.nanoTime();
        availableProcessors = Runtime.getRuntime().availableProcessors();
        allThreadIds = threadMXBean.getAllThreadIds();
        currentTimeCpuUsage = 0;
        for (long id : allThreadIds) {
            currentTimeCpuUsage += threadMXBean.getThreadCpuTime(id);
            if(log.isTraceEnabled()){ //Encadenando la informacion de hilos
                info += threadMXBean.getThreadInfo(id).getThreadName() + ",time=" + currentTimeCpuUsage + "|";
            }
        }
        if(log.isTraceEnabled()){ //Mostrando la informacion de hilos
            info= info.replace("\n", "");
            log.trace(info);
        }
        
        if(threadCpuUsage.isEmpty()){
            threadCpuUsage.add(currentTimeCpuUsage);
            cpuUsage.add(new Millisecond(), 0);
        }else{
            realTimePeriod = currentTimeAsked - lastTimeAsked;
            lastTimeCpuUsage = threadCpuUsage.get(threadCpuUsage.size()-1);
            cpuPercentUsage = 100d*(currentTimeCpuUsage - lastTimeCpuUsage)/(realTimePeriod*availableProcessors);
            cpuUsage.add(new Millisecond(), cpuPercentUsage);
        }
        lastTimeAsked = currentTimeAsked;
        threadCpuUsage.add(currentTimeCpuUsage);
        
        LogMF.trace(log, "-------------- check OUT:percent={0}, last.usage={1},current.usage={2}, nProcessors={3}", 
                cpuPercentUsage, lastTimeCpuUsage,currentTimeCpuUsage, availableProcessors);
    }
}
