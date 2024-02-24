/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.filter;

import com.novatronic.formatter.internal.InternalFormat;

/**
 * Clase que almacena el contenedor de filtros y aplica los filtros para los
 * formatos internos anonimos, los que no tienen un identificiador de
 * formateador definido, y formatos internos asociados a un formateador.
 *
 * @author rcastillejo
 * @since 17/12/13
 * @version 01.00.00
 */
public class SingletonFilters {

    private static Filters filters;

    /**
     * Asigna el contenedor de filtros configurados.
     *
     * @param filters Contenedor de filtros
     */
    public static void configureFilters(Filters filters) {
        SingletonFilters.filters = filters;
    }

    /**
     * Devuelve la cadena que representa el formato interno filtrado, este
     * formato se considera anonimo debido a que no se le asocia un
     * identificador de formato. Por ello, este formato es analizado por cada
     * uno de sus campos y subcampos para obtener el filtro del repositorio de
     * filtros referenciados. Solo se aplica a los campos de un nivel; es decir,
     * no aplica para campos agrupadores. La asociacion del campo con un filtro
     * esta definido por un path.
     *
     * @param intFmt Formato interno a filtrar
     * @return La cadena que representa el formato interno filtrado
     */
    public static String filter(InternalFormat intFmt) {
        return filters.filter(intFmt);
    }

    /**
     * Devuelve la cadena del intFmtToFilter filtrado. El cual se especifica el
     * identificador del formateador para obtener del repositorio de filtros a
     * aplicar. Solo se aplica a los campos de un nivel; es decir, no aplica
     * para campos agrupadores. La asociacion del campo con un filtro esta
     * definido por un path.
     *
     * @param formatId Identificador del Formato Interno asociado al filtrado
     * @param intFmt Formato interno a filtrar
     * @return La cadena que representa el formato interno filtrado
     */
    public static String filter(String formatId, InternalFormat intFmt) {
        return filters.filter(formatId, intFmt);
    }
}
