/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui.panels;

import com.novatronic.formatter.gui.Datas;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JPanel;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

/**
 *
 * @author Omar
 */
public class GraphPanel {

    public static final Logger log = Logger.getLogger(GraphPanel.class);
    
    public static JPanel buildMemoryPanel(){
        TimeSeries total = new TimeSeries("Memoria Disponible");
        TimeSeries used = new TimeSeries("Memory Usada");
        
        Datas.getInstance().add(Datas.MEMORY_DATA_USED, used);
        Datas.getInstance().add(Datas.MEMORY_DATA_TOTAL, total);
        
//        total.setMaximumItemAge(paramInt);
//        free.setMaximumItemAge(paramInt);
        
        TimeSeriesCollection localTimeSeriesCollection = new TimeSeriesCollection();
        localTimeSeriesCollection.addSeries(total);
        localTimeSeriesCollection.addSeries(used);
        
        DateAxis localDateAxis = new DateAxis("Tiempo");
        localDateAxis.setAutoRange(true);
        localDateAxis.setLowerMargin(0.0D);
        localDateAxis.setUpperMargin(0.0D);
        localDateAxis.setTickLabelsVisible(true);
        
        NumberAxis localNumberAxis = new NumberAxis("Memoria (MBytes)");
        
        
        XYLineAndShapeRenderer localXYLineAndShapeRenderer = new XYLineAndShapeRenderer(true, false);
        localXYLineAndShapeRenderer.setSeriesPaint(0, Color.red);
        localXYLineAndShapeRenderer.setSeriesPaint(1, Color.green);
        
        XYPlot localXYPlot = new XYPlot(localTimeSeriesCollection, localDateAxis, localNumberAxis, localXYLineAndShapeRenderer);
        
        
        JFreeChart localJFreeChart = new JFreeChart(null, new Font("SansSerif", 1, 24), localXYPlot, true);
        ChartPanel localChartPanel = new ChartPanel(localJFreeChart, true);
        
        return localChartPanel;
    }
    
    public static JPanel buildCPUPanel(){
        TimeSeries CPUusage = new TimeSeries("Uso de CPU");
        
        Datas.getInstance().add(Datas.CPU_USAGE, CPUusage);
        
//        CPUusage.setMaximumItemAge(paramInt);
        
        TimeSeriesCollection localTimeSeriesCollection = new TimeSeriesCollection();
        localTimeSeriesCollection.addSeries(CPUusage);
        
        DateAxis localDateAxis = new DateAxis("Tiempo");
        localDateAxis.setAutoRange(true);
        localDateAxis.setLowerMargin(0.0D);
        localDateAxis.setUpperMargin(0.0D);
        localDateAxis.setTickLabelsVisible(true);
        
        NumberAxis localNumberAxis = new NumberAxis("Uso de CPU (%)");
        
        
        XYLineAndShapeRenderer localXYLineAndShapeRenderer = new XYLineAndShapeRenderer(true, false);
        localXYLineAndShapeRenderer.setSeriesPaint(0, Color.red);
        
        XYPlot localXYPlot = new XYPlot(localTimeSeriesCollection, localDateAxis, localNumberAxis, localXYLineAndShapeRenderer);
        
        
        JFreeChart localJFreeChart = new JFreeChart(null, new Font("SansSerif", 1, 24), localXYPlot, true);
        ChartPanel localChartPanel = new ChartPanel(localJFreeChart, true);
        
        return localChartPanel;
    }
}
