/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui.panels;

import com.novatronic.formatter.gui.ComponentsID;
import com.novatronic.formatter.gui.Components;
import com.novatronic.formatter.gui.util.ToolWindowUtil;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import org.apache.log4j.Logger;
import org.noos.xing.mydoggy.ToolWindow;
import org.noos.xing.mydoggy.ToolWindowAnchor;
import org.noos.xing.mydoggy.ToolWindowManager;
import org.noos.xing.mydoggy.plaf.MyDoggyToolWindowManager;

/**
 *
 * @author Omar
 */
public class ToolWindowTest {

    private static final Logger log = Logger.getLogger(ToolWindowTest.class);

    public static ToolWindowManager buildToolWindow() {
        ToolWindowManager toolWindowManagerTest;
        JComponent panelSelector;
        JComponent panelMain;
        JComponent panelLog;

        toolWindowManagerTest = new MyDoggyToolWindowManager();
        Components.getInstance().add(
                (MyDoggyToolWindowManager) toolWindowManagerTest,
                ComponentsID.TWM_TEST);

        panelSelector = new TestSelectorPanel();
        panelMain = buildMainPanel();
        panelLog = new LogPanel();

        toolWindowManagerTest.registerToolWindow("Log", "Log", null,
                panelLog, ToolWindowAnchor.BOTTOM);
        toolWindowManagerTest.registerToolWindow("Test", "Test", null,
                panelSelector, ToolWindowAnchor.LEFT);

        setupTestTool(toolWindowManagerTest);

        // Made all tools available
        for (ToolWindow window : toolWindowManagerTest.getToolWindows()) {
            window.setAvailable(true);
            log.debug(window.getId());
        }
        toolWindowManagerTest.getContentManager().addContent("Main",
                "Test Main",
                null, // An icon
                panelMain);

        return toolWindowManagerTest;
    }

    public static JComponent buildMainPanel() {
        JTabbedPane tabbedPane;

        tabbedPane = new JTabbedPane();
        tabbedPane.add(new IFToFramePanel(), "IF a Trama");
        tabbedPane.add(new FrameToIFPanel(), "Trama a FI");
        tabbedPane.add(new IFAndFrameTestPanel(), "Test Log");
        //tabbedPane.add(new LogToXmlPanel(), "Log a XML");
        tabbedPane.add(new PerformancePanel(), "Performance");

        return tabbedPane;
    }

    private static void setupTestTool(ToolWindowManager toolWindowManagerTest) {
        ToolWindowUtil.setupCommonToolWindow(toolWindowManagerTest, "Log", 60);
        ToolWindowUtil.setupCommonToolWindow(toolWindowManagerTest, "Test", 250);
    }
}
