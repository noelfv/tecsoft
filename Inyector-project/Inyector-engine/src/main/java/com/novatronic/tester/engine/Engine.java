/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.tester.engine;

import com.novatronic.tester.connection.Connection;
import com.novatronic.tester.connection.ConnectionFactory;
import com.novatronic.tester.dataacces.AccesTransaction;
import com.novatronic.tester.dataacces.dao.AcquirerConnectionDao;
import com.novatronic.tester.dataacces.dao.DAOFactory;
import com.novatronic.tester.dataacces.dao.TransactionDao;
import com.novatronic.tester.domain.AcquirerConnection;
import com.novatronic.tester.domain.Transaction;
import com.novatronic.tester.exception.TesterConnectionException;
import com.novatronic.tester.exception.TesterException;
import com.novatronic.tester.report.Report;
import com.novatronic.tester.validate.Validator;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * @author ofernandez
 */
public class Engine implements TestListener {

    private static final Logger log = Logger.getLogger(Engine.class);
    private static final String CONFIG = "tester.properties";
    private static Properties props;
    /*TODO:Deberia ser privado pero el TestWorker lo necesita para acceder
     via el DaoF0actory (a este se le indica la clase)*/
    public static Class DATA_ACCES;
    private static Class VALIDATOR;
    private static Class REPORT;
    private final Map<String,ConnectionFactory> connectionFactories;
    private int testWorkersFinalized;
    private int testWorkersStepFinalized;
    private List<TestWorker> testWorkers;
    private List<AcquirerConnection> acquirerConnectionList = new ArrayList<AcquirerConnection>();
    private List<TestResult> testResultList = new ArrayList<TestResult>();
    private final List<EngineListener> engineStoppedListenerList = new ArrayList<EngineListener>();
    private static boolean initialized = false;
    private EngineMode executionMode = EngineMode.ALL_TEST;
    private final ApplicationContext context;
    
    private final DAOFactory daoFactory;

    public interface Key{
        public static final String DATA_NAME = "DataName";
        public static final String SIXCLIENT_NAME = "SixClientName";
        public static final String TCPCLIENT_NAME = "TcpClientName";
        public static final String TCPSERVER_NAME = "TcpServerName";
        public static final String EQUALSVALIDATOR_NAME = "EqualValidator";
        public static final String REPORT_NAME = "ReportName";
    }
    
    public Engine(){
        ConnectionFactory factory;
        
        log.debug("Creando Engine...");
        context = new ClassPathXmlApplicationContext("applicationContext.xml");
        connectionFactories = new HashMap<String,ConnectionFactory>();
        
        /*Preparando factorias de tipos de conexion*/
        factory = (ConnectionFactory)context.getBean("SixClientFactory");
        connectionFactories.put(Connection.SIXCLIENT_KEY, factory);
        factory = (ConnectionFactory)context.getBean("TcpClientFactory");
        connectionFactories.put(Connection.TCPCLIENT_KEY, factory);
        factory = (ConnectionFactory)context.getBean("TcpServerFactory");
        connectionFactories.put(Connection.TCPSERVIDOR_KEY, factory);
        
        daoFactory = (DAOFactory)context.getBean("DataFactory");

        initialized = true;
    }

    static {
        props = new Properties();
        FileInputStream fis;
        try {
            fis = new FileInputStream(CONFIG);
            props.load(fis);
            DATA_ACCES = Class.forName(props.getProperty(Key.DATA_NAME));
            VALIDATOR = Class.forName(props.getProperty(Key.EQUALSVALIDATOR_NAME));
            REPORT = Class.forName(props.getProperty(Key.REPORT_NAME));

            initialized = true;
            
        } catch (FileNotFoundException ex) {
            log.error("No se puedieron cargar las propiedades de configuracion:"
                    + CONFIG, ex);
            initialized = false;
        } catch (IOException ex) {
            log.error("No se puedo leer el archivo de configuracion", ex);
            initialized = false;
        } catch (ClassNotFoundException ex) {
            log.error("No se puedieron obtener las clases", ex);
            initialized = false;
        } catch (Exception ex) {
            log.error("Error desconocido", ex);
            initialized = false;
        }
    }

    public static String getPropertie(String key){
        return Engine.props.getProperty(key);
    }

    public void pause() {
        for (TestWorker testWorker : testWorkers) {
            testWorker.pauseJob();
        }
    }

    /**
     * @throws TesterConnectionException
     */
    public void stop() {
        for (TestWorker testWorker : testWorkers) {
            testWorker.stopJob();
        }
    }

    public void resume() {
        for (TestWorker testWorker : testWorkers) {
            testWorker.continueJob();
        }
    }

    /**
     * //TODO manejar excepciones
     * @param TestResultList
     * @exception IllegalArgumentException
     */
    public void start(List<TestResult> TestResultList, TestListener testListener,
            boolean stepbyStep) {
        if (stepbyStep) {
            executionMode = EngineMode.STEP_BY_STEP;
        } else {
            executionMode = EngineMode.ALL_TEST;
        }
        log.debug("Iniciando ejecucion con modo:" + this.executionMode);

        if (!initialized) {
            throw new IllegalStateException("El motor no ha sido incializado correctamente");
        }
        /*Creamos los tipos de Conexion*/
        prepareTestWorkers(TestResultList);
        for (int testNum = 0; testNum < TestResultList.size(); testNum++) {
            TestResult testResult = TestResultList.get(testNum);
            Connection connection = null;
            String key = createKeyConnection(testResult);
            connection = buildConnection(key,testResult.getAcquirerConnection());
            TestWorker testWorker = new TestWorker(testResult, connection, testNum,
                    this.getValidator(), testNum + "-" + testResult.getAcquirerName(),
                    stepbyStep);
            testWorker.addTestListener(testListener);
            testWorker.addTestListener(this);
            this.testWorkers.add(testWorker);
        }

        startTestWorkers();
    }

    private void startTestWorkers(){
        log.debug("Iniciando hilos para los test...");
        for (int i = 0; i < this.testWorkers.size(); i++) {
            log.debug("Iniciando hilo:" + this.testWorkers.get(i).getName());
            this.testWorkers.get(i).start();
        }
    }

    private String createKeyConnection(TestResult testResult){
        String key = testResult.getAcquirerConnection().getConnectionType()
                    + testResult.getAcquirerConnection().getConnectionMode();
        return key;
    }

    private Connection buildConnection(String key,
                                        AcquirerConnection acquirerConnection){
        ConnectionFactory factory = connectionFactories.get(key);
        if(factory != null){
            return factory.getConnection(acquirerConnection);
        }else{
            throw new IllegalArgumentException("Tipo de conexion desconocida:"
                                                + key);
        }
    }

    private void prepareTestWorkers(List<TestResult> TestResultList){
        /* Creamos los hilos*/
        testWorkers = new ArrayList<TestWorker>();
        testWorkersFinalized = TestResultList.size();
        testWorkersStepFinalized = TestResultList.size();
    }

    public EngineMode getExecutionMode() {
        return this.executionMode;
    }

    public String generateReport(){
        log.debug("Generando reporte:" + testResultList);
        Report report = getReport();
        report.setTestResults(testResultList);
        return report.generateReport();
    }

    /**
     *
     * @return
     * @throws TesterException
     */
    private Report getReport() {
        Report report = null;
        try {
            report = (Report) Engine.REPORT.newInstance();
            return report;
        } catch (Exception ex) {
            throw new TesterException("No es posible instanciar el reporte", ex);
        }
    }

    /**
     *
     * @return
     * @throws TesterException
     */
    private Validator getValidator() {
        Validator validator = null;
        try {
            validator = (Validator) Engine.VALIDATOR.newInstance();
            return validator;
        } catch (InstantiationException ex) {
            throw new TesterException("No es posible instanciar el validador", ex);
        } catch (IllegalAccessException ex) {
            throw new TesterException("No es posible instanciar el validador", ex);
        }
    }

    /**
     *
     * @return
     * @throws TesterException
     */
    public List<AcquirerConnection> retrieveAcquirerConnectionList() {
        try{
//            DAOFactory daoFactory = DAOFactory.instance(DATA_ACCES);
            AccesTransaction trans = daoFactory.getAccesTransaction();
            AcquirerConnectionDao connDao = daoFactory.getAcquirerConnectionDao(trans);
            trans.beginTransaction();
            acquirerConnectionList = connDao.findAll();
            trans.endTransaction();
            return acquirerConnectionList;
        } catch(Exception ex){
            throw new TesterException("No fue posible obtener la lista de "
                    + "adquirientes", ex);
        }

    }

    /**
     *
     * @param acquirerConnection
     * @return
     * @throws TesterException
     */
    public List<Transaction> retrieveIDTransactionList(AcquirerConnection acquirerConnection) {
        try{
//            DAOFactory daoFactory = DAOFactory.instance(DATA_ACCES);
            AccesTransaction trans = daoFactory.getAccesTransaction();
            TransactionDao connDao = daoFactory.getTransactionDao(trans);
            trans.beginTransaction();
            List result = connDao.findAll();
            trans.endTransaction();
            return result;
        } catch(Exception ex){
            throw new TesterException("No fue posible obtener adquirientes", ex);
        }
    }

    public List<TestResult> getTestResultList() {
        return testResultList;
    }

    public void setTestResultList(List<TestResult> testResultList) {
        this.testResultList = testResultList;
    }

    public List<AcquirerConnection> getAcquirerConnectionList() {
        return acquirerConnectionList;
    }

    public void setAcquirerConnectionList(List<AcquirerConnection> acquirerConnectionList) {
        this.acquirerConnectionList = acquirerConnectionList;
    }

    public void addEngineListener(EngineListener engineStoppedListener) {
        engineStoppedListenerList.add(engineStoppedListener);
    }

    private void bindEngineStopped() {
        for (int i = 0; i < engineStoppedListenerList.size(); i++) {
            EngineListener engineStoppedListener = engineStoppedListenerList.get(i);
            engineStoppedListener.engineStopped();
        }
    }

    private void bindEngineStepFinished() {
        for (int i = 0; i < engineStoppedListenerList.size(); i++) {
            EngineListener engineStoppedListener = engineStoppedListenerList.get(i);
            engineStoppedListener.engineStepFinished();
        }
    }

    @Override
    public void setInit(int row) {
    }

    @Override
    public void setFailled(Long value, int row) {
    }

    @Override
    public void setSuccesful(Long value, int row) {
    }

    @Override
    public void setState(TestState value, int row) {
        /*actualizamos corridas*/
        if (value == TestState.STEP_FINISHED) {
            testWorkersStepFinalized--;
            log.debug("Actualizando testWorkersStepFinalized="
                    + testWorkersStepFinalized + ",testWorkersFinalized="
                    + testWorkersFinalized);
        }
        /*actualizamos finalizados o parados*/
        if ((value == TestState.FINISHED) || (value == TestState.STOPPED)) {
            testWorkersFinalized--;         //Disminuye la cantidad de TestWorker vivos
            testWorkersStepFinalized--;     //Se disminuye pues ya no se ejecutara
            log.debug("Actualizando testWorkersStepFinalized="
                    + testWorkersStepFinalized + ",testWorkersFinalized="
                    + testWorkersFinalized);
        }
        /*Enviamos eventos*/
        /*Si terminaron todos los paso a paso->Evento fin de corrida paso a paso*/
        if (testWorkersStepFinalized == 0) {
            log.debug("Bind Engine - STEP_FINISHED, testWorkersStepFinalized:"
                    + testWorkersStepFinalized);
            this.bindEngineStepFinished();
            testWorkersStepFinalized = testWorkersFinalized;

        }
        /*Si todos estan detenidos-> evento ejecucion detenida*/
        if (testWorkersFinalized == 0) {
            log.debug("Bind Engine - STEP_FINISHED, testWorkersStepFinalized:"
                    + testWorkersStepFinalized);
            this.bindEngineStopped();
        }
    }
}
