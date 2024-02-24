/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui.states;

import com.novatronic.formatter.gui.exception.GUIException;
import com.novatronic.formatter.gui.util.FindResource;
import java.util.List;
import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 *
 * @author Omar
 */
public class XMLReader {
    private static final Logger log = Logger.getLogger(XMLReader.class);
    private Element root;
    
    public void readFile(String configFile) throws GUIException{
        log.debug("Leyendo configuracion [" + configFile + "]");
        root = FindResource.getXMLElement(configFile);
    }
    
    public void createStates(StateMachine stateMachine){
        List<Element> stateElements;
        String stateID;
        String stateClass;
        
        try{
            stateElements = root.getChild("states").getChildren("state");
            for (Element element : stateElements) {
                stateID = element.getAttributeValue("id");
                stateClass = element.getAttributeValue("class");
                stateMachine.createState(stateID, stateClass);
            }
        } catch(Exception ex){
            throw new GUIException("No fue posible crear los estados", ex);
        }
    }
    
    public void createTransitions(StateMachine stateMachine){
        List<Element> transElements;
        String stateID;
        
        transElements = root.getChild("stateMachine").getChildren("transition");
        for (Element element : transElements) {
            stateID = element.getAttributeValue("from");
            State state = stateMachine.getState(stateID);
            addTransitionsToState(element, state, stateMachine);
        }
    }
    
    private void addTransitionsToState(Element stateElement, State state, StateMachine stateMachine){
        List<Element> transElements;
        String componentId;
        String stateId;
        
        transElements = stateElement.getChildren("if");
        for (Element element : transElements) {
            componentId = element.getAttributeValue("actionOn");
            stateId = element.getAttributeValue("toState");
            stateMachine.addTransition(state, componentId, stateId);
        }
    }
}
