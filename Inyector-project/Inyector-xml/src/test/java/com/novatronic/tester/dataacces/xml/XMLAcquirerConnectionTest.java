/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.tester.dataacces.xml;


import com.novatronic.tester.dataacces.AccesTransaction;
import com.novatronic.tester.dataacces.dao.AcquirerConnectionDao;
import com.novatronic.tester.dataacces.dao.DAOFactory;
import com.novatronic.tester.domain.AcquirerConnection;
import java.util.List;
import junit.framework.TestCase;

/**
 *
 * @author nteruya
 */
public class XMLAcquirerConnectionTest extends TestCase {

    private final static String CONNECTION_XML = "connections.xml";
    
    public XMLAcquirerConnectionTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }


    public void testFindAll(){
        System.out.println("Executing testFindAll...");

        XMLFactory xmlFactory = (XMLFactory) DAOFactory.instance(XMLFactory.class);
        AccesTransaction trans = xmlFactory.getAccesTransaction();
        AcquirerConnectionDao acquirerConnDao = xmlFactory.getAcquirerConnectionDao(trans);
        List<AcquirerConnection> lista =  acquirerConnDao.findAll();
        trans.close();
        assertNotNull(lista);
        for(AcquirerConnection acq :  lista){
            System.out.println("Connection type : acquirerBin:" +  acq.getAcquirerBin() + "|acquirerName:" + acq.getAcquirerName() + "|numberTransactions:" + acq.getNumberTransactions());
        }
        System.out.println("testFindAll done");
       
    }

    public void testGetAcquirerConnectionById(){
        System.out.println("Executing testGetAcquirerConnectionById...");
        XMLFactory xmlFactory = (XMLFactory) DAOFactory.instance(XMLFactory.class);
        AccesTransaction trans = xmlFactory.getAccesTransaction();
        AcquirerConnectionDao acquirerConnDao = xmlFactory.getAcquirerConnectionDao(trans);
        AcquirerConnection acquirerConnection = acquirerConnDao.findById(Long.parseLong("500010"), true);
        trans.close();
        String result = acquirerConnection.getConnectionType();
        String expResult = "SIX";
        assertEquals(result, expResult);
        System.out.println("testGetAcquirerConnectionById done Id=500010" );
    }

   
    

}
