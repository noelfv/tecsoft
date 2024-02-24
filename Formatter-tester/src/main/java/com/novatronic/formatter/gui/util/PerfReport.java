/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui.util;

import java.io.File;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

/**
 *
 * @author Omar
 */
public class PerfReport {

    private static Logger log = Logger.getLogger(PerfReport.class);
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss.SSS");
    private static final NumberFormat formatExec = new DecimalFormat("#.00");
    private static final NumberFormat formatMem = new DecimalFormat("#.00");
    private static final NumberFormat formatCpu = new DecimalFormat("#.000");
    private static final String LS = System.getProperty("line.separator");

    public static void createReport(String fileName, List<Double> memResults,
            List<Double> cpuResults, List<Long> execResults, Date dateInit,
            Date dateEnd) {
        
        File fileReport;
        PrintWriter pw;

        try {
            fileReport = new File(fileName);
            pw = new PrintWriter(fileReport);
            
            writeGeneralReport(pw, dateInit, dateEnd);
            writeCpuReport(pw, cpuResults);
            writeMemoryReport(pw, memResults);
            writeExecutionReport(pw, execResults);
            
            pw.close();

            log.info("Archivo generado en:" + fileReport.getAbsolutePath());

        } catch (Exception ex) {
            log.error("No fue posible generar el Reporte en el archivo="
                    + fileName + ", causa:" + ex.getMessage(), ex);
        }

    }

    private static void writeGeneralReport(PrintWriter pw, Date dateInit,
            Date dateEnd) {
        pw.println("[Generales]");
        pw.println("Inicio   : " + DATE_FORMAT.format(dateInit));
        pw.println("Fin      : " + DATE_FORMAT.format(dateEnd));
        pw.println("Tiempo   : " + (dateEnd.getTime() - dateInit.getTime()) + "ms");
        pw.println("Num CPUs : " + Runtime.getRuntime().availableProcessors());
        pw.println();
    }

    private static void writeCpuReport(PrintWriter pw, List<Double> cpuResults) {
        Double[] cpuStats;
        int length;

        pw.println("[Uso de CPU]");
        if (cpuResults == null || cpuResults.isEmpty()) {
            pw.println("SIN REGISTROS");
        } else {
            cpuStats = getStats(cpuResults);
            length = formatCpu.format(cpuStats[3]).length();
            pw.println("Unidades : %");
            pw.printf("Muestras : %1$.0f" + LS, cpuStats[0]);
            pw.printf("Max      : %1$" + length + ".3f" + LS, cpuStats[1]);
            pw.printf("Min      : %1$" + length + ".3f" + LS, cpuStats[2]);
            pw.printf("Suma     : %1$" + length + ".3f" + LS, cpuStats[3]);
            pw.printf("Promedio : %1$" + length + ".3f" + LS, cpuStats[4]);
        }
        pw.println();
    }

    private static void writeMemoryReport(PrintWriter pw, List<Double> memResults) {
        Double[] memStats;
        int length;

        pw.println("[Uso de Memoria]");
        if (memResults == null || memResults.isEmpty()) {
            pw.println("SIN REGISTROS");
        } else {
            memStats = getStats(memResults);
            length = formatMem.format(memStats[3]).length();
            pw.println("Unidades : MB");
            pw.printf("Muestras : %1$.0f" + LS, memStats[0]);
            pw.printf("Max      : %1$" + length + ".2f" + LS, memStats[1]);
            pw.printf("Min      : %1$" + length + ".2f" + LS, memStats[2]);
            pw.printf("Suma     : %1$" + length + ".2f" + LS, memStats[3]);
            pw.printf("Promedio : %1$" + length + ".2f" + LS, memStats[4]);
        }
        pw.println();
    }

    private static void writeExecutionReport(PrintWriter pw, List<Long> execResults) {
        Double[] execStats;
        int length;

        pw.println("[Ejecuciones]");
        if (execResults == null || execResults.isEmpty()) {
            pw.println("SIN REGISTROS");
        } else {
            execStats = getStats(execResults);
            length = formatExec.format(execStats[3]).length();
            pw.println("Unidades : Nanosegundos");
            pw.printf("Muestras : %1$.0f" + LS, execStats[0]);
            pw.printf("Max      : %1$" + length + ".2f" + LS, execStats[1]);
            pw.printf("Min      : %1$" + length + ".2f" + LS, execStats[2]);
            pw.printf("Suma     : %1$" + length + ".2f" + LS, execStats[3]);
            pw.printf("Promedio : %1$" + length + ".2f" + LS, execStats[4]);
        }
        pw.println();
    }

    private static Double[] getStats(List list) {
        LogMF.trace(log, "Lista recibida para Reporte:{0}", list);
        double size = list.size();
        double max = ((Number) Collections.max(list)).doubleValue();
        double min = ((Number) Collections.min(list)).doubleValue();
        double average;
        double sum;

        sum = 0;
        for (int i = 0; i < list.size(); i++) {
            sum += ((Number) list.get(i)).doubleValue();
        }

        average = sum / list.size();

        LogMF.trace(log, "Report:size={0}, max={1}, min={2}, sum={3}, average={4}",
                new Object[]{list.size(), max, min, sum, average});

        return new Double[]{size, max, min, sum, average};
    }
}
