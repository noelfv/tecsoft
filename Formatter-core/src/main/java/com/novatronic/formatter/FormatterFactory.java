/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter;

import com.novatronic.formatter.exception.FormatterFactoryException;
import com.novatronic.formatter.filter.Filters;
import com.novatronic.formatter.filter.SingletonFilters;
import com.novatronic.formatter.macros.ParserMacro;
import com.novatronic.formatter.macros.SimpleParserMacro;
import com.novatronic.formatter.util.ResourceHelper;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * Esta clase es la primera a ser creada al usar el formateador. Para su creacion se
 * requiere un archivo el cual contiene la configuracion de los formatos a usar. Luego, a
 * partir de esta se pueden ir obteniendo los formatos para crear o analizar las tramas.
 * Puede revisar los contructores sobrecargados para las distintas necesidades de lectura
 * del archivo de configuracion de la factoria.<br/><br/>
 * Un aspecto importante en este punto es que la factoria intenta delegar la mayoria de
 * tareas de creacion de estructuras de objetos en esta fase para de esta manera obtener
 * un mejor rendimiento al momento de realizar la creacion o analisis de tramas. En este
 * sentido, la factoria deberia ser creada una sola vez, lo cual significaria que la 
 * configuracion se lee una unica vez. Por tanto, mantener una creacion constante de esta
 * factoria conllevaria a una degradacion en el tiempo durante su uso.<br/><br/>
 * Un ejemplo simple de uso seria el siguiente:<br/><br/>
 * <pre>
 * FormatterFactory factory = new FormatterFactory("RUTA_AL_ARCHIVO");
 * Formatter formatISO200 = factory.getFormatter("ID_DEL_FORMATO");
 * </pre>
 * En la primera linea se realiza la creacion de la factoria. Esta linea deberia ejecutarse
 * un unica vez. Tambien es importante recalcar que luego de esta linea, los formatos ya
 * han sido creados y estan listos para su ejecucion. La linea dos simplemente accede al
 * mapa interno de los formatos creados a partir de la configuracion usada. Podria tambien
 * crear varias factorias para distintos archivos de configuracion de forma de agrupar por
 * configuracion los formatos.<br/><br/>
 * Finalmente, si bien podría haberse programado esta clase como estatica siguiendo un 
 * patron singleton, se prefirio mantener la creacion de una instancia de tal manera que 
 * permita dejar abierta la posibilidad de usarla bajo un patron sigleton o mediante 
 * varios hilos de ejecucion.
 * 
 * @author ofernandez
 * @version 1.0
 * @since 1.0, 05/11/2010
 */
public class FormatterFactory {

    private static final Logger log = Logger.getLogger(FormatterFactory.class);
    private Map<String, Formatter> formatters;
    private ParserMacro parserMacro;
    private Filters filters;

    /**
     * Ubicacion de la configuracion por omision: {@value}. Este es utilizado en
     * caso no se asigne una ubicacion de configuracion al construir este Instancia. Este
     * archivo es buscado inicialmente comoun recurso dentro del classpath y finalmente
     * en el directorio de ejecucion.
     */
    private static final String DEFAULT_CONFIGURATION_PATH = "format.xml";

    /**
     * Lee la configuracion desde un archivo por defecto {@link
     * #DEFAULT_CONFIGURATION_PATH DEFAULT_CONFIGURATION_PATH}. Dicho Archivo puede estar
     * en la ruta de ejecucion o como un recurso del classpath
     * @throws FormatterFactoryException Sino se pudo leer el archivo
     */
    public FormatterFactory() {
        URL UrlFormat;
        
        formatters = new LinkedHashMap();
        log.debug("Leyendo configuracion [" + DEFAULT_CONFIGURATION_PATH + "]");
        try {
            UrlFormat = ResourceHelper.findResource(DEFAULT_CONFIGURATION_PATH);
            Document doc = (new SAXBuilder()).build(UrlFormat);
            readDoc(doc);
        } catch (JDOMException ex) {
            throw new FormatterFactoryException("Formato invalido para la "
                    + "configuracion [" + DEFAULT_CONFIGURATION_PATH + "]", ex);
        } catch (IOException ex) {
            throw new FormatterFactoryException("No es posible leer el archivo"
                    + " [" + DEFAULT_CONFIGURATION_PATH + "]", ex);
        }
    }

    /**
     * Lee la configuracion desde la ruta pasada por parametro la cual puede ubicarse en
     * el classpath, en el directorio de ejecucion o en una ruta especificada en 
     * {@code configuration}.
     * @param configuration Ruta al archivo que contiene la configuracion. Este puede ser
     * un archivo en el classpath, en el directorio de ejecucion o una ruta especifica.
     * @throws FormatterFactoryException
     */
    public FormatterFactory(String configuration) {
        URL UrlFormat;
        
        formatters = new LinkedHashMap();
        log.debug("Leyendo configuracion [" + configuration + "]");
        try {
            UrlFormat = ResourceHelper.findResource(configuration);
            Document doc = (new SAXBuilder()).build(UrlFormat);
            readDoc(doc);
        } catch (JDOMException ex) {
            throw new FormatterFactoryException("Formato invalido para la "
                    + "configuracion [" + configuration + "]", ex);
        } catch (IOException ex) {
            throw new FormatterFactoryException("No es posible leer el archivo"
                    + " [" + configuration + "]", ex);
        }
    }

    /**
     * Lee la configuracion desde la ruta pasada por parametro.
     * @param configuration Ruta al archivo que contiene la configuracion.
     * @throws FormatterFactoryException
     */
    public FormatterFactory(URL configuration) {
        formatters = new LinkedHashMap();
        log.debug("Leyendo configuracion [" + configuration + "]");
        try {
            Document doc = (new SAXBuilder()).build(configuration);
            readDoc(doc);
        } catch (JDOMException ex) {
            throw new FormatterFactoryException("Formato invalido para la "
                    + "configuracion [" + configuration + "]", ex);
        } catch (IOException ex) {
            throw new FormatterFactoryException("No es posible leer el archivo"
                    + " [" + configuration + "]", ex);
        }
    }

    /**
     * Lee la configuracion desde la ruta pasada por parametro.
     * @param configuration tipo File que contiene la configuracion
     * @throws FormatterFactoryException
     */
    public FormatterFactory(File configuration) {
        formatters = new LinkedHashMap();
        log.debug("Leyendo configuracion [" + configuration + "]");
        try {
            Document doc = (new SAXBuilder()).build(configuration);
            readDoc(doc);
        } catch (JDOMException ex) {
            throw new FormatterFactoryException("Formato invalido para la "
                    + "configuracion [" + configuration + "]", ex);
        } catch (IOException ex) {
            throw new FormatterFactoryException("No es posible leer el archivo"
                    + " [" + configuration + "]", ex);
        }
    }

    /**
     * Lee la configuracion desde la ruta pasada por parametro.
     * @param configuration InputStream que contiene la configuracion
     * @throws FormatterFactoryException
     */
    public FormatterFactory(InputStream configuration) {
        formatters = new LinkedHashMap();
        log.debug("Leyendo configuracion [" + configuration + "]");
        try {
            Document doc = (new SAXBuilder()).build(configuration);
            readDoc(doc);
        } catch (JDOMException ex) {
            throw new FormatterFactoryException("Formato invalido para la "
                    + "configuracion [" + configuration + "]", ex);
        } catch (IOException ex) {
            throw new FormatterFactoryException("No es posible leer el archivo"
                    + " [" + configuration + "]", ex);
        }
    }

    private void readDoc(Document doc) {
        try {
            log.debug("Documento leido, obteniendo elemento raiz");
            Element root = doc.getRootElement();
            log.debug("Raiz obtenida. Configurando formateadores");
            configureMacros(root.getChild(Tag.MACROS));
            configureFilters(root.getChild(Tag.FILTERS));
            configureFormatters(root.getChild(Tag.FORMATTERS).getChildren());
            configureSingletonFilters();
        } catch (IllegalStateException  ex) {
            throw new FormatterFactoryException(ex.getMessage(),ex);
        } catch (Exception ex){
            throw new FormatterFactoryException(ex.getMessage(), ex);
        }
    }

    private void configureMacros(Element elementConfig){
        //FIXME se puede cambiar por una llamada de clase distinta y asi inyectarlo.
        parserMacro = new SimpleParserMacro();
        if(elementConfig != null){
            parserMacro.readConfiguration(elementConfig);
        }
        
    }

    private void configureFilters(Element elementConfig){
        //FIXME se puede cambiar por una llamada de clase distinta y asi inyectarlo.
        filters = new Filters();
        if(elementConfig != null){
            filters.readConfiguration(elementConfig);
        }
    }
    
    private void configureFormatters(List<Element> formattersAsList) {
        for (int i = 0; i < formattersAsList.size(); i++) {
            //FIXME se puede cambiar por una llamada de clase distinta y asi inyectarlo.
            Formatter formatter = new SimpleFormatter();
            formatter.setParserMacro(parserMacro);
            formatter.setFilters(filters);
            formatter.readConfiguration(formattersAsList.get(i));
            formatters.put(formatter.getId(), formatter);
        }
    }
    
    private void configureSingletonFilters() {
        SingletonFilters.configureFilters(filters);
    }

    /**
     * Devuelve al cantidad de formateadores configurados y habilitados para su
     * uso.
     * @return
     */
    public int getNumberOfFormatters() {
        return formatters.size();
    }

    /**
     * Devueve un formateador correspondiente al identificador recibidio como
     * parametro. Si permite claves del tipo null debido a que internamente se
     * utiliza una lista del tipo {@link LinkedHashMap }.
     * @param id Identificador del formateador a obtener
     * @return el formatedor solicitado o null en caso no exista.
     */
    public Formatter getFormatter(String id) {
        return formatters.get(id);
    }
    
    /**
     * Devuelve el contenedor de los filtros configurados
     * 
     * @return Contenedor de filtros
     */
    public Filters getFilters(){
        return filters;
    }
    
    public interface Tag {
        public static final String MACROS = "macros";
        public static final String FILTERS = "filters";
        public static final String FORMATTERS = "formatters";
        public static final String FORMATTER = "formatter";
    }
}
