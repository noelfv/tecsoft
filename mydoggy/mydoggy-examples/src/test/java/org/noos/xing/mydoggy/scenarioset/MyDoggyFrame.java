package org.noos.xing.mydoggy.scenarioset;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.skin.SkinInfo;
import org.noos.xing.mydoggy.ContentManager;
import org.noos.xing.mydoggy.TabbedContentManagerUI;
import org.noos.xing.mydoggy.TabbedContentManagerUI.TabLayout;
import org.noos.xing.mydoggy.plaf.MyDoggyToolWindowManager;
import org.noos.xing.mydoggy.plaf.ui.content.MyDoggyMultiSplitContentManagerUI;

public class MyDoggyFrame
{
    private static MyDoggyToolWindowManager init()
    {
        MyDoggyToolWindowManager myDoggyToolWindowManager = new MyDoggyToolWindowManager();

        initContentManager(myDoggyToolWindowManager);

        return myDoggyToolWindowManager;
    }

    public static void main(String[] args)
    {
        JFrame frame = new JFrame("MyDoggy Test");
        JMenuBar bar = new JMenuBar();
        Map<String, SkinInfo> skinMap = SubstanceLookAndFeel.getAllSkins();
        JMenu menu = new JMenu("L&F");
        for (final Map.Entry<String, SkinInfo> entry : skinMap.entrySet())
        {
            JMenuItem jmiSkin = new JMenuItem(entry.getValue().getDisplayName());
            jmiSkin.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    SubstanceLookAndFeel.setSkin(entry.getValue().getClassName());
                }
            });

            menu.add(jmiSkin);
        }
        bar.add(menu);
        frame.setJMenuBar(bar);
        final MyDoggyToolWindowManager myDoggyToolWindowManager = init();
        frame.setLayout(new BorderLayout());
        frame.getContentPane().add(myDoggyToolWindowManager, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(new Dimension(500, 500));
        frame.setVisible(true);
    }

    private static void initContentManager(MyDoggyToolWindowManager myDoggyToolWindowManager)
    {
        ContentManager contentManager = myDoggyToolWindowManager.getContentManager();
        TabbedContentManagerUI contentManagerUI = new MyDoggyMultiSplitContentManagerUI();
        contentManager.setContentManagerUI(contentManagerUI);
        contentManagerUI.setPopupMenuEnabled(false);
        contentManagerUI.setMinimizable(false);
        contentManagerUI.setMaximizable(false);
        contentManagerUI.setTabPlacement(TabbedContentManagerUI.TabPlacement.TOP);
        contentManagerUI.setShowAlwaysTab(true);
        contentManagerUI.setTabLayout(TabLayout.WRAP);

        for (int index = 0; index < 4; index++)
        {
            String contentId = "Content_" + index;
            JPanel panel = new JPanel();
            panel.setPreferredSize(new Dimension(100, 100));
            contentManager.addContent(contentId, contentId, null, panel);
        }
    }
}