/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui;

import com.novatronic.formatter.gui.actions.ActionsBuilder;
import com.novatronic.formatter.gui.actions.TestActionsBuilder;
import com.novatronic.formatter.gui.panels.ToolWindowTest;
import com.novatronic.formatter.gui.design.TesterEffect;
import com.novatronic.formatter.gui.design.TesterIcon;
import com.novatronic.formatter.gui.log.JTextAreaAppender;
import com.novatronic.formatter.gui.states.StateMachine;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.apache.log4j.Logger;
import org.noos.xing.mydoggy.ToolWindowManager;
import org.noos.xing.mydoggy.plaf.MyDoggyToolWindowManager;

/**
 *
 * @author Omar
 */
public class Tester {
    private static final Logger log = Logger.getLogger(Tester.class);
    private JFrame frame;
    private ToolWindowManager toolWindowManagerTest;
    private ActionsBuilder TestActionsBuilder;
    private StateMachine testStateMachine;
    
    public static void main(String[] args) {
        //SplashScreen splash = SplashScreen.getSplashScreen();
        //splash.getSize();
        Tester testerGui = new Tester();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            testerGui.run();
        } catch (Exception ex) {
            log.error("No fue posible iniciar el Tester", ex);
        }
    }
    
    private void run() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setUp();
                start();
            }
        });
    }
    
    private void setUp() {
        initComponents();
        setUpTestStateAndActions();
    }

    private void initComponents(){
        
        frame = new JFrame("Formatter Tester");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Components.getInstance().add(frame, ComponentsID.FR_MAIN);

        toolWindowManagerTest = ToolWindowTest.buildToolWindow();
        frame.add((MyDoggyToolWindowManager)toolWindowManagerTest);
        
        log.debug(Components.getInstance());
        TesterIcon.updateTesterIcons(frame);
        TesterEffect.addEffects();
        JTextAreaAppender.addTextArea(Components.getInstance().get(ComponentsID.TA_TEST_LOG, JTextArea.class));
    }
    
    private void setUpTestStateAndActions(){
        testStateMachine = new StateMachine();
        testStateMachine.setUpStates(Config.SM_TEST_PANEL);
        testStateMachine.init();
        log.debug(testStateMachine);
        
        TestActionsBuilder = new TestActionsBuilder(testStateMachine);
        TestActionsBuilder.setUpActions();
    }
    
    
    private void start() {
        frame.pack();
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
    }
}
