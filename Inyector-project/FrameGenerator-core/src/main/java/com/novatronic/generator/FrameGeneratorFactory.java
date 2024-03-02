/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.generator;

import com.novatronic.generator.exception.FrameGeneratorFactoryException;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 *
 * @author ofernandez
 * @version 1.0
 * @since 1.0, 12/11/2010
 */
public abstract class FrameGeneratorFactory {
    private static final Logger log = Logger.getLogger(FrameGeneratorFactory.class);

    /**
     * Ubicacion de la configuracion por omision: {@value}. Este es utilizado en
     * caso no se asigne una ubicacion de configuracion al construir este Instancia.
     */
    private static final String DEFAULT_CONFIG_PATH = "generator.xml";
    
    /**
     * Ubicacion de la configuracion. En un inicio tendra como valor :
     * {@value #DEFAULT_CONFIG_PATH}.
     * @see #DEFAULT_CONFIG_PATH DEFAULT_CONFIG_PATH
     */
    private static String configurationPath = DEFAULT_CONFIG_PATH;

    public FrameGeneratorFactory(){
    }

    public static FrameGenerator createFrameGenerator(String customConfigurationPath){
        configurationPath = customConfigurationPath;
        return createFrameGenerator();
    }

    /**
     * Crea el FrameGenerator. Para realizar esto, lee la configuracion que
     * tiene asignado por omision {@link #DEFAULT_CONFIG_PATH DEFAULT_CONFIG_PATH}
     * y luego carga la estructura de tramas a usar en la configuracion 
     * @return
     * @throws FrameGeneratorFactoryException Si el archivo de configuracion no
     * puede ser encontrado o tiene un mal formato.
     */
    public static FrameGenerator createFrameGenerator(){
        log.debug("Leyendo configuracion [" + configurationPath + "]");
        //FIXME aca se deberia inyectar el implementador de FrameGenerator
        FrameGenerator frameGenerator = new SimpleFrameGenerator();
        try {
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(configurationPath);
            log.debug("Documento leido, obteniendo elemento raiz");
            Element root = doc.getRootElement();
            log.debug("Raiz obtenida. Configurando formateadores");
            frameGenerator.readConfiguration(root);
            return frameGenerator;
        } catch (JDOMException ex) {
            throw new FrameGeneratorFactoryException("Formato invalido para la "
                    + "configuracion [" + configurationPath + "]", ex);
        } catch (IOException ex) {
            throw new FrameGeneratorFactoryException("No es posible leer el archivo"
                    + " [" + configurationPath + "]", ex);
        }
    }
}
