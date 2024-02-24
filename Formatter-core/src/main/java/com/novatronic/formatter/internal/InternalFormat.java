/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.internal;

import com.novatronic.formatter.exception.FieldValueException;
import com.novatronic.formatter.exception.InvalidFieldTypeException;
import com.novatronic.formatter.support.IntFormatPrint;
import java.util.*;
import java.util.Map.Entry;
import org.apache.log4j.Logger;

/**
 * Esta clase permite manejar los datos a ser usados por el formateador de manera
 * independiente al mismo de forma que pueda usarse para distintos formateadores. Este
 * objeto le ofrece ademas un contexto de ejecucion.
 *
 * @author ofernandez
 * @version 2.1.0 - 2013
 * @since 1.0, 05/11/2010
 */
public class InternalFormat extends InternalField implements Map<String, Object> {

    private static final Logger log = Logger.getLogger(InternalFormat.class);
    private Map<String, InternalField> fields;
    private InternalFormat parent;
    private static final String SUBKEY_TOKEN = "\\.";

    //--- INIT ---- Contructores ------------
    public InternalFormat() {
        fields = new LinkedHashMap<String, InternalField>();
    }

    /**
     * Crean un objeto InternalFormat
     *
     * @param id
     */
    public InternalFormat(String id) {
        setId(id);
        fields = new LinkedHashMap<String, InternalField>();
        log.trace("InternalFormat creado, id=" + getId());
    }

    /**
     * Crea un objeto internal format a partir de un objeto del tipo Map el cual
     * necesariamente debe estar formado por claves del tipo String y valores que puedan
     * ser String u otro objeto Map de caracteristicas similares a este
     *
     * @param map Objeto a partir del cual se extraeran los valores a usar para la
     * creacion del objeto InternalFormat.
     * @param id Es el Indentificador de este InternalFormat. Se usar este valor en caso
     * se desee anidar este objeto en otro InternalFormat.
     * @throws FieldValueException Si el objeto Map por agregar no contiene valores que
     * sean String u otro objeto Map.
     */
    public InternalFormat(Map<String, ? extends Object> map, String id) {
        this(id);
        addMapToInternalFormat(map);
        log.trace("InternalFormat creado, id=" + getId());
    }

    /**
     * Crea un objeto internal format a partir de un objeto del tipo Map el cual
     * necesariamente debe estar formado por claves del tipo String y valores que puedan
     * ser String u otro objeto Map de caracteristicas similares a este
     *
     * @param map Objeto a partir del cual se extraeran los valores a usar para la
     * creacion del objeto InternalFormat.
     * @throws FieldValueException Si el objeto Map por agregar no contiene valores que
     * sean String u otro objeto Map.
     */
    public InternalFormat(Map<String, ? extends Object> map) {
        this();
        addMapToInternalFormat(map);
    }

    //--- END ---- Contructores ------------
    /**
     * Agrega un objeto internalFormat a partir de un objeto del tipo Map el cual
     * necesariamente debe estar formado por claves del tipo String y valores que puedan
     * ser String u otro objeto Map de caracteristicas similares a este
     *
     * @param map Objeto a partir del cual se extraeran los valores a usar para la
     * creacion del objeto InternalFormat.
     * @throws FieldValueException Si el objeto Map por agregar no contiene valores que
     * sean String u otro objeto Map. Map<? extends String, ? extends Object> m
     */
    private void addMapToInternalFormat(Map<? extends String, ? extends Object> map) {
        for (Iterator<? extends String> it = map.keySet().iterator(); it.hasNext();) {
            String id = it.next();
            Object value = map.get(id);
            if (value instanceof String) {
                InternalField intField = new InternalField(id, (String) value);
                fields.put(id, intField);
                //log.trace("Agregado id=" + id + ", value=" + value);
            } else if (value instanceof Map) {
                InternalFormat intFormat = new InternalFormat(id);
                addMapAsInternalField(intFormat, (Map) value);
                addInternalField(intFormat);
            } else {
                throw new FieldValueException("El campo por agregar no es del "
                        + "tipo String o Map");
            }
        }
    }

    /**
     * Agrega un elemento Map como un internalField. Para lograr esto se debe trasladar
     * los campos del Map como InternalField.
     *
     * @param map El mapa por agregar
     * @param intFormat El intFormat al cual debe trasladarse
     * @throws FieldValueException Si alguno de los contenidos del mapa por agregar no es
     * un String o un Map.
     */
    private void addMapAsInternalField(InternalFormat intFormat, Map<String, ? extends Object> map) {
        String id;
        Object value;
        InternalField intField;
        InternalFormat subintFormat;

        for (Iterator<String> it = map.keySet().iterator(); it.hasNext();) {
            id = it.next();
            value = map.get(id);
            if (value instanceof String) {
                intField = new InternalField(id, (String) value);
                intFormat.addInternalField(intField);
                //log.trace("Agregado id=" + id + ", value=" + value);
            } else if (value instanceof Map) {
                subintFormat = new InternalFormat(id);
                addMapAsInternalField(subintFormat, (Map) value);
                intFormat.addInternalField(subintFormat);
            } else {
                throw new FieldValueException("El valor por agregar no es del "
                        + "tipo String o Map para la clave=" + id);
            }
        }
    }

    /**
     * Retorna todos los valores que se mantienen internamente en el Objeto InternalFormat
     * como una lista ordenada segun su orden interno el cual es el orden de aparicion en
     * la ocnfiguracion.
     *
     * @return La lista de objetos.
     */
    public List<InternalField> getInternalFieldsAsList() {
        return new ArrayList<InternalField>(fields.values());
    }

    /**
     * Retornal una lista que contiene los identificadores de todos los objetos que son
     * manejados por este objeto. Tener en cuenta que si se tiene campos que son
     * agrupadores de campos, estos seran devueltos como parte del campo.
     *
     * @return Una lista que contiene los identificadore de los campos almacenados en este
     * objeto.
     */
    public List<String> getIdsAsList() {
        return new ArrayList<String>(fields.keySet());
    }

    /**
     * Retorna un objeto interno el cual es accedido por su identificador interno el cual
     * es el indicado en la configuracion. En caso de no encontrarse el objeto se
     * devolvera un objeto null. Debe tenerse en cuenta que el objeto, puede podria ser
     * del tipo IntenalFormat
     *
     * @param id el identificador del objeto interno a consultar.
     * @return El objeto correspondiente a este identificador o null en caso de no
     * encontrarse el objeto.
     */
    public InternalField getInternalField(String id) {
        return fields.get(id);
    }

    /**
     * Retorna el valor de un InternalField el cual es accedido a traves de una ruta bajo
     * el formato:<br><br> [/]&lt;IDEN01&gt;[.&lt;IDEN02&gt;.&lt;IDEN03&gt;...]<br><br>
     * Una secuencia como la anterior (cadena de Identificadores) presupone que se esta
     * navegando a traves de objetos InternalFormat hasta llegar al objeto deseado. En
     * caso de no encontrarse el objeto se devolvera null, eso es si el objeto mismo no
     * esta o si la ruta no puede seguirse en algun punto intermedio. Ademas se debe tener
     * en cuenta el simbolo "/" el cual, en caso de presentarse, indicara que se obtendra
     * un valor pero cuya path se debe recorrer desde el FI raiz de este FI (lo cual es
     * distinto al padre de este FI, en caso lo tenga)
     *
     * @param path Es la ruta a seguir para obtener el objeto
     * @return El valor del objeto correspondiente a este identificador o null en caso de
     * no encontrarse el objeto. Esto ultimo podria ser tambien porque la ruta no puede
     * seguirse a mitad de esta, es decir, no se encontro el objeto.
     * @throws InvalidFieldTypeException Si el valor buscando segun el path recibido es un
     * InternalFormat y por tanto este no tiene un valor valido asociado. O si al buscar
     * en la ruta recibido, durante este camino, los identificadores intermedios no
     * representan un IF
     * @since 2.1.0
     */
    public String getValue(String path) {
        InternalField field;

        field = get(path);
        if (field instanceof InternalFormat) {
            throw new InvalidFieldTypeException("El path=[" + path + "] corresponde a un IF");
        } else {
            return (field == null) ? null : field.getValue();
        }
    }

    /**
     * Retorna un objeto InternalFormat el cual es accedido a traves de una ruta bajo el
     * formato:<br><br> <span
     * style="color:green;font-weight:bold;">[/]&lt;IDEN01&gt;[.&lt;IDEN02&gt;.&lt;IDEN03&gt;...]</span><br><br>
     * Una secuencia como la anterior (cadena de Identificadores) presupone que se esta
     * navegando a traves de objetos InternalFormat hasta llegar a la ruta deseado. En
     * caso de no encontrarse el objeto, se devolvera null.
     *
     * @param path Es la ruta a seguir para obtener el objeto
     * @return El valor del objeto correspondiente a este identificador o null en caso de
     * no encontrarse el objeto. Esto ultimo podria ser tambien porque la ruta no puede
     * seguirse a mitad de esta, es decir, no se encontro el objeto.
     * @throws InvalidFieldTypeException Si el valor buscando segun el path recibido es un
     * InternalFormat y por tanto este no tiene un valor valido asociado. O si al buscar
     * en la ruta recibido, durante este camino, los identificadores intermedios no
     * representan un IF
     */
    public InternalFormat getIFmt(String path) {
        InternalField field;

        field = get(path);
        if (field instanceof InternalFormat || field == null) {
            return (field == null) ? null : (InternalFormat) field;
        } else {
            throw new InvalidFieldTypeException("El path=[" + path + "] NO corresponde a un IF");
        }
    }

    /**
     * Retorna un objeto InternalField el cual es accedido a traves de una ruta bajo el
     * formato:<br><br> <span
     * style="color:green;font-weight:bold;">[/]&lt;IDEN01&gt;[.&lt;IDEN02&gt;.&lt;IDEN03&gt;...]</span><br><br>
     * Una secuencia como la anterior (cadena de Identificadores) presupone que se esta
     * navegando a traves de objetos InternalFormat hasta llegar a la ruta deseado. En
     * caso de no encontrarse el objeto, se devolvera null.
     *
     * @param path Es la ruta a seguir para obtener el objeto
     * @return El valor del objeto correspondiente a este identificador o null en caso de
     * no encontrarse el objeto. Esto ultimo podria ser tambien porque la ruta no puede
     * seguirse a mitad de esta, es decir, no se encontro el objeto.
     * @throws InvalidFieldTypeException Si al buscar en la ruta recibido, durante este
     * camino, los identificadores intermedios no representan un IF
     */
    public InternalField get(String path) {
        String pathTemp;
        //TODO: REvisar funcionalidad..:Excepciones lanzadas
        pathTemp = path;
        if (path.startsWith("/")) {
            return getFromRoot(path);
        } else {
            if (pathTemp.indexOf('.') == -1) {
                return getFromSimpleId(pathTemp);
            } else {
                return getFromToken(pathTemp);
            }
        }
    }

    private InternalField getFromRoot(String path) {
        String pathTemp;
        InternalFormat ifmt;

        log.trace("Se traslada la operacion al IF ROOT");
        ifmt = getParentRoot();
        pathTemp = path.substring(1);
        return ifmt.get(pathTemp);
    }

    private InternalField getFromSimpleId(String path) {
        InternalField field;

        log.trace("El path es un Id:" + path);
        field = fields.get(path);
        return (field == null) ? null : field;
    }

    private InternalField getFromToken(String pathTemp) {
        String[] claves;
        InternalField field;

        claves = pathTemp.split(SUBKEY_TOKEN);
        log.trace("Buscando en profundidad, path=" + pathTemp);
        field = this;
        for (int position = 0; position < claves.length; position++) {
            log.trace("Buscando clave=" + claves[position]);
            field = getIntFieldFromIntFormat(field, claves[position]);
            if (field == null) {
                log.trace("Clave no encontradoa, path=" + claves[position]);
                return null;
            }
        }
        return (field == null) ? null : field;
    }

    private InternalField getIntFieldFromIntFormat(InternalField intField, String subId) {
        if (intField instanceof InternalFormat) {
            return ((InternalFormat) intField).getInternalField(subId);
        } else {
            throw new InvalidFieldTypeException("El campo con id=" + subId + " no es un IF");
        }
    }

    /**
     * Agrega un nuevo campo a los manejados internamente en este objeto. Si ya se tiene
     * previamente un campo con el mismo identificador del campo por agregar, se
     * actualizara el antiguo valor al nuevo valor recibido. Además, si el campo por
     * agregar es del tipo InternalFormat, se asignará como padre este objeto.
     *
     * @param value El objeto InternalField por agregar al InternalFormat.
     */
    public InternalField addInternalField(InternalField value) {
        if (value instanceof InternalFormat) {
            ((InternalFormat) value).setParent(this);
        }
        return fields.put(value.getId(), value);
    }

    /**
     * Este metodo devuelve el path de este formato interno encadenado con el id pasado
     * como parametro generando un path final con dicho valor. Por ejemplo,Para un FI como
     * el siguiente:<br/><br/>
     * <code><pre>
     * +>ID=NULL, VALUES=
     * +--->ID='path01', VALUES=
     * +------>ID='path02', VALUES=
     * +--------->ID='path03', VALUES=
     * +------------>ID='path04', VALUES=
     * +--------------->ID='path05', VALUES=
     * +------------------>ID='filed0102030405_1', VALUE=[001]
     * +------------------>ID='filed0102030405_2', VALUE=[002]
     * +------------------>ID='filed0102030405_3', VALUE=[002]
     *
     * //Si estamos en el FI de ID=path05
     * String path = fmtInt.getPath();
     * //Obtendremos: "path01.path02.path03.path04.path05"
     * //Si estamos en el FI de ID=path01
     * String path = fmtInt.getPath();
     * //Obtendremos: "path01"
     * //Si estamos en el FI raiz, usualmente de ID=NULL
     * String path = fmtInt.getPath();
     * //Obtendremos: ""
     * </pre></code> Esto es util para poder saber la profundidad del mismo o poder
     * acceder o generar algun path alternativo.
     *
     * @return EL path crrespondiente a este formato interno
     */
    public String getPath() {
        InternalFormat intFmt;
        String path;

        intFmt = this;
        path = intFmt.getId() == null ? "" : intFmt.getId();
        while (!intFmt.isParentRoot()) {
            intFmt = intFmt.getParent();
            path = intFmt.isParentRoot() ? path : intFmt.getId() + "." + path;
        }
        return path;

    }

    /**
     * Este metodo realiza la misma operacion que el metodo {@link #getPath() } con la
     * diferenecia de que el id pasado como parametro como parte de la ruta final. Por
     * ejemplo, si tenemos un FI como el siguiente:<br/><br/>
     * <pre><code>
     * +>ID=NULL, VALUES=
     * +--->ID='path01', VALUES=
     * +------>ID='path02', VALUES=
     * +--------->ID='filed0102_1', VALUE=[001]
     * +--------->ID='filed0102_2', VALUE=[002]
     *
     * //y aplicando este metodo para el FI de id="path02"
     * String path=intFmt.makePath("test");
     * //Obtenedremos path="path01.path02.test"
     * </code></pre>
     * @return EL path correspondiente a este formato interno encadenado con el id pasado
     * por parametro
     * @since 2.1.0
     */
    public String makePath(String id) {
        String path;

        path = getPath();
        if (path.equals("")) {
            path = id;
        } else {
            path = getPath() + "." + id;
        }

        return path;
    }

    /**
     * Devuelve el InternalFormat raiz para el caso de tener InternalFormat's anidados. Si
     * este objeto no es parte de un anidamiento se devolvera este mismo objeto.
     *
     * @return El mismo objeto si este no esparte de un anidamiento de InternalFormat's.
     * El objeto raiz del anidamiento en caso de ser parte de un anidamiento.
     */
    public InternalFormat getParentRoot() {
        if (isParentRoot()) {
            return this;
        } else {
            return parent.getParentRoot();
        }
    }

    /**
     * Indica si el objeto actual es la raiz de internalFormat's anidados. Se debe tener
     * en cuenta que si este objeto se agrega a un nuevo InternalFormat, automaticamente
     * tendra un padre y dejara de ser la raiz.
     *
     * @return True si este InternalFormat es la raiz, False en cualquier otro caso.
     * @see #addInternalField(com.novatronic.formatter.internal.InternalField)
     * addInternalField(InternalField)
     */
    public boolean isParentRoot() {
        return (parent == null);
    }

    /**
     * Agrega un nuevo campo a los manejados internamente en este objeto. Este campo sera
     * creado como un objeto del tipo InternalField a partir de los dos parametros
     * recibidos. Si ya se tiene previamente un campo con el mismo identificador del campo
     * por agregar, se actualizara el antiguo valor al nuevo valor recibido.
     *
     * @param id Identificador del objeto por agregar
     * @param value El valor del nuevo objeto por agregar.
     */
    private InternalField addInternalField(String id, String value) {
        return fields.put(id, new InternalField(id, value));
    }

    /**
     * Este metodo agrega un nuevo campo a este IF. Para esto se requiere un path y el
     * valor por agregar. El path puede tener las siguientes caracteristicas:<br><br> 1.
     * Puede ser un identificador<br> 2. Puede ser una ruta cuyos identificadores estan
     * separados por puntos. Esta ruta es recorrida a partir del contexto del actual
     * IF<br> 3. Puede forzar a que se siga la ruta a partir del elemento raiz.<br><br> En
     * general el formato es:<br><br>
     * [/]&lt;IDEN01&gt;[.&lt;IDEN02&gt;.&lt;IDEN03&gt;...]<br><br> Una secuencia como la
     * anterior (cadena de Identificadores) presupone que se esta navegando a traves de
     * objetos IF hasta llegar a la ruta deseado. En caso de no encontrarse algun
     * identificador intermedio (el cual deberia ser un IF), se creara para luego
     * proseguir por la ruta indicada.
     *
     * @param path Ruta en la cual se debera colocar el nuevo campo
     * @param value El valor a ser colocado en la ruta indicada
     * @return Este mismo objeto.
     * @since 2.1.0
     */
    public InternalFormat add(String path, String value) {
        InternalField intField;

        intField = new InternalField(null, value);

        return addIntField(path, intField);
    }

    /**
     * Este metodo agrega un IF a este IF. Para esto se requiere un path y el valor por
     * agregar. El path puede tener las siguientes caracteristicas:<br><br> 1. Puede ser
     * un identificador<br> 2. Puede ser una ruta cuyos identificadores estan separados
     * por puntos. Esta ruta es recorrida a partir del contexto del actual IF<br> 3. Puede
     * forzar a que se siga la ruta a partir del elemento raiz.<br><br> En general el
     * formato es:<br><br> [/]&lt;IDEN01&gt;[.&lt;IDEN02&gt;.&lt;IDEN03&gt;...]<br><br>
     * Una secuencia como la anterior (cadena de Identificadores) presupone que se esta
     * navegando a traves de objetos IF hasta llegar a la ruta deseado. En caso de no
     * encontrarse algun identificador intermedio (el cual deberia ser un IF), se creara
     * para luego proseguir por la ruta indicada.
     *
     * @param path Ruta en la cual se debera colocar el nuevo campo
     * @param intFormat El IF a ser colocado en la ruta indicada
     * @return Este mismo objeto.
     * @since 2.1.0
     */
    public InternalFormat add(String path, InternalFormat intFormat) {
        return addIntField(path, intFormat);
    }

    private InternalFormat addIntField(String path, InternalField intField) {
        String pathTemp;

        pathTemp = path;
        if (pathTemp.startsWith("/")) {
            addToRoot(pathTemp, intField);
        } else {
            if (pathTemp.indexOf('.') == -1) {
                addSimpleId(pathTemp, intField);
            } else {
                addByToken(pathTemp, intField);
            }
        }

        return this;
    }

    private void addToRoot(String path, InternalField intField) {
        InternalFormat parentIF;

        log.trace("Dejamos que el ROOT agregue el campo");
        parentIF = getParentRoot();
        path = path.substring(1);
        parentIF.addIntField(path, intField);
    }

    private void addSimpleId(String pathTemp, InternalField intField) {
        log.trace("El path es un ID, se agrega directamente=" + pathTemp);
        intField.setId(pathTemp);
        addInternalField(intField);
    }

    private void addByToken(String pathTemp, InternalField intField) {
        String[] claves;
        InternalFormat tempIF;
        InternalFormat parentIF;
        int index;

        parentIF = this;
        claves = pathTemp.split(SUBKEY_TOKEN);
        log.trace("Ubicando en profundidad, path=" + pathTemp);
        for (index = 0; index < claves.length - 1; index++) {
            tempIF = parentIF.getIFmt(claves[index]);
            if (tempIF == null) {
                log.trace("No se ubico=[" + claves[index] + "], se crea IF");
                tempIF = new InternalFormat(claves[index]);
                parentIF.addInternalField(tempIF);
            }
            parentIF = tempIF;
        }
        log.debug("Profundidad accedida, asignando=" + index);
        intField.setId(claves[index]);
        parentIF.addInternalField(intField);
    }

    /**
     * Si el formato interno no tiene padre, este retorna null. Esto mismo puede
     * consultarse a partir del metodo {@link #isParentRoot() isParentRoot()}
     *
     * @return InternalFormat si este posee un padre, NULL si este no tiene uno asignado
     * lo cual significaria que este el internalFormat ROOT.
     */
    public InternalFormat getParent() {
        return parent;
    }

    /**
     * Asigna un IF padre a este IF
     */
    public void setParent(InternalFormat parent) {
        this.parent = parent;
    }

    /**
     * {@inheritDoc}
     * @see IntFormatPrint#prettyPrint(com.novatronic.formatter.internal.InternalFormat, int) 
     */
    @Override
    public String toString() {
        return IntFormatPrint.prettyPrint(this, fields.size() * 30);
    }
    
    //---- INIT ---- Metodos implementados de la interfaz Map
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final InternalFormat other = (InternalFormat) obj;
        if (((this.getId() != null) && (other.getId() == null))
                || ((this.getId() == null) && (other.getId() != null))
                || ((this.getId() != null) && (other.getId() != null)
                && !this.getId().equals(other.getId()))) {
            return false;
        }
        if (this.fields != other.fields && (this.fields == null || !this.fields.equals(other.fields))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (this.fields != null ? this.fields.hashCode() : 0);
        return hash;
    }

    public int size() {
        return this.fields.size();
    }

    public boolean isEmpty() {
        return fields.isEmpty();
    }

    public boolean containsKey(Object key) {
        return fields.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return fields.containsValue(value);
    }

    public Object get(Object key) {
        InternalField value = fields.get(key);
        if (value == null) {
            return value;
        }
        if (!(value instanceof InternalFormat)) {
            return value.getValue();
        } else {
            return value;
        }
    }

    public Object remove(Object key) {
        InternalField value = fields.remove(key);
        if (value == null) {
            return value;
        }
        if (!(value instanceof InternalFormat)) {
            return value.getValue();
        } else {
            return value;
        }
    }

    public void clear() {
        fields.clear();
    }

    public Set<String> keySet() {
        return fields.keySet();
    }

    public Object put(String key, Object value) {
        InternalField obj;
        if (value instanceof InternalField) {
            ((InternalField) value).setId(key);
            return addInternalField((InternalField) value);
        } else if (value instanceof String) {
            return addInternalField(key, (String) value);
        } else if (value instanceof Map) {
            InternalFormat intFormat = new InternalFormat(key);
            addMapAsInternalField(intFormat, (Map) value);
            obj = fields.get(key);
            return obj;
        } else {
            throw new IllegalArgumentException("El Objeto por asignar no es del"
                    + " tipo adecuado:{String, Map, InternalField}");
        }
    }

    public void putAll(Map<? extends String, ? extends Object> m) {
        this.addMapToInternalFormat(m);
    }

    public Collection<Object> values() {
        return new ArrayList<Object>(fields.values());
    }

    public Set<Entry<String, Object>> entrySet() {
        Set set = new LinkedHashSet();
        Iterator<InternalField> iterator = fields.values().iterator();
        while (iterator.hasNext()) {
            set.add(new EntryInternalFormat(iterator.next()));
        }
        return set;
    }

    //---- END ---- Metodos implementados de la interfaz Map
    public class EntryInternalFormat implements Entry<String, Object>, Comparable {

        private InternalField intField;

        public EntryInternalFormat(InternalField intField) {
            this.intField = intField;
        }

        public String getKey() {
            return intField.getId();
        }

        public Object getValue() {
            if (!(intField instanceof InternalFormat)) {
                return intField.getValue();
            } else {
                return intField;
            }
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 59 * hash + (this.intField != null ? this.intField.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final EntryInternalFormat other = (EntryInternalFormat) obj;
            if (this.intField != other.intField && (this.intField == null || !this.intField.equals(other.intField))) {
                return false;
            }
            return true;
        }

        public Object setValue(Object value) {
            InternalField obj;
            if (value instanceof InternalField) {
                return fields.put(intField.getId(), (InternalField) value);
            } else if (value instanceof String) {
                return fields.put(intField.getId(), new InternalField(intField.getId(), (String) value)).getValue();
            } else if (value instanceof Map) {
                InternalFormat intFormat = new InternalFormat(intField.getId());
                addMapAsInternalField(intFormat, (Map) value);
                obj = fields.put(intField.getId(), intFormat);
                if (!(obj instanceof InternalFormat)) {
                    return obj.getValue();
                } else {
                    return obj;
                }
            } else {
                throw new IllegalArgumentException("El Objeto por asignar no es del"
                        + " tipo adecuado:{String, Map, InternalField}");
            }
        }

        public int compareTo(Object o) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    
    
    
    
    
    
    
    
    
    
    
     /**
     * Este metodos es usado cuando es posible que un campo este compuesto por
     * mas campos, por tanto se debe haber guardado como un tipo InternalFormat.
     * De ser este el caso, el objeto se casteara como tal previo a devolverlo.
     * @param id El identificador del objeto por obtener.
     * @return Un objeto del tipo InternalFormat en caso aplique, null en caso
     * no exista nungun objeto con este identificador.
     * @throws InvalidFieldTypeException Si el objeto existe y este no puede ser
     * resuelto como el tipo InternalFormat.
     */
    public InternalFormat getFieldAsInternalFormat(String id) {
        InternalField field = fields.get(id);
        if(field == null){
            return null;
        }
        if (field instanceof InternalFormat) {
            return (InternalFormat) field;
        } else {
            throw new InvalidFieldTypeException("El objeto id=" + id + " no puede ser "
                    + "representado como un InternalFormat:" + field);
        }
    }
    
    
       /**
     * Devuelve el valor almacenado por un objeto InternalField y cuyo identificador
     * es el "id" pasado como parametro.
     * @param id El identificador del objeto a consultar.
     * @return El valor almacenado por el objeto cuyo identificador es el parametro
     * recibido. Null en caso no exista ningun objeto con esta clave.
     * @throws InvalidFieldTypeException Si el objeto es el del tipo InternalFormat
     * y por tanto no debera tener un valor valido almacenado.
     */
    public String getInternalFieldValue(String id) {
        InternalField field = fields.get(id);
        if (field instanceof InternalFormat) {
            throw new InvalidFieldTypeException("No se puede obtener un valor en"
                    + " un objeto del tipo InternalFormat ");
        } else if (field == null) {
            return null;
        } else {
            return field.getValue();
        }
    }
}
