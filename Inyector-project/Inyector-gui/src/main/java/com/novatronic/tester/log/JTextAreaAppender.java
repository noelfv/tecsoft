/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.tester.log;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;

/**
 *
 * @author ofernandez
 */
public class JTextAreaAppender extends WriterAppender {
    static private List<JTextArea> logs;
    private static JTextArea jTextArea;

    /**
     * Set the target JTextArea for the logging information to appear.
     * @param jTextArea
     */
    static public void addTextArea(JTextArea jTextArea) {
        if (logs == null){
            logs = new ArrayList<JTextArea>();
        }
        logs.add(jTextArea);
    }

    /**
     * Format and then append the loggingEvent to the stored JTextArea.
     * @param loggingEvent
     */
    @Override
    public void append(LoggingEvent loggingEvent) {
        final String message = this.layout.format(loggingEvent);

        // Append formatted message to textarea using the Swing Thread.
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (logs !=null){
                    for (JTextArea jTextArealog : logs) {
                        jTextArealog.append(message);
                    }
                }
            }
        });
    }
}
