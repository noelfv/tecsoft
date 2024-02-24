/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter;

import com.novatronic.formatter.context.FieldConfigurationContext;
import com.novatronic.formatter.context.FmtContext;
import com.novatronic.formatter.field.Field;
import com.novatronic.formatter.field.FieldFactory;
import com.novatronic.formatter.filter.Filters;
import com.novatronic.formatter.internal.InternalFormat;
import com.novatronic.formatter.macros.ParserMacro;
import com.novatronic.formatter.util.VariableByteBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 *
 * @author ofernandez
 * @version 1.0
 * @since 1.0, 05/11/2010
 */
public abstract class Formatter implements FieldConfigurationContext {

    private static final Logger log = Logger.getLogger(Formatter.class);
    private String id;
    private Map<String, Field> fields;
    private ParserMacro parserMacro;
    private FmtContext configContext;
    private Filters filters;
    private String parentPath;

    public interface Attr {

        public static final String ID = "id";
        public static final String NAME = "name";
        public static final String TYPE = "type";
    }

    public interface Tag {

        public static final String FIELD = "field";
        
    }

    public Formatter() {
        configContext = new FmtContext();
        fields = new LinkedHashMap<String, Field>();
        parentPath = "";
        filters = new Filters();
    }

    /**
     * Lee la configuracion de un formato de trama
     *
     * @param formatterconfig Es el elemento de JDom el cual contiene dicho formato
     */
    public final void readConfiguration(Element formatterconfig) {
        log.debug("Leyendo configuracion...");
        id = formatterconfig.getAttributeValue(Attr.ID);
        log.debug(">>>>>>>>>ID=" + id + ":Leyendo Custom configuration y FieldsConfiguration");
        readCustomConfiguration(formatterconfig);
        readFieldsConfiguration(formatterconfig.getChildren(Tag.FIELD));
        log.debug("<<<<<<<<<ID=" + id + ":Configuracion leida");
    }

    /**
     * Devuelve al Parser asociado a este formateador
     *
     * @return El parser asociado, null en caso no tener ninguno asociado.
     */
    public ParserMacro getParserMacro() {
        return parserMacro;
    }

    public FmtContext getConfigContext() {
        return configContext;
    }

    public String getContextPath() {
        return parentPath;
    }

    public String getFilterPath() {
        return parentPath;
    }
    
    public Filters getFilters() {
        return filters;
    }

    /**
     * Asigna un nuevo parser a este formateador
     *
     * @param parserMacro el parser por asignar
     */
    public void setParserMacro(ParserMacro parserMacro) {
        this.parserMacro = parserMacro;
    }
    
    public void setFilters(Filters filters) {
        this.filters = filters;
    }
    
    /**
     * Lee configuracion asociada a este formateador, teniendo en cuenta campos o
     * atributos adicionales a este.
     *
     * @param element Es el objeto a partir del cual se leera la configuracion.
     */
    protected abstract void readCustomConfiguration(Element element);

    /**
     * Actualiza el objeto internalFormat que se genera a partir de la configuracion. Para
     * esto fin se utiliza el objeto recibido como parametro: {@code updateInternalFormat}
     * y desde el cual se obtendrán los nuevos valores de los campos relacionados a la
     * trama. Este objeto NO agrega un nuevo Campo en caso no exista en la configuracion,
     * pero a pesar de ello si lo agrega al objeto InternalFormat exista o no en la
     * configuracion.
     *
     * @param updateInternalFormat El objeto desde el cual se actualizara o agregara los
     * nuevos valores de los campos.
     * @return La trama generada a partir de los campos configurados.
     * @throws CreateFrameException
     */
    public abstract VariableByteBuffer getFrames(InternalFormat updateInternalFormat);

    /**
     * Actualiza el objeto internalFormat que se genera a partir de la configuracion. Para
     * esto fin se utiliza el objeto recibido como parametro: {@code updateInternalFormat}
     * y desde el cual se obtendrán los nuevos valores de los campos relacionados a la
     * trama. Este objeto NO agrega un nuevo Campo en caso no exista en la configuracion,
     * pero a pesar de ello si lo agrega al objeto InternalFormat exista o no en la
     * configuracion.
     *
     * @param updateInternalFormat El objeto desde el cual se actualizara o agregara los
     * nuevos valores de los campos.
     * @return La trama generada a partir de los campos configurados como un arreglo de
     * bytes.
     */
    public final byte[] getFramesAsArray(InternalFormat updateInternalFormat) {
        return getFrames(updateInternalFormat).getByteArray();
    }

    /**
     * Este crea una trama pero sin necesidad de tener como parametro un objeto
     * {@link InternalFormat} pues asume que debera obtener los datos a partir de los
     * campos fijos de la configuracion. Para esto creara internamente un internalFormat a
     * partir de dicha configuracion. La trama sera creada estrictamente desde los datos
     * de la configuracion. Se debe tener especial cuidado en tener valores fijos
     * agregados pues al realizar el procesado de campos podria generar un error en estos
     * y por ende en la generacion de la trama.
     *
     * @throws FormatterException
     * @return El ByteBuffer correspondiente a la trama generada.
     */
    public abstract VariableByteBuffer getFrames();

    /**
     * Este crea una trama pero sin necesidad de tener como parametro un objeto
     * {@link InternalFormat} pues asume que debera obtener los datos a partir de los
     * campos fijos de la configuracion. Para esto creara internamente un internalFormat a
     * partir de dicha configuracion. La trama sera creada estrictamente desde los datos
     * de la configuracion. Se debe tener especial cuidado en tener valores fijos
     * agregados pues al realizar el procesado de campos podria generar un error en estos
     * y por ende en la generacion de la trama.
     *
     * @return Un arreglo de bytes correspondiente a la trama generada.
     */
    public final byte[] getFramesAsArray() {
        return getFrames().getByteArray();
    }

    /**
     * Crea un objeto InternalFormat a partir de un arreglo de bytes pasado por parametro.
     * Se asume que el arreglo recibido es un la trama recibida y tiene un formato legible
     * por este formateador. Si la trama recibida no tiene un formato que sea legible para
     * este, se recibira una excepcion.
     *
     * @param frame
     * @return
     * @throws ReadFrameException
     */
    public abstract InternalFormat createInternalFormatFromFrame(
            VariableByteBuffer frame);

    /**
     *
     * @param frame
     * @return
     * @throws FormatterException
     */
    public final InternalFormat createInternalFormatFromFrame(byte[] frame) {
        VariableByteBuffer varFrame = new VariableByteBuffer();
        varFrame.append(frame, 0, frame.length);
        return createInternalFormatFromFrame(varFrame);
    }

    /**
     * Este metodo permite crear unFI a partir de la trama pasado como parametro.
     * Internamente la cadena recibida es agregada un objeto VariableByteBuffer para de
     * esta forma ser procesada por el metodo {@link #createInternalFormatFromFrame(
     * com.novatronic.formatter.util.VariableByteBuffer) createInternalFormatFromFrame(
     * VariableByteBuffer)}
     *
     * @param frame Es la trama a ser analizada
     * @return Un FI con lo datos obtenidos a partir de la trama recibida
     * @throws ReadFrameException En caso se obtena algun error al analizar al trama
     */
    public final InternalFormat createInternalFormatFromFrame(String frame) {
        VariableByteBuffer varFrame = new VariableByteBuffer();
        varFrame.add(frame);
        return createInternalFormatFromFrame(varFrame);
    }

    public abstract InternalFormat createInternalFormatFromFrame(VariableByteBuffer frame, InternalFormat intFormat);

    /**
     * Crea un objeto internalFormat a partir de la configuracion relacionada con este
     * formateador. Los valores de cada campo se obtienen a partir de los valores fijos de
     * este (texto del tag field) o segun el comportamiento de este campo.
     *
     * @return El objeto internalFormat creado.
     */
    public abstract InternalFormat getInternalFormatFromConfig();

    /**
     * Crea un objeto internalFormat a partir de la configuracion relacionada con este
     * formateador o segun el comportamiento de cada campo. Los valores de cada campo se
     * obtienen a partir de los valores fijos de este. Por ejemplo:<br /><br />
     * Un campo puede tener la siguiente configuracion:<br />
     * <code>&lt;field id="01" type="Fixed" length="4"&gt;<b>FFFF</b>&lt;/field&gt;</code>
     * <br /><br />
     * Siendo <b>FFFF</b> el valor constante el cual sera asignado al formato creado desde
     * la configuracion. Pero existen casos en el cual no es posible construir el
     * InternalFormat debido a que existen variantes sobre su construccion como es el caso
     * del campos Case. Para estos casos es posible pasar parametros a manera de
     * properties que nos permitan ayudarle a discernir la forma como debe crear dicho
     * InternalFormat. Este properties tendra las claves que seran leidos por los campos
     * que requieren ayuda para dicha construccion. Ver mas informacion en la
     * configuracion de cada campo.
     *
     * @param params Un properties con valores a ser leidos por los campos.
     * @return El objeto internalFormat creado.
     */
    public abstract InternalFormat getInternalFormatFromConfig(Properties params);

    /**
     * Crea un objeto internalFormat a partir de la configuracion relacionada con este
     * formateador o segun el comportamiento de cada campo. Los valores de cada campo se
     * obtienen a partir de los valores fijos de este. Por ejemplo:<br /><br />
     * Un campo puede tener la siguiente configuracion:<br />
     * <code>&lt;field id="01" type="Fixed" length="4"&gt;<b>FFFF</b>&lt;/field&gt;</code>
     * <br /><br />
     * Siendo <b>FFFF</b> el valor constante el cual sera asignado al formato creado desde
     * la configuracion. Pero existen casos en el cual no es posible construir el
     * InternalFormat debido a que existen variantes sobre su construccion como es el caso
     * del campos Case. Para estos casos es posible pasar parametros a manera de un
     * arreglo que nos permitan ayudarle a discernir la forma como debe crear dicho
     * InternalFormat. Este arreglo tendra las claves que seran leidos por los campos que
     * requieren ayuda para dicha construccion. Ver mas informacion en la configuracion de
     * cada campo.<br/><br/>
     * Un ejemplo de esta llamada seria el siguiente:<br/>
     * <code>
     * Formatter fmt;:<br/>
     * InternalFormat intFmt;:<br/>
     * //...:<br/>
     * intFmt = fmt.getInternalFormatFromConfig("clave1", "valor1", "clave2",
     * "valor2");<br/>
     * </code><br/>
     * En el ejemplo estamos indicando que clave1=valor1, clave2=valor2; y que seran
     * trasladados a los campos que requieran dichos valores para dichas claves.
     *
     * @param values Un arreglo conteniendo una cantidad para de String, siendo los
     * impares las claves y los inmediatos pares el valor para dicha clave.
     * @return El objeto internalFormat creado.
     * @throws IllegalArgumentException Si el arreglo no tiene una cantidad para de
     * elementos
     */
    public InternalFormat getInternalFormatFromConfig(String... values) {
        Properties params;
        String key;
        String value;

        log.trace("Procesando valores clave-valor...");
        params = new Properties();
        if (values.length % 2 != 0) {
            throw new IllegalArgumentException("La cantidad de argumentos variables debe ser múltiplo de 2");
        }
        for (int i = 0; i < values.length; i += 2) {
            key = values[i];
            value = values[i + 1];
            LogMF.trace(log, "Agregando clave=[{0}], valor=[{1}]", key, value);
            params.put(key, value);
        }

        return getInternalFormatFromConfig(params);

    }

    /**
     * Crea una trama a partir del Internal format pasado como parametro. Este
     * internalFormat debe tener necesariamente todos los valores necesarios para generar
     * la trama. En este sentido, una secuencia validad de uso seria la
     * siguiente:<br/><br/>
     * <pre>
     * {@code
     * Formatter format = formatterFactory.getFormatter("ID_FORMATO");
     * InternalFormat intFormat = format.getInternalFormatFromConfig();
     *
     * //... agregando campos adicionales
     *
     * VariableByteBuffer buffer = format.getFrameFromInternalFormat(intFormat);
     * }
     * </pre> En la segunda linea se obtiene el FI a partir de la configuracion lo cual
     * garantiza que este FI tiene los datos que se consideran constantes y que residen en
     * dicho lugar. La ultima linea realiza la creacion de la trama usando el metodo em
     * cuestion el cual asume que el FI pasado como parametro contiene la informacion
     * necesaria y suficiente para crear el la trama.
     *
     * @param intFormat El formato interno que contiene los datos suficientes y necesarios
     * para la creacion de la trama
     * @return La trama creada a partir de lo configurado para este formato
     * @throws CreateFrameException Si se produce un error al genera la trama
     * @see FormatterFactory
     * @see #getInternalFormatFromConfig()
     */
    public abstract VariableByteBuffer getFrameFromInternalFormat(InternalFormat intFormat);

    /**
     * Retorna una lista de todos los campos cargados a partir de la configuracion.
     *
     * @return Una lista conteniendo los campos realacionados a este Formateador
     */
    protected final List<Field> getFieldsAsList() {
        return new ArrayList<Field>(fields.values());
    }

    private void readFieldsConfiguration(List<Element> fieldsAsList) {
        log.debug("Procesando la configuracion de los campos...");
        for (int i = 0; i < fieldsAsList.size(); i++) {
            Field field = FieldFactory.getField(fieldsAsList.get(i), this);
            addField(field);
            log.debug("Agregado[name=" + field.getType() + ",id="
                    + field.getId() + "]");
        }
        log.debug("Configuracion terminada para el formateador " + getId());
    }

    /**
     * Agrega un nuevo campo al formateador.
     *
     * @param field El campo que se agregara.
     */
    protected final void addField(Field field) {
        fields.put(field.getId(), field);
    }

    /**
     * Devuelve un campo creado a partir de la configuracion.
     *
     * @param id Identificador del campo indicado en la configuracion
     * @return El campo asociado a este {@code id} o null en caso de no existir-
     */
    protected final Field getField(String id) {
        return fields.get(id);
    }

    /**
     * Devuelve el identificador de este formateador asignado en la configuracion.
     *
     * @return El identificador.
     */
    public final String getId() {
        return id;
    }

    /**
     * Devuelve la cantidad campos cargados y configurados para este formateador
     *
     * @return La cantidad de campos que maneja esta instancia.
     */
    public int getSize() {
        return fields.size();
    }
    
    /**
     * {@inheritDoc} <br/><br/>En este caso, devuelve el identificador de este 
     * formateador asignado en la configuracion para que este disponible para 
     * los campos del mismo.
     * @see #getId()
     */
    public String getFormatterId(){
        return id;
    }
}
