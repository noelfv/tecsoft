/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui;

import com.novatronic.formatter.gui.util.FindResource;
import java.io.InputStream;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 *
 * @author Omar
 */
public class Config {
    private static final Logger log = Logger.getLogger(Config.class);
    private static final String CONFIG_FILE_NAME = "tester.properties";
    private static final Properties configuration;
    
    static{
        configuration = new Properties();
        loadConfiguration();
    }
    
    private static void loadConfiguration(){
        InputStream is;
        try {
            is = FindResource.get(CONFIG_FILE_NAME).openStream();
            configuration.load(is);
            log.info("Configuracion leida");
        } catch (Exception ex) {
            log.warn("Imposible leer el archivo=" + CONFIG_FILE_NAME
                    + ". Se utiliza valores por defecto", ex);
        }
    }
    
    public static final String PERF_DIREC_IF_TO_FRAME = "FI a Trama";
    public static final String PERF_DIREC_FRAME_TO_IF = "Trama a FI";
    public static final String TESTS_DIR = configuration.getProperty("test.dir");
    public static final String FORMATS_DIR = configuration.getProperty("formats.dir");
    public static final String SM_TEST_PANEL = configuration.getProperty("sm.test.panel");
    public static final String DESING_CONFIG = configuration.getProperty("config.icons");
    public static final String ACTIONS_CONFIG = configuration.getProperty("config.actions");
    public static final String TESTFILE_PREFIX = configuration.getProperty("testfile.prefix");
}
