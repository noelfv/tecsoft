/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui.actions;

import com.novatronic.formatter.gui.Config;
import com.novatronic.formatter.gui.exception.GUIException;
import com.novatronic.formatter.gui.states.StateMachine;
import com.novatronic.formatter.gui.util.FindClass;
import com.novatronic.formatter.gui.util.FindResource;
import java.util.List;
import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 *
 * @author Omar
 */
public class TestActionsBuilder extends ActionsBuilder {

    private static final Logger log = Logger.getLogger(TestActionsBuilder.class);

    public TestActionsBuilder(StateMachine stateMachine) {
        super(stateMachine);
    }

    @Override
    public void setUpActions() {
        Element root;
        String compId;
        String compClass;
        String actionClass;

        try {
            root = FindResource.getXMLElement(Config.ACTIONS_CONFIG);
            List<Element> list = root.getChild("actions").getChildren("action");
            for (int i = 0; i < list.size(); i++) {
                Element elemAction = list.get(i);
                compId = elemAction.getAttributeValue("compId");
                compClass = elemAction.getAttributeValue("compClass");
                actionClass = elemAction.getAttributeValue("actionClass");
                buildAction(compId, FindClass.getClass(compClass), FindClass.getClass(actionClass));
            }
        } catch (InstantiationException ex) {
            log.error("No fue posible instanciar la accion", ex);
        } catch (IllegalAccessException ex) {
            log.error("No fue posible activar las accion", ex);
        } catch (Exception ex) {
            throw new GUIException("No fue posible crear la accion", ex);
        }
    }
}
