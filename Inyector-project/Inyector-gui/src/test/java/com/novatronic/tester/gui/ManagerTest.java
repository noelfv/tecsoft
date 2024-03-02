/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.tester.gui;

import javax.swing.UIManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ofernandez
 */
public class ManagerTest {
    private static final Logger log = LoggerFactory.getLogger(ManagerTest.class);
    
    public static void main(String args[]) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            log.error("No se pudo cargar el L&F", ex);
        }

        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                new Inyector().setVisible(true);
            }
        });
    }
}
