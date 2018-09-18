/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.sun.enterprise.tools.verifier.tests.web;

import com.sun.enterprise.tools.verifier.Result;

/** 
 * Listener class must implement a valid interface.
 * Verify that the Listener class implements one of five valid interfaces.
 * 
 * @author Jerome Dochez
 * @version 1.0
 */
public class ListenerClassImplementsValidInterface extends ListenerClass implements WebCheck {
        
    /**
     * <p>
     * Run the verifier test against a declared individual listener class
     * </p>
     *
     * @param result is used to put the test results in
     * @param listenerClass is the individual listener class object to test
     * @return true if the test pass
     */
    protected boolean runIndividualListenerTest(Result result, Class listenerClass) {
        
        boolean validInterface = false;
        Class clazz = listenerClass;
        
        if (listenerClass==null) {
            return false;
        }
        
	validInterface = isImplementorOf(clazz,"javax.servlet.ServletContextAttributeListener");
	if (validInterface != true) {
	    validInterface = isImplementorOf(clazz,"javax.servlet.ServletContextListener");
	}
	if (validInterface != true) {
	    validInterface = isImplementorOf(clazz,"javax.servlet.http.HttpSessionAttributeListener");
	}
	if (validInterface != true) {
	    validInterface = isImplementorOf(clazz,"javax.servlet.http.HttpSessionListener");
	}
    if (validInterface != true) {
	    validInterface = isImplementorOf(clazz,"javax.servlet.ServletRequestAttributeListener");
	}if (validInterface != true) {
	    validInterface = isImplementorOf(clazz,"javax.servlet.ServletRequestListener");
	}if (validInterface != true) {
	    validInterface = isImplementorOf(clazz,"javax.servlet.http.HttpSessionBindingListener");
	}

         if (validInterface) {
             result.addGoodDetails(smh.getLocalString
                (getClass().getName() + ".passed",
                 "Listener class [ {0} ] implements a valid interface.",
                 new Object[] {listenerClass.getName()}));
         } else if (!validInterface){
             result.addErrorDetails(smh.getLocalString
                (getClass().getName() + ".failed",
                 "Error: Listener class [ {0} ] does not implement one or more of the following valid interface.\n javax.servlet.ServletContextAttributeListener, javax.servlet.ServletContextListener, javax.servlet.http.HttpSessionAttributeListener, javax.servlet.http.HttpSessionListener",
                  new Object[] {clazz.getName()}));
	 }
	    	                     
        return validInterface;
    }
}
