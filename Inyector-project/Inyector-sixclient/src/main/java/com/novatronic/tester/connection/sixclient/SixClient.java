/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.tester.connection.sixclient;

import com.novatronic.tester.connection.sixclient.exception.ApiSixConfigurationException;
import com.novatronic.tester.connection.sixclient.exception.ApiSixDestroyException;
import com.novatronic.tester.connection.sixclient.exception.ApiSixTrxException;

/**
 *
 * @author ofernandez
 */
public interface SixClient {
    public abstract void init() throws ApiSixConfigurationException;
    public abstract String sendMessage(String message) throws ApiSixTrxException;
    public abstract void destroy() throws ApiSixDestroyException;
}
