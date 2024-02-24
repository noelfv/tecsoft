/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.filter;

import com.novatronic.formatter.exception.FilterException;
import com.novatronic.formatter.filter.util.FilterRule;
import com.novatronic.formatter.internal.InternalField;
import com.novatronic.formatter.internal.InternalFormat;
import com.novatronic.formatter.util.VariableByteBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 * Clase que crea, configura y almacena los filtros mediante el elemento XML que
 * contiene los filtros. Asimismo, contiene el repositorio de filtros por
 * formateador y los filtros asociados a un campo.
 *
 * @author rcastillejo
 * @version 1.0
 * @since 1.0, 29/11/2013
 */
public class Filters {
    
    private static final Logger log = Logger.getLogger(Filters.class);
    private static final String FIELD_PATH_SEPARATOR = ".";
    private static final String PADDING = "+";
    /**
     * Repositorio de filtros, este es generado a partir de del elemento jdom de
     * configuracion de los mismos en XML. Este repositorio tiene como llave al
     * identificador del filtro.
     *
     * @see #readConfiguration(org.jdom.Element)
     */
    private Map<String, Filter> filters;
    /**
     * Repositorio de filtros referenciados, este es generado a partir de la
     * configuracion de un campo. Este repositorio tiene como llave a la ruta
     * del campo y almacena a los filtros que estan asociados a un campo
     * independiente de un formateador. Cabe resaltar que si existe mas de un
     * campo con una misma ruta tienen filtros asociados se guardara el ultimo
     * configurado.
     *
     * @see #addFilter(java.lang.String, java.lang.String, java.lang.String)
     */
    private Map<String, Filter> referenceFilters;
    private Map<String, Pattern> referenceFilterPatterns;
    /**
     * Repositorio de filtros por formateador, este es generado a partir de la
     * configuracion de un campo. Este repositorio tiene como llave al
     * identificador del formateador y almacena los filtros que estan asociados
     * a los campos de este formateador.
     *
     * @see #addFilter(java.lang.String, java.lang.String, java.lang.String)
     */
    private Map<String, Map<String, Filter>> formatterFilters;
    
    private Map<String, Map<String, Pattern>> formatterFilterPatterns;
    
    private interface Tag {
        
        String FILTER = "filter";
    }
    
    public Filters() {
        filters = new LinkedHashMap();
        referenceFilters = new LinkedHashMap();
        formatterFilters = new LinkedHashMap();
        referenceFilterPatterns = new LinkedHashMap();
        formatterFilterPatterns = new LinkedHashMap();
    }

    /**
     * En este metodo se lee la configuracion para la creacion de los filtros
     * almacenados por su identificador.
     *
     * @param elementConfig Es el elemento de jdom el cual apunta a la
     * configuracion de los Filtros en el XML.
     */
    public void readConfiguration(Element elementConfig) {
        List<Element> filtersList = elementConfig.getChildren(Tag.FILTER);
        for (Element elemConfigFilter : filtersList) {
            Filter filter = FilterFactory.createFilter(elemConfigFilter);
            filters.put(filter.getId(), filter);
        }
    }

    /**
     * Agrega un filtro al repositorio de filtros por cada formteador. Asimismo,
     * agrega al repositorio de filtros independiente de un formateador
     * referenciado con un {@link InternalFormat#getPath() path}.
     *
     * @param formatId Identificador del Formateador
     * @param path Path del campo asociado a un filtro
     * @param filterId Identificador del Filtro
     * @throws FilterException En caso el filtro buscado sea desconocido
     */
    public void addFilter(String formatId, String path, String filterId) {
        addFilter(formatId, path, filters.get(filterId));
    }

    /**
     * Agrega un filtro al repositorio de filtros por cada formteador. Asimismo,
     * agrega al repositorio de filtros independiente de un formateador
     * referenciado con un {@link InternalFormat#getPath() path}.
     *
     * @param formatId Identificador del Formateador
     * @param path Path del campo asociado a un filtro
     * @param filter Filtro
     * @throws FilterException En caso el filtro sea nulo
     */
    public void addFilter(String formatId, String path, Filter filter) {
        Map mapFilters;
        Map mapFilterPatterns;
        Pattern patternField;
        
        if (filter == null) {
            throw new FilterException("El Filtro asociado al campo[" + path
                    + "] del formateador[" + formatId + "] es desconocida");
        }
        log.debug("Agregado filtro [formatterId=" + formatId + ", path=" + path + ", filter=" + filter.getId() + "] ...");
        mapFilters = verifiedFormatterFitlter(formatId);
        mapFilterPatterns = verifiedFormatterFitlterPattern(formatId);
        mapFilters.put(path, filter);
        patternField = FilterRule.makePatternByPath(path);
        mapFilterPatterns.put(path, patternField);
        formatterFilters.put(formatId, mapFilters);
        formatterFilterPatterns.put(formatId, mapFilterPatterns);
        referenceFilters.put(path, filter);
        referenceFilterPatterns.put(path, patternField);
        log.debug("Filtro agregado [formatterId=" + formatId + ", path=" + path + ", filter=" + filter.getId() + "]");
    }

    /**
     * Devuelve el Mapa de filtros de un formateador. En caso el mapa no exista,
     * se creara el mismo.
     *
     * @param formatterId Identificador del Formateador
     * @return Mapa de filtros
     */
    private Map verifiedFormatterFitlter(String formatterId) {
        Map mapFilters;
        
        if (formatterFilters.containsKey(formatterId)) {
            mapFilters = formatterFilters.get(formatterId);
        } else {
            mapFilters = new LinkedHashMap();
        }
        return mapFilters;
    }
    
    private Map verifiedFormatterFitlterPattern(String formatterId) {
        Map mapFilters;
        
        if (formatterFilterPatterns.containsKey(formatterId)) {
            mapFilters = formatterFilterPatterns.get(formatterId);
        } else {
            mapFilters = new LinkedHashMap();
        }
        return mapFilters;
    }
    

    /**
     * Devuelve la cadena del {@link InternalFormat intFmtToFilter} filtrado. El
     * cual se especifica el identificador del formateador para obtener del
     * repositorio de filtros a aplicar. <br/>Solo se aplica a los campos de un
     * nivel; es decir, no aplica para campos agrupadores.<br/>La asociacion del
     * campo con un filtro esta definido por un
     * {@link InternalFormat#getPath() path}.
     *
     * @param formatterId Identificador del Formateador
     * @param intFmtToFilter Formato interno a filtrar
     * @return La cadena que representa al objeto filtrado
     * @see #filterIntFormat(com.novatronic.formatter.internal.InternalFormat,
     * java.util.Map)
     */
    public String filter(String formatterId, InternalFormat intFmtToFilter) {
        log.debug("Filtrando ...");
        Map<String, Filter> mapFilters;
        
        if (formatterFilters.containsKey(formatterId)) {
            mapFilters = formatterFilters.get(formatterId);
            return filterIntFormat(intFmtToFilter, mapFilters, 
                    formatterFilterPatterns.get(formatterId));
        }
        
        return intFmtToFilter.toString();
    }

    /**
     * Devuelve la cadena del {@link InternalFormat intFmtToFilter} filtrado. El
     * cual se especifica el identificador del formateador para obtener del
     * repositorio de filtros a aplicar. <br/>Solo se aplica a los campos de un
     * nivel; es decir, no aplica para campos agrupadores.<br/>La asociacion del
     * campo con un filtro esta definido por un
     * {@link InternalFormat#getPath() path}. Asimismo, se puede indicar desde
     * que nivel se desea filtrar mediante el parametro basePath.
     *
     * @param formatterId Identificador del Formateador
     * @param basePath Ruta base donde se iniciara el filtrado
     * @param intFmtToFilter Formato interno a filtrar
     * @return La cadena que representa al objeto filtrado
     * @see #filterIntFormat(com.novatronic.formatter.internal.InternalFormat,
     * java.util.Map)
     */
    public String filter(String formatterId, String basePath, InternalFormat intFmtToFilter) {
        log.debug("Filtrando ...");
        Map<String, Filter> mapFilters;
        Map<String, Pattern> mapFilterPatterns;
        
        if (formatterFilters.containsKey(formatterId)) {
            mapFilters = formatterFilters.get(formatterId);
            mapFilterPatterns = formatterFilterPatterns.get(formatterId);
            return filterIntFormat(basePath, intFmtToFilter, mapFilters, mapFilterPatterns);
        }
        
        return intFmtToFilter.toString();
    }

    /**
     * Devuelve la cadena que representa el
     * {@link InternalFormat formato interno} filtrado, este formato se
     * considera anonimo debido a que no se le asocia un identificador de
     * formato. Por ello, este formato es analizado por cada uno de sus campos y
     * subcampos para obtener el filtro del repositorio de filtros
     * referenciados. <br/>Solo se aplica a los campos de un nivel; es decir, no
     * aplica para campos agrupadores.<br/>La asociacion del campo con un filtro
     * esta definido por un {@link InternalFormat#getPath() path}.
     *
     * @param intFmtToFilter Objeto a filtrar
     * @return La cadena que representa al objeto filtrado
     * @see #filterIntFormat
     * @see #referenceFilters
     */
    public String filter(InternalFormat intFmtToFilter) {
        log.debug("Filtrando ...");
        InternalFormat intFmtFiltered = new InternalFormat(intFmtToFilter);
        
        return filterIntFormat(intFmtFiltered, referenceFilters, referenceFilterPatterns);
    }

    /**
     * Aplica el filtrado a un formato interno mediante un repositorio de
     * filtro. Para ello, recorre cada filtro del repositorio y aplica el
     * filtrado al campo del formato interno asociado. <br/>Solo se aplica a los
     * campos de un nivel; es decir, no aplica para campos agrupadores.<br/>La
     * asociacion del campo con un filtro esta definido por un
     * {@link InternalFormat#getPath() path}.
     *
     * @param intFormat Formato interno a filtrar
     * @param mapFilters Repositorio de filtros a aplicar
     */
    private String filterIntFormat(InternalFormat intFormat, Map mapFilters, Map mapFilterPatterns) {
        VariableByteBuffer printer = new VariableByteBuffer(intFormat.size() * 30);
        printer.add("\n");
        printIntFormat(intFormat.getPath(), intFormat, printer, PADDING, mapFilters, mapFilterPatterns);
        return printer.toString();
    }
    
    private String filterIntFormat(String basePath, InternalFormat intFormat, Map mapFilters, Map mapFilterPatterns) {
        VariableByteBuffer printer = new VariableByteBuffer(intFormat.size() * 30);
        printer.add("\n");
        printIntFormat(basePath + FIELD_PATH_SEPARATOR, intFormat, printer, PADDING, mapFilters, mapFilterPatterns);
        return printer.toString();
    }
    
    private void printIntFormat(String basePath, InternalFormat intFormat, 
            VariableByteBuffer printer, String padding, Map<String, Filter> mapFilters, 
            Map<String, Pattern> mapFilterPatterns) {
        List<InternalField> list = new ArrayList<InternalField>(intFormat.getInternalFieldsAsList());
        String filterPath;
        printer.add(padding);
        printer.add(">ID=");
        printer.add(intFormat.getId() == null ? "NULL" : "'" + intFormat.getId() + "'");
        printer.add(", VALUES=\n");
        padding += "---";
        
        for (InternalField intField : list) {
            if (intField instanceof InternalFormat) {
                InternalFormat intFmt = (InternalFormat) intField;
                printIntFormat(intFmt.getPath() + FIELD_PATH_SEPARATOR, intFmt, printer, padding, mapFilters, mapFilterPatterns);
            } else {
                filterPath = findFilterPath(mapFilterPatterns, basePath + intField.getId());
                printIntField(intField, printer, padding, filterPath == null 
                        ? mapFilters.get(basePath + intField.getId()) : mapFilters.get(filterPath) );
                printer.add("\n");
            }
        }
    }
    
    private void printIntField(InternalField intField, VariableByteBuffer printer, String padding, Filter filter) {
        String value;
        
        value = intField.getValue();
        printer.add(padding);
        printer.add(">ID=");
        printer.add("'" + intField.getId() + "'");
        printer.add(", VALUE=");
        if (filter != null) {
            value = filter.filter(value);
        }
        printer.add(value == null ? "NULL" : "[" + value + "]");
    }
    
    /**
     * TODO: DOcumentar
     * @param path list.#.01
     * @param key list.0.01, list.0.02, list.0.03
     * @return
     */
    private String findFilterPath(Map<String, Pattern> mapFilterPatterns, String path) {
        String filterPathId;
        Matcher matcher;
        
        filterPathId = null;
        log.debug("Haciendo matching path=" + path + "...");
        for (Map.Entry<String, Pattern> entry : mapFilterPatterns.entrySet()) {
            String key = entry.getKey();
            Pattern pattern = entry.getValue();
            if(pattern != null){
                log.trace("Evaluando patron=" + pattern.pattern());
                matcher = pattern.matcher(path);
                if(matcher.find()){
                    filterPathId = key;
                    break;
                }
            }            
        }
        log.debug("Matching path=" + path + ", filterPathId=" + filterPathId + "");
        return filterPathId;
    }

    /**
     * Devuelve un filtro por su identificador.
     *
     * @param id Identificador del Filtro
     * @return Filtro obtenido
     */
    public Filter getFilter(String id) {
        return filters.get(id);
    }

    /**
     * Devuelve el listado de filtros cargados en la configuracion.
     *
     * @return Listado de filtros
     */
    public List<Filter> getFilterAsList() {
        return new ArrayList<Filter>(filters.values());
    }

    /**
     * Devuelve el repositorio de filtros asociado a un formateador
     *
     * @param formatterId Identificador del formateador
     * @return Repositorio de filtro del formateador
     */
    public Map getFiltersByFormatterId(String formatterId) {
        return formatterFilters.get(formatterId);
    }
}
