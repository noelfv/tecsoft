/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui.design;

import com.novatronic.formatter.gui.ComponentsID;
import com.novatronic.formatter.gui.Components;
import com.novatronic.formatter.gui.Config;
import com.novatronic.formatter.gui.util.FindResource;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 *
 * @author Omar
 */
public class TesterIcon {

    private static final Logger log = Logger.getLogger(TesterIcon.class);
    private static Element root;
    private static final String LEAF_ICON = "/com/novatronic/formatter/img/Tree-field_03.png";
    private static final String CLOSED_ICON = "/com/novatronic/formatter/img/Tree-intF_02.png";
    private static final String OPEN_ICON = "/com/novatronic/formatter/img/Tree-intF_02.png";
    private static final String FRAME_ICON = "/com/novatronic/formatter/img/LogoNova.gif";

    public static void updateTesterIcons(JFrame frame) {
        Class clazz;

        clazz = frame.getClass();
        updateTreeRender();
        frame.setIconImage(new ImageIcon(clazz.getResource(FRAME_ICON)).getImage());

        readXml(clazz);
    }

    private static void readXml(Class clazz) {
        root = FindResource.getXMLElement(Config.DESING_CONFIG);
        List<Element> list = root.getChild("icons").getChildren("icon");
        for (int i = 0; i < list.size(); i++) {
            Element eleIcon = list.get(i);
            String compName = eleIcon.getAttributeValue("comp");
            String iconResource = eleIcon.getAttributeValue("icon");
            log.trace("Asignando a[" + compName + "], el icono[" + iconResource + "]");
            setButtonIcon(clazz, compName, iconResource);
        }
    }

    private static void setButtonIcon(
            Class clazz, String componentID, String resourcePath) {
        AbstractButton button;

        button = Components.getInstance().get(componentID, AbstractButton.class);
        log.trace("Componente buscado:" + button);
        if (button != null) {
            button.setIcon(new ImageIcon(clazz.getResource(resourcePath)));
            button.setContentAreaFilled(false);
            button.setFocusPainted(false);
        } else {
            log.debug("No se ubico el componente=[" + componentID + "]");
        }
    }

    private static void updateTreeRender() {
        JTree treIntFrmTest = Components.getInstance().get(
                ComponentsID.TR_INTFORMAT_TEST, JTree.class);
        JTree treIntFrmGenerado = Components.getInstance().get(
                ComponentsID.TR_INTFORMAT_RESULT, JTree.class);

        updateTreeIcons(treIntFrmTest);
        updateTreeIcons(treIntFrmGenerado);
    }

    private static void updateTreeIcons(JTree jtree) {
        DefaultTreeCellRenderer render;
        render = (DefaultTreeCellRenderer) jtree.getCellRenderer();
        render.setLeafIcon(new ImageIcon(jtree.getClass().getResource(LEAF_ICON)));
        render.setClosedIcon(new ImageIcon(jtree.getClass().getResource(CLOSED_ICON)));
        render.setOpenIcon(new ImageIcon(jtree.getClass().getResource(OPEN_ICON)));
    }
}
