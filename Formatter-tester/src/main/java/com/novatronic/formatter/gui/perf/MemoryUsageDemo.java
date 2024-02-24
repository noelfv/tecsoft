/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui.perf;

import info.clearthought.layout.TableLayout;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

/**
 *
 * @author Omar
 */
public class MemoryUsageDemo extends JPanel {

    private TimeSeries total = new TimeSeries("Total CPU");
    private TimeSeries free = new TimeSeries("Free Memory");

    public MemoryUsageDemo(int paramInt) {
        super(new BorderLayout());
//        NumberFormat formatter;
//        formatter = new DecimalFormat("###,###");
        
        this.total.setMaximumItemAge(paramInt);
        this.free.setMaximumItemAge(paramInt);
        
        TimeSeriesCollection localTimeSeriesCollection = new TimeSeriesCollection();
        localTimeSeriesCollection.addSeries(this.total);
//        localTimeSeriesCollection.addSeries(this.free);
        
        DateAxis localDateAxis = new DateAxis("Time");
//        localDateAxis.setTickLabelFont(new Font("SansSerif", 0, 12));
//        localDateAxis.setLabelFont(new Font("SansSerif", 0, 14));
        localDateAxis.setAutoRange(true);
        localDateAxis.setLowerMargin(0.0D);
        localDateAxis.setUpperMargin(0.0D);
        localDateAxis.setTickLabelsVisible(true);
        
        NumberAxis localNumberAxis = new NumberAxis("CPU (%)");
//        localNumberAxis.setTickLabelFont(new Font("SansSerif", 0, 12));
//        localNumberAxis.setLabelFont(new Font("SansSerif", 0, 14));
//        localNumberAxis.setNumberFormatOverride(formatter);
//        localNumberAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        
        
        XYLineAndShapeRenderer localXYLineAndShapeRenderer = new XYLineAndShapeRenderer(true, false);
        localXYLineAndShapeRenderer.setSeriesPaint(0, Color.red);
        localXYLineAndShapeRenderer.setSeriesPaint(1, Color.green);
//        localXYLineAndShapeRenderer.setSeriesStroke(0, new BasicStroke(3.0F, 0, 2));
//        localXYLineAndShapeRenderer.setSeriesStroke(1, new BasicStroke(3.0F, 0, 2));
        
        XYPlot localXYPlot = new XYPlot(localTimeSeriesCollection, localDateAxis, localNumberAxis, localXYLineAndShapeRenderer);
        
        
        JFreeChart localJFreeChart = new JFreeChart(null, new Font("SansSerif", 1, 24), localXYPlot, true);
        //JFreeChart localJFreeChart = new JFreeChart(localXYPlot);
//        ChartUtilities.applyCurrentTheme(localJFreeChart);
        ChartPanel localChartPanel = new ChartPanel(localJFreeChart, true);
        localChartPanel.setBorder(BorderFactory.createTitledBorder("Uso de Memoria"));
        
        JPanel panelConfig = new JPanel(false);
        double panelPropSizes[][] = {{100,TableLayout.FILL,10}, //Columns
            {100, TableLayout.FILL, 10, 100}};    //Rows
        panelConfig.setLayout(new TableLayout(panelPropSizes));
        
        panelConfig.add(localChartPanel,"1,1");
        
        add(panelConfig);
    }

    public void addTotalObservation(double paramDouble) {
        this.total.add(new Millisecond(), paramDouble);
    }

    public void addFreeObservation(double paramDouble) {
        this.free.add(new Millisecond(), paramDouble);
    }

    public static void main(String[] paramArrayOfString) {
        JFrame localJFrame = new JFrame("Memory Usage Demo");
        MemoryUsageDemo localMemoryUsageDemo = new MemoryUsageDemo(300000);
        localJFrame.add(localMemoryUsageDemo, "Center");
        localJFrame.setBounds(200, 120, 600, 600);
        localJFrame.setVisible(true);

        Timer timer = new Timer();
        DataGenerator data = new DataGenerator(localMemoryUsageDemo);
        timer.schedule(data, 200L,2000L);
        localJFrame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent paramWindowEvent) {
                System.exit(0);
            }
        });
    }
}

class DataGenerator extends TimerTask {

    private MemoryUsageDemo localMemoryUsageDemo;

    DataGenerator(MemoryUsageDemo localMemoryUsageDemo) {
        this.localMemoryUsageDemo = localMemoryUsageDemo;
    }

    @Override
    public void run() {
        //long freeMemory = Runtime.getRuntime().freeMemory();
        long start = System.nanoTime();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        long[] allThreadIds = threadMXBean.getAllThreadIds();
//        System.out.println("Total JVM Thread count: " + allThreadIds.length);
        long nano = 0;
        String threadsInfo = "";
        for (long id : allThreadIds) {
            nano += threadMXBean.getThreadCpuTime(id);
            threadsInfo += threadMXBean.getThreadInfo(id);
            
        }
        System.out.println(threadsInfo);
        
        double realTime = System.nanoTime() - start;
        
        double cpuTime = nano / 1E6;
        //freeMemory = freeMemory / 1024;
//        long totalMemory = Runtime.getRuntime().totalMemory();
        
        //totalMemory = totalMemory / 1024;
//        System.out.printf("Total cpu time: %s ms; real time: %s\n", nano / 1E6, realTime);
        localMemoryUsageDemo.addTotalObservation(cpuTime*100/realTime);
//        localMemoryUsageDemo.addFreeObservation(0.05);
    }
}
