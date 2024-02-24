/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.tester.reader;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import org.apache.log4j.Logger;

/**
 *
 * @author Omar
 */
public class DirReader {
    private static final Logger log = Logger.getLogger(DirReader.class);
    
    /**
     * Lee los archivos de un directorio indicado en el parametro path,
     * utilizando un filtro de prefijo para los archivos a leer.
     * @param path Ruta de busqueda absoluta o relativa
     * @param filter Prefijo que deberan tener los archivos
     * @return Un arreglo de cadenas con los archivos encontrados. Un arreglo
     * vacio en caso de no tener archivos que coincidan con el filtro dado. Null
     * en caso no exista el directorio
     * @throws ReaderException Si no es posible leer la ruta con el filtro
     * recibido
     */
    public static String[] readDirectory(String path, String filter) {
        String[] filesInDir;
        DirFilter dirFilter = new DirFilter(filter);
        File dir = new File(path);
        log.debug("path=" + path + ",filter=" + filter + ",dir=" + dir);
        
        try{
            filesInDir = dir.list(dirFilter);
            log.debug("filesInDir=" + Arrays.asList(filesInDir));
            return filesInDir;
        }
        catch(Exception ex){
            throw new ReaderException("No es posible leer la ruta=" + path
                    + ", con el filtro=" + filter, ex);
        }
    }
}

class DirFilter implements FilenameFilter{
    private String prefix;
    
    public DirFilter(String prefix){
        this.prefix = prefix;
    }
    @Override
    public boolean accept(File dir, String name) {
        return name.startsWith(prefix);
    }
    
}
