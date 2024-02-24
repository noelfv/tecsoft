/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.field;

import com.novatronic.formatter.Formatter;
import com.novatronic.formatter.context.FieldConfigurationContext;
import com.novatronic.formatter.context.FmtContext;
import com.novatronic.formatter.exception.FieldConfigurationException;
import com.novatronic.formatter.exception.FieldValueException;
import com.novatronic.formatter.filter.Filter;
import com.novatronic.formatter.filter.Filters;
import com.novatronic.formatter.internal.InternalField;
import com.novatronic.formatter.internal.InternalFormat;
import com.novatronic.formatter.macros.ParserMacro;
import com.novatronic.formatter.util.VariableByteBuffer;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 *
 * @author ofernandez
 * @version 1.0
 * @since 1.0, 05/11/2010
 */
public abstract class Field implements FieldConfigurationContext {

    private static final Logger log = Logger.getLogger(Field.class);
    /**
     * Referencia al Parseador del formateador a partir del cual se parseara el cotenido
     * fijo de un campo.
     */
    private ParserMacro parserMacro;
    /**
     * Referencia al Contenedor de filtros.
     */
    private Filters filters;
    
    /**
     * Identificador del Campo el cual es el mismo que el indicado en la configuracion
     */
    private String id;
    /**
     * Identificador del Formateador asociado al campo.
     */
    private String formatterId;
    /**
     * Texto contenido en la configuracion y que se ubica dentro del tag del campo
     */
    private String text;
    /**
     * Es el tipo de campo de esta instancia y que esta asociado a la clase
     * implementadora. Este valor es relavante para la instancia de esta clase. La
     * construccion de este esta asociado a la factoria de Campos.
     *
     * @see FieldFactory
     */
    private String type;
    /**
     * Indica si el valor como texto se debe parsear o no. Este valor es asignado desde la
     * configuracion a partir del atributo "parse={true}". Si este atributo no aparece, se
     * asume no se parseara nada.
     */
    private boolean parseable;
    /**
     * Indica si el valor almacenado por defecto se debera tener en cuenta al crear el
     * InternalFormat de Configuracion. Para aquellos campos marcados con TRUE, tendran un
     * valor nulo y no seran tomados en cuenta, por omision sera FALSE.
     */
    private boolean nullable;
    /**
     * Indica el tipo de protección del valor. Este valor es asignado desde la
     * configuracion a partir del atributo "protec". Si este atributo no aparece 
     * o tiene el valor "NONE", se asume que no se enmascarara nada.
     */
    private Filter filter;
    /**
     * Indica si el valor almacenado debera ser protegido. Para aquellos campos 
     * que tengan configurado el atributo "protec".
     */
    private boolean protectable;
    /**
     * Contexto de la configuracion. A partir de este se pueden obtener o colocar datos
     * que puedan usar otros campos para poder comunicarse
     */
    private FmtContext configContext;
    /**
     * Es la clave a buscar en los parametros recibidos al momento de crear un FI a partir
     * de la configuracion
     */
    private String keyParam;
    protected String logId;
    
    private String parentPath;
    private String path;

    private class Attr {

        private static final String TYPE = "type";
        private static final String PARSE = "parse";
        private static final String NULLABLE = "null";
        private static final String KEY_PARAM = "keyParam";
        private static final String TYPE_PROTEC = "protec";
    }

    private interface Const {
        
        String PROTEC_DEFAULT = "NONE";
    }

    /**
     * Crea un objeto del tipo {@link InternalField} el cual se usa para agregar el valor
     * del campo al {@link InternalFormat} y que finalmente estan destinados a usarse
     * programaticamente. Este metodo es importante si se genera la trama unicamente a
     * partir de la configuracion sin actualizar los campos con nuevos valores. Tambien es
     * usado para crear el primer internalFormat
     *
     * @return Un nuevo objeto {@link InternalField}
     * @throws FieldValueException Si se produce algun problema con el valor de obtenido
     * para fomar al trama.
     * @see Formatter#getInternalFormatFromConfig()
     */
    public InternalField getValueAsInternalField() {
        return getValueAsInternalField(null, null);
    }

    /**
     * Crea un objeto del tipo {@link InternalField} el cual se usa para agregar el valor
     * del campo al {@link InternalFormat} y que finalmente estan destinados a usarse
     * programaticamente. Este metodo es importante si se genera la trama unicamente a
     * partir de la configuracion sin actualizar los campos con nuevos valores. Ademas,
     * este puede recibir un archivo de propiedades que puede ser leido por los campos
     * para el caso en el cual se requiera alguna construccion de un
     * {@link InternalFormat} pero que segun su funcionamiento puede variar en la forma
     * como se crear internamente este.
     *
     * @param params Un objeto properties con las parametros a usar por los campos del
     * formateador
     * @param intFmtCtx
     * @return Un nuevo objeto {@link InternalField}
     * @throws FieldValueException Si se produce algun problema con el valor de obtenido
     * para fomar al trama.
     * @see Formatter#getInternalFormatFromConfig()
     */
    public InternalField getValueAsInternalField(Properties params, InternalFormat intFmtCtx) {
        return new InternalField(this.getId(), this.getValue(params, intFmtCtx));
    }

    /**
     * Lee la configuracion obtenida en el xml. Esta configuracion basicamente se define
     * en los atributos pero puede contemplar tambien elementos internos. Estos elementos
     * internos son delegados a las clases implementadoras las cuales leeran su
     * configuracion adicional. Son mandatorios los campos: id, type.
     *
     * @param element Element del Dom donde se ubican los atributos o sub elementos que
     * usara este campos.
     * @throws FieldConfigurationException Si ocurre un problema al tratar de leer la
     * configuracion del Campo.
     */
    public final void readConfiguration(Element element) {
        String typeProtec;
        
        protectable = Boolean.FALSE;
        type = element.getAttributeValue(Attr.TYPE);
        nullable = Boolean.parseBoolean(element.getAttributeValue(Attr.NULLABLE));
        text = element.getChildren().isEmpty() ? text = element.getText() : null;
        keyParam = element.getAttributeValue(Attr.KEY_PARAM);
        parseable = Boolean.valueOf(element.getAttributeValue(Attr.PARSE));
        typeProtec = element.getAttributeValue(Attr.TYPE_PROTEC);
        try {
            if(!(typeProtec == null || typeProtec.equals(Const.PROTEC_DEFAULT))){
                filter = getFilterByProtec(typeProtec);
                protectable = Boolean.TRUE;
            }
            logId = "[Id='" + id + "']";
            readCustomConfiguration(element);
        } catch (Exception ex) {
            throw new FieldConfigurationException("id=" + id + "," + ex.getMessage(), ex);
        }

    }

    /**
     * En este metodo se lee la configuracion adicional que necesitara los campos
     * especializados.
     *
     * @param element Es el elemento jdom el cual apunta a la configuracion de este campo.
     * @throws FieldConfigurationException Si al momento de leer la configuracion del
     * campo esta tenga un formato incorrecto.
     */
    protected abstract void readCustomConfiguration(Element element);

    /**
     * En este metodo, el campo coloca una cantidad de bytes asociada a este campo al
     * final de la trama (dada por frame). Tambien puede darse el caso de colocar bytes en
     * otra posicioin, debido a lo cual la trama incrementara su tamaño. El tamaño actual
     * de la trama puede ser consultado por el metodo
     * {@link VariableByteBuffer#getLength()  VariableByteBuffer.getLength()}
     *
     * @param internalFormat Datos que pueden ser usados por el cmapo para generar la
     * trama.
     * @param frame Es la trama en si misma. Este objeto se debrá colocar la informacion
     * relativa a este campo.
     * @throws FieldValueException Si se tiene un problema al generar el valor. Esto puede
     * ser porque tiene validacion de formato o de otro tipo.
     * @return la cantidad de bytes agregados a la trama.
     */
    public abstract int putBytes(InternalFormat internalFormat, VariableByteBuffer frame);

    /**
     * Lee de la trama {@code frame}, los bytes correspondiente a este campos y los
     * traslada al objeto {@code internalFormat}. Para este fin utiliza un arreglo de
     * bytes que es pasado por parametro y un entero que indica la posicion del byte desde
     * donde deberia empezar a leer los bytes a partir de los cuales le dara foram al
     * campo.
     *
     * @param internalFormat Es el formato interno donde dbera colocarase el nuevo campo
     * leido desde la trama.
     * @param frame Es la trama desde donde se leera el campo.
     * @param posicion Es la posicion desde deberá empezar a leer este campo.
     * @return La nueva posicion desde donde debera empezar a leer el siguiente campo.
     */
    public abstract int readBytes(InternalFormat internalFormat,
            VariableByteBuffer frame, int posicion);

    /**
     * Devuelve el identificador de este campo el cual ha sido asignado en la
     * confiuracion.
     *
     * @return el identificador de este campo.
     */
    public final String getId() {
        return id;
    }

    /**
     * Asigna un identificador para este campo. Este identificador debe ser unico
     * para el contexto en el cual se ejecuta.
     * @param id Identificador del Campo
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Devueve el valor por omision asignado en la configuracion a este objeto
     *
     * @return El texto contenido en la configuracion o null en caso de contener
     * subelementos.
     */
    public String getText() {
        return text;
    }

    /**
     * Devuelvo el tipo de campo. Es decir, parte del nombre de la clase que se
     * instanciara aunque no el nombre completo (no incluye el paquete). La regla por
     * omision de nombramiento de la clase es "type" + Field.
     *
     * @return El nombre del tipo configurado para el campo.
     */
    public String getType() {
        return type;
    }

    /**
     * Devuelve la clave configurada para el campo, el cual se usara para obtener un
     * parametro de las propiedades recibidas al crear un FI de la configuracion.
     *
     * @return Una cadena conteniendo la clave a usar, null en caso no haber sido
     * configurado dicho parametro.
     */
    public String getKeyParam() {
        return keyParam;
    }

    /**
     * Este metodo devuelve el el valor del parametro recibido basado en la clave
     * configurada para este proposito o en caso de no haber sido configurada, generar un
     * formato de clave el cual es el path correspondiente a este campo. Por ejemplo, si
     * se tiene el FI siguiente:
     * <pre><code>
     * +>ID=NULL, VALUES=
     * +--->ID='path01', VALUES=
     * +------>ID='path02', VALUES=
     * +--------->ID='0102_1', VALUE=[001]
     * +--------->ID='0102_2', VALUE=[002]
     * +--------->ID='0102_3', VALUE=[002]
     * </code></pre> La clave por omision para el campo "0102_3" sera:
     * path01.path.02.0102_3
     *
     * @param params El properties que de donde se obtiene el valor del parametro
     * @param intFmtCtx El FI del contexto. Este podria estar anidado a otro FI
     * @return El valor de la clave partir de "params" o null en caso no existir dicho
     * valor en "params"-
     */
    protected String getParamValue(Properties params, InternalFormat intFmtCtx) {
        String keyCase;

        keyCase = (keyParam == null)
                ? (intFmtCtx == null) ? null : intFmtCtx.makePath(getId())
                : keyParam;
        keyCase = (params == null)
                ? null
                : params.getProperty(keyCase);

        return keyCase;
    }

    /**
     * Este metodo evalua si se ha configurado una clave cuyo valor asociado se obtendra
     * de la variable "params", en caso de no encontrarlo, se procedera a obtener tal como
     * lo realiza el metodo {@link #getValue() }
     *
     * @param params Un properties con las clave-valor de parametros a partir de los
     * cuales los campos pueden variar sus valores fijos y permitir la creacion
     * configurable de FI's
     * @param intFmtCtx El FI de contexto desde el cual se va formando por cada campo.
     * @return El valor preprocesado correspondiente a este campo, el cual pudo haberse
     * obtenido de los parametros, haberse parseado u obtenido de la configuracion.
     * @throws ParseException Si este campo ha sido marcado para parsear una macro a
     * partir del valor fijo y esta macro no pudo ser ubicada, sus argumentos eran
     * incorrectos o hubo un problema adicional en el parseo.
     */
    protected String getValue(Properties params, InternalFormat intFmtCtx) {
        String value;

        value = getParamValue(params, intFmtCtx);
        value = (value == null) ? getValue() : value;

        return value;
    }
    
    /**
     * Este metodo evalua si el valor exista, el cual se procedera con el 
     * filtrado, caso contrario, no aplicara el filtro y retornara lo recibido.
     *
     * @param value El valor a filtrar
     * @return El valor filtrado preprocesado correspondiente a este campo.
     */
    protected String applyFilter(String value) {
        return (protectable) ? filter.filter(value) : value;
    }
    
    /**
     * Este metodo filtra un formato interno partiendo de una ruta base.
     *
     * @param basePath Ruta base
     * @param intFmt Formato interno a filtrar
     * @return El valor filtrado correspondiente al formato interno.
     */
    protected String applyFilter(String basePath, InternalFormat intFmt) {
        return filters.filter(formatterId, basePath, intFmt);
    }
    
    
    /**
     * Este metodo internamente recibe el texto contenido en la configuracion el cual sera
     * filtrado por el parser de forma que pueda ser preprocesado previo a su conversion a
     * bytes a partir del metodo {@link #putBytes }
     *
     * @return El valor preprocesado correspondiente a este campo, previamente parseado.
     * Por ejemplo asignando valores automaticos de fecha, hora, secuencial, etc.
     * @throws ParseException Si este campo ha sido marcado para parsear una macro a
     * partir del valor fijo y esta macro no pudo ser ubicada, sus argumentos eran
     * incorrectos o hubo un problema adicional en el parseo.
     */
    protected String getValue() {
        if (parseable) {
            log.debug("[" + this.id + "]Parseando texto=" + getText());
            return parserMacro.parse(this.getText());
        } else {
            return this.getText();
        }

    }
        
    /**
     * Indica si este campo tendra un valor de configuracion considerado nulo y que por
     * tanto no debera ser considerado al crear el InternalFormat de la configuracion.
     */
    public boolean isNullable() {
        return nullable;
    }

    /**
     * Indica si el valor almacenado debera ser protegido. Para aquellos campos 
     * que tengan configurado el atributo "protec".
     * 
     * @return Si es protegido
     */
    public boolean isProtectable() {
        return protectable;
    }
    
    /**
     * Devuelve el filtro asociado a este campo.
     * @return Filtro asociado al campo
     */
    public Filter getFilter(){
        return this.filter;
    }
    
    /**
     * Devuelve un filtro de acuerdo al tipo de proteccion especificado. Este 
     * filtro es buscado del contexto de configuracion para los campos mediante
     * un tipo de proteccion.
     * 
     * @param typeProtec Tipo de proteccion, el cual es especificado en el 
     * atributo "protec"
     * @return Filtro asociado al campo
     */
    public Filter getFilterByProtec(String typeProtec) {
        return this.filters.getFilter(typeProtec);
    }
    
    /**
     * Devuelve el contexto de configuraion referenciado por este campo
     *
     * @return el contexto de configuracion de este campo
     */
    public FmtContext getConfigContext() {
        return configContext;
    }

    /**
     * Asigna un contexto de configuracion a este campo
     *
     * @param configContext
     */
    public void setConfigContext(FmtContext configContext) {
        this.configContext = configContext;
    }

    public final ParserMacro getParserMacro() {
        return parserMacro;
    }

    public final void setParserMacro(ParserMacro parserMacro) {
        this.parserMacro = parserMacro;
    }

    public Filters getFilters() {
        return this.filters;
    }

    public void setFilters(Filters filters) {
        this.filters = filters;
    }
        
    public String getParentPath() {
        return parentPath;
    }

    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }
    
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
        
    /**
     * {@inheritDoc  }
     * @return 
     */
    public String getContextPath(){
        return path;
    }
        
    /**
     * {@inheritDoc  }
     * @return 
     */
    public String getFilterPath(){
        return path;
    }

    public String getFormatterId() {
        return formatterId;
    }

    public void setFormatterId(String formatterId) {
        this.formatterId = formatterId;
    }
    
    @Override
    public String toString() {
        return "Field{"
                + "parserMacro=" + parserMacro
                + ", parentPath=" + parentPath
                + ", path=" + path
                + ", id=" + id
                + ", text=" + text
                + ", type=" + type
                + ", filter=" + filter
                + ", protectable=" + protectable
                + ", parseable=" + parseable
                + ", nullable=" + nullable
                + ", configContext=" + configContext
                + ", keyParam=" + keyParam
                + ", logId=" + logId + '}';
    }
}