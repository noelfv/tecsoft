/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.tester.dataacces.db;


import java.io.File;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 *
 * @author ofernandez
 */
public class HibernateUtil {
    private static final String FILE_CONFIG = "hibernate.cfg.xml";
    private static final Logger log = Logger.getLogger(HibernateUtil.class);
    private static final SessionFactory sessionFactory = buildSessionFactory();
    
    private static SessionFactory buildSessionFactory() {
        try {
            /* Crea la configuracion via xml local*/
            File file = new File(FILE_CONFIG);
            return new Configuration().configure(file).buildSessionFactory();

        } catch (Exception ex) {
            log.error("No fue posible iniciar el SessionFactory", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Devuelve el {@link #sessionFactory sessionFactory} a partir poder generar
     * session de Hibernate.
     * @return El objeto sessionFactory
     */
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
