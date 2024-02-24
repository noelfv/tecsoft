/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui.actions.perf;

import com.novatronic.formatter.gui.Datas;
import com.novatronic.formatter.gui.actions.Action;
import com.novatronic.formatter.gui.util.PerfReport;
import com.novatronic.formatter.tester.beans.FormatterTest;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JButton;
import org.apache.log4j.Logger;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesDataItem;

/**
 *
 * @author Omar
 */
public class PerfReportAction extends Action<JButton> implements ActionListener {

    private static Logger log = Logger.getLogger(PerfReportAction.class);
    
    private TimeSeries menUsed;
    private TimeSeries cpuUsage;
    private List<List<Long>> results;
    private Integer menInit;
    private Integer cpuInit;
    private Integer menEnd;
    private Integer cpuEnd;
    private List<Double> memResults;
    private List<Double> cpuResults;
    private List<Long> execResults;
    private Date dateInit;
    private Date dateEnd;
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

    @Override
    public void subscribe(JButton component) {
        component.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        log.info("Procesando Reporte de Performance...");
        menUsed = Datas.getInstance().get(Datas.MEMORY_DATA_USED, TimeSeries.class);
        cpuUsage = Datas.getInstance().get(Datas.CPU_USAGE, TimeSeries.class);
        results = (List<List<Long>>)Datas.getInstance().get(Datas.PERF_RESULTS);
        menInit = Datas.getInstance().get(Datas.PERF_MEM_INIT_POS, Integer.class);
        menEnd = Datas.getInstance().get(Datas.PERF_MEM_END_POS, Integer.class);
        cpuInit = Datas.getInstance().get(Datas.PERF_CPU_INIT_POS, Integer.class);
        cpuEnd = Datas.getInstance().get(Datas.PERF_CPU_END_POS, Integer.class);
        dateInit = Datas.getInstance().get(Datas.PERF_TIME_INIT, Date.class);
        dateEnd = Datas.getInstance().get(Datas.PERF_TIME_END, Date.class);
        
        memResults = timeSeriesToList(menUsed.getItems(),menInit,menEnd);
        cpuResults = timeSeriesToList(cpuUsage.getItems(),cpuInit,cpuEnd);
        execResults = new ArrayList<Long>();
        for (int i = 0; i < results.size(); i++) {
            execResults.addAll(results.get(i));
        }
        
        FormatterTest actualTest = Datas.getInstance().get(Datas.ACTUAL_TEST, FormatterTest.class);
        String fileName = "[REPORT]" + actualTest.getId() + "." 
                + actualTest.getFormatId() + "-" + DATE_FORMAT.format(new Date())
                + ".txt";
        PerfReport.createReport(fileName, memResults, cpuResults, execResults, dateInit, dateEnd);
        
    }
    
    private List<Double> timeSeriesToList(List<TimeSeriesDataItem> timeSeries, Integer init, Integer end){
        List<Double> result;
        
        result = new ArrayList<Double>();
        for (int i = init; i < end; i++) {
            result.add(timeSeries.get(i).getValue().doubleValue());
        }
        
        return result;
    }
}
