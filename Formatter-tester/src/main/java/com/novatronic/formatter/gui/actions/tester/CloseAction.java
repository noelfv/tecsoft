/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui.actions.tester;

import com.novatronic.formatter.gui.Datas;
import com.novatronic.formatter.gui.actions.Action;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import javax.swing.JFrame;

/**
 *
 * @author Omar
 */
public class CloseAction extends Action<JFrame> implements WindowListener {

    @Override
    public void subscribe(JFrame component) {
        component.addWindowListener(this);
    }

    @Override
    public void windowClosing(WindowEvent e) {
        Timer timer;
        ExecutorService execService;
        
        timer = Datas.getInstance().get(Datas.PERF_TIMER_GRAPH, Timer.class);
        if (timer != null){
            timer.cancel();
        }
        
        execService = Datas.getInstance().get(Datas.PERF_SERVICE, ExecutorService.class);
        execService.shutdown();
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }
}
