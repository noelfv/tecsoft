/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.tester.connection.sixclient;

import com.novatronic.components.sixclient.tcp.ClienteSIXConnectionFactory;
import com.novatronic.components.sixclient.tcp.ClienteSIXParams;
import com.novatronic.pool.util.PoolConnectionParams;
import com.novatronic.tester.connection.Connection;
import com.novatronic.tester.connection.ConnectionFactory;
import com.novatronic.tester.connection.sixclient.exception.SixClientTesterConnectionException;
import com.novatronic.tester.domain.AcquirerConnection;
import com.novatronic.tester.util.ResourceHelper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 *
 * @author ofernandez
 */
public class SixClientConnectionFactory extends ConnectionFactory {

    public static final Logger log = Logger.getLogger(SixClientConnectionFactory.class);
    private static final String POOL_PROP_PATH = "poolSix.properties";
    private static final String EMPTY_STR = "";
    private static final String HOST_NAME = "hostname";
    private static final String USER_NAME = "username";
    private static final String USER_PASSWORD = "password";
    private static final String HOST_DEST = "hostdest";
    private static final String PORT_DEST = "portdest";
    private static final String PROC_DEST = "procdest";
    private static final String SIXDRV_TYPE = "typesixdrv";
    public static final int IDX_POOL = 0;
    public static final int IDX_SIX = 1;

    /**
     * {@inheritDoc}
     *
     * @throws SixClientTesterConnectionException
     */
    public Connection getConnection(AcquirerConnection acquirerConnection) {
        ClienteSIXConnectionFactory factory;

        try {
            /* 3. Cargando los datos del SixFactory y lo agregamos a la conexion*/
            Properties[] props = buildUnifiedConfiguration(acquirerConnection);
            factory = ClienteSIXConnectionFactory.createConnectionFactory(
                    props[IDX_SIX], props[IDX_POOL]);
            SixClientConnection sixClientConnection = new SixClientConnection();
            sixClientConnection.setSixFactory(factory);

            return sixClientConnection;

        } catch (Exception ex) {
            throw new SixClientTesterConnectionException("Error al leer la configuracion", ex);
        }
    }

    private Properties[] buildUnifiedConfiguration(AcquirerConnection acquirerConnection)
            throws IOException {
        InputStream is;
        Properties generalProps;
        Properties[] props = new Properties[2];

        is = ResourceHelper.find(POOL_PROP_PATH);
        generalProps = new Properties();
        generalProps.load(is);
        props[IDX_POOL] = new Properties();
        props[IDX_SIX] = getParamsConnection(acquirerConnection);

        for (PoolConnectionParams pPool : PoolConnectionParams.values()) {
            if (generalProps.getProperty(pPool.getName()) != null) {
                props[IDX_POOL].put(pPool.getName(), generalProps.getProperty(pPool.getName()));
            }
        }
        if (props[IDX_POOL].isEmpty()) {
            props[IDX_POOL] = null;
        }

        for (ClienteSIXParams pConf : ClienteSIXParams.values()) {
            if (generalProps.getProperty(pConf.getName()) != null) {
                props[IDX_SIX].put(pConf.getName(), generalProps.getProperty(pConf.getName()));
            }
        }
        return props;
    }

    private Properties getParamsConnection(AcquirerConnection acquirerConnection) {
        String host;
        String port;
        String serverAppl;
        String userName;
        String userPassword;
        String ip;
        String sixDriverType;
        Properties sixProps = new Properties();

        /* 1.1 Obtenemos los datos*/
        host = acquirerConnection.getHost();
        port = acquirerConnection.getPort();
        serverAppl = acquirerConnection.getServerAppl();
        userName = acquirerConnection.getUserName();
        userPassword = acquirerConnection.getUserPassword();
        ip = acquirerConnection.getIp();
        sixDriverType = acquirerConnection.getSixDriverType();

        /* 1.2. Validamos los datos recibidos*/
        if ((host.equals(EMPTY_STR) || host == null)
                || (port.equals(EMPTY_STR) || host == null)
                || (serverAppl.equals(EMPTY_STR) || serverAppl == null)
                || (userName.equals(EMPTY_STR) || userName == null)
                || (userPassword.equals(EMPTY_STR) || userPassword == null)) {

            throw new IllegalArgumentException("Parametros incompletos");
        }

        /* 1.3. Cargamos la propiedades*/
        sixProps.setProperty(HOST_NAME, host);
        sixProps.setProperty(PORT_DEST, port);
        sixProps.setProperty(HOST_DEST, ip);
        sixProps.setProperty(PROC_DEST, serverAppl);
        sixProps.setProperty(USER_NAME, userName);
        sixProps.setProperty(USER_PASSWORD, userPassword);
        sixProps.setProperty(SIXDRV_TYPE, sixDriverType);

        return sixProps;
    }
}
