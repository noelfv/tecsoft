/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui.design;

import com.novatronic.formatter.gui.Components;
import com.novatronic.formatter.gui.Config;
import com.novatronic.formatter.gui.util.FindResource;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.AbstractButton;
import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 *
 * @author Omar
 */
public class TesterEffect extends MouseAdapter {

    private static final Logger log = Logger.getLogger(TesterEffect.class);
    private static Element root;

    public static void addEffects() {
        TesterEffect testerEffect;
        testerEffect = new TesterEffect();

        readXml(testerEffect);
    }

    private static void readXml(TesterEffect testerEffect) {
        String text;
        String compName;
        Element eleIcon;

        root = FindResource.getXMLElement(Config.DESING_CONFIG);
        testerEffect = new TesterEffect();
        List<Element> list = root.getChild("tooltips").getChildren("tooltip");
        for (int i = 0; i < list.size(); i++) {
            eleIcon = list.get(i);
            compName = eleIcon.getAttributeValue("comp");
            text = eleIcon.getAttributeValue("text");
            addListenerAndToolTip(compName, text, testerEffect);
        }
    }

    private static void addListenerAndToolTip(String compName, String text, TesterEffect testerEffect) {
        AbstractButton button;
        Components comps;

        comps = Components.getInstance();
        log.trace("Asignando a[" + compName + "], el texto[" + text + "]");
        button = comps.get(compName, AbstractButton.class);
        if (button != null) {
            button.addMouseListener(testerEffect);
            button.setToolTipText(text);
        } else {
            log.debug("No se encontro el componente=[" + compName + "]");
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        AbstractButton button;

        if (e.getComponent() instanceof AbstractButton) {
            button = (AbstractButton) e.getComponent();
            button.setContentAreaFilled(true);
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        AbstractButton button;

        if (e.getComponent() instanceof AbstractButton) {
            button = (AbstractButton) e.getComponent();
            button.setContentAreaFilled(false);
        }
    }
}
