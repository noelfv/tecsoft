/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.tester.engine;

import com.novatronic.tester.connection.Connection;
import com.novatronic.tester.dataacces.AccesTransaction;
import com.novatronic.tester.dataacces.dao.DAOFactory;
import com.novatronic.tester.dataacces.dao.TransactionDao;
import com.novatronic.tester.domain.Transaction;
import com.novatronic.tester.exception.TesterConnectionException;
import com.novatronic.tester.exception.TesterTimeOutConnectionException;
import com.novatronic.tester.exception.TesterValidationException;
import com.novatronic.tester.validate.Validator;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author ofernandez
 */
public class TestWorker extends Thread {

    private static final Logger log = Logger.getLogger(TestWorker.class);
    private final TestResult testResult;
    private final Connection connection;
    private List<Transaction> transactionList;
    private boolean stop = false;
    private boolean pause = false;
    private boolean stepByStep = false;
    private final Validator validator;
    private final List<TestListener> testListenerList;
    private final int position;
    private final Long sleep;
    private final Long idAcquirerConnection;

    public TestWorker(TestResult testResult, Connection connection,
            int position, Validator validator, String name, boolean stepByStep) {
        Long tempLong;
        this.testResult = testResult;
        this.connection = connection;
        this.testListenerList = new ArrayList<TestListener>();
        this.position = position;
        this.validator = validator;
        this.validator.setTestResult(testResult);
        this.setName(name);
        this.stepByStep = stepByStep;
        this.idAcquirerConnection = testResult.getAcquirerConnection().getId();

        tempLong = this.testResult.getAcquirerConnection().getSleep();
        this.sleep = (tempLong == null) ? 0 : tempLong * 1000;
    }

    public TestResult getTestResult(){
        return this.testResult;
    }

    public void addTestListener(TestListener testListener) {
        log.debug("Agregando listener:" + testListener);
        testListenerList.add(testListener);
    }

    private void bindTestInit(){
        testResult.clearTest();
        for (int i = 0; i < testListenerList.size(); i++) {
            log.debug("[" + testListenerList.get(i) + "]bindTestInit");
            testListenerList.get(i).setInit(position);
        }
    }

    private void bindTestFailled() {
        testResult.setTestFailled(testResult.getTestFailled() + 1);
        for (int i = 0; i < testListenerList.size(); i++) {
            log.debug("[" + testListenerList.get(i) + "]bindTestFailled");
            testListenerList.get(i).setFailled(testResult.getTestFailled(), position);
        }
    }

    private void bindTestSuccesful() {
        testResult.setTestSuccesful(testResult.getTestSuccesful() + 1);
        for (int i = 0; i < testListenerList.size(); i++) {
            log.debug("[" + testListenerList.get(i) + "]bindTestSuccesful");
            testListenerList.get(i).setSuccesful(testResult.getTestSuccesful(), position);
        }
    }

    private void bindSetState(TestState value) {
        testResult.setState(value);
        for (int i = 0; i < testListenerList.size(); i++) {
            log.debug("[" + testListenerList.get(i) + "]Bind state:"+value);
            testListenerList.get(i).setState(value, position);
        }
    }

    /**
     */
    @Override
    public void run() {
        /* Colocando el estado de iniciado, e iniciando las pruebas*/
        this.bindTestInit();
        this.bindSetState(TestState.RUNNING);

        try {
            /* Obteniendo todas las tramas a probar*/
            retrieveTransactionsToTest();

            /*Validamos si tenemos transacciones por procesar*/
            log.debug("Iniciando pruebas con "+this.transactionList.size()+" transacciones");
            if (transactionList.isEmpty()) {
                log.debug("0 transacciones por procesar. Se finaliza la prueba");
                this.bindSetState(TestState.FINISHED);
                return;
            }

            /*Ejecutando y comparando*/
            int txNumber = 0;
            do {
                this.bindSetState(TestState.RUNNING);
                log.debug("obtenemos la transaccion (" + txNumber + ")");
                Transaction transaction = this.transactionList.get(txNumber);
                
                /*Realizamos ejecucion*/
                log.debug("Enviamos la trama [" + transaction.getAcquirerIn() + "]");
                String tramaInput = this.connection.execute(transaction.getAcquirerIn());
                log.debug("Recibimos la trama [" + tramaInput + "]");
                if (this.validator.validate(transaction, tramaInput)) {
                    this.bindTestSuccesful();
                } else {
                    this.bindTestFailled();
                }
                this.bindSetState(TestState.STEP_FINISHED);
                txNumber++;
            } while (!this.shouldStopExecution(txNumber));

            /*avisando se termino las pruebas*/
            log.debug("Terminando las pruebas");
            this.bindSetState(TestState.FINISHED);

        } catch (TesterTimeOutConnectionException ex) {
            log.debug("Timeout de conexion:" + this.getName());
            this.bindSetState(TestState.STOPPED);
        } catch (InterruptedException ex) {
            log.error("Se interrrumpio al TestWorker:" + this.getName(), ex);
            this.bindSetState(TestState.STOPPED);
        } catch (TesterConnectionException ex) {
            log.error("Se forzo una desconexión:", ex);
            this.bindSetState(TestState.STOPPED);
        } catch (TesterValidationException ex) {
            log.error("No fue posible realizar la validacion", ex);
            this.bindSetState(TestState.STOPPED);
        } catch (Exception ex) {
            log.error("Error desconocido", ex);
            this.bindSetState(TestState.STOPPED);
        } finally {
            if (!this.stop) {
                log.debug("Realizando desconeccion");
                this.connection.disconnnect();
            }
        }
    }

    /**
     * Obtiene de la conexion a datos la cantidad de transacciones a testear.
     * Estas seran colocadas en lista con el fin de poder ser obtenidas una a
     * una en cada prueba.
     */
    private void retrieveTransactionsToTest(){
        /* Obteniendo todas las tramas a probar*/
        log.debug("Consultando en la BD por el Adquiriente de id="
                +this.idAcquirerConnection);
        DAOFactory daoFactory = DAOFactory.instance(Engine.DATA_ACCES);
        AccesTransaction trans = daoFactory.getAccesTransaction();
        TransactionDao connDao = daoFactory.getTransactionDao(trans);
        trans.beginTransaction();
        transactionList = connDao.getByAcquirer(this.idAcquirerConnection);
        trans.endTransaction();
    }

    /**
     * Este metodo revisa la operacion actual del TestWorker. En caso sea paso a
     * paso o lo hayan pausado, el TestWorker caera en wait a la espera que
     * lo notifiquen para continuar o terminar. Para considerar el fin de la
     * prueba se compara la cantidad transacciones en lista con el numero de
     * transaccion procesada (txProcessed). Si esta resulta igual, se detiene
     * los test (return = true).
     * @param txProcessed Es el numero de transaccion testeada.
     * @return true si se desea detener el TestWorker, false en cualquier otro
     * caso.
     * @throws InterruptedException
     */
    private boolean shouldStopExecution(int txProcessed) throws InterruptedException {
        log.debug("Revisando operacion para el hilo:" + this.getName()
                + ",txProcessed:" +txProcessed+ ",stop:"+stop + ",pause:"+pause
                + ",stebByStep:"+stepByStep);
        if (stop || (txProcessed == transactionList.size())) {
            return true;
        } else if (pause || stepByStep) {
            synchronized (this) {
                try {
                    log.debug("El TestWorker espera notify para continuar:pause["
                            + pause +"], stepByStep[" + stepByStep + "]");
                    wait();
                } catch (InterruptedException ex) {
                    log.error("El TestWorker ha sido interrumpido", ex);
                }
            }
            return false;
        } else if (!stepByStep) {
            Thread.sleep(sleep);
        }
        return false;
    }

    /**
     * Pausa la ejecución de las pruebas realizado por este hilo. En el caso que
     * alguna prueba se encuentre iniciada, la pause no se aplicar&aacute; a
     * esta prueba pero si a las subsiguientes.
     */
    public void pauseJob() {
        if (isAlive()) {
            if (!this.stop){
                this.pause = true;
                testResult.setState(TestState.PAUSED);
                this.bindSetState(TestState.PAUSED);
            }
        }
    }

    /**
     * Detiene las pruebas en curso. Si alguna prueba ya se inicio previamente,
     * esta podría detenerse abruptamente.
     * @throws TesterConnectionException
     */
    public void stopJob() {
        if (isAlive()) {
            this.stop = true;
            this.bindSetState(TestState.STOPPED);
            this.connection.disconnnect();
        }
    }

    /**
     * Este metodo contiinua la ejecuci&oacute;n de las pruebas en caso estas
     * hayan sido pausadas.
     */
    public void continueJob() {
        if (isAlive()) {
            this.pause = false;
            testResult.setState(TestState.RUNNING);
            this.bindSetState(TestState.RUNNING);
            synchronized (this) {
                notify();
            }
        }
    }
}
