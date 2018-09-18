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

package com.sun.enterprise.tools.verifier.tests.ejb.entity.ejbql;

import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.StringManagerHelper;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.jdo.spi.persistence.support.ejb.ejbqlc.EJBQLC;
import com.sun.jdo.spi.persistence.support.ejb.ejbqlc.EJBQLException;
import org.glassfish.ejb.deployment.descriptor.EjbCMPEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.PersistenceDescriptor;
import org.glassfish.ejb.deployment.descriptor.QueryDescriptor;

import java.lang.reflect.Method;
import java.util.Iterator;


/**
 * This class contains tests for EJB QLs that are shared 
 * by tests for entity beans and for depenent objects.
 *
 * @author	Qingqing Ouyang
 * @version
 */
public class EjbQLChecker {
    
    /**
     * <p>
     * helper property to get to the localized strings
     * </p>
     */
    protected static final LocalStringManagerImpl smh = 
        StringManagerHelper.getLocalStringsManager();
    
    /**
     * Check the syntax and semantics of the targetted
     * queries.
     *
     * @param desc An PersistenceDescriptor object.
     * @param ejbqlDriver An EjbQlDriver created using the
     *        targetted ejb bundle.
     * @param result The test results.
     * @param ownerClassName Name of the class initiated the test.
     * @return whether any error has occurred.
     */
    public static boolean checkSyntax (EjbDescriptor ejbDesc,
            EJBQLC ejbqlDriver, Result result, String ownerClassName) {
        
        boolean hasError = false;
        String query = null;
        PersistenceDescriptor desc = ((EjbCMPEntityDescriptor)ejbDesc).getPersistenceDescriptor();
        
        for (Iterator it = desc.getQueriedMethods().iterator(); it.hasNext();) {
            MethodDescriptor method = (MethodDescriptor) it.next();
            try {
                QueryDescriptor qDesc = desc.getQueryFor(method);
                query = qDesc.getQuery();
                
                if (qDesc.getIsEjbQl()) {
                    Method m = method.getMethod(ejbDesc);

                    int retypeMapping = mapRetType(qDesc.getReturnTypeMapping());
        
                    boolean finder = false;

                    if ((method.getName()).startsWith("find")) {
                       finder = true;
                       retypeMapping = 2; /*QueryDescriptor.NO_RETURN_TYPE_MAPPING;*/
                    }

                    ejbqlDriver.compile(query, m, retypeMapping, finder, ejbDesc.getName());
                }
            } catch (EJBQLException ex) {
                ex.printStackTrace();
                if (!hasError) {
                    hasError = true;
                }
	
                result.addErrorDetails
                    (smh.getLocalString(ownerClassName + ".parseError",
                            "Error: [ {0} ] has parsing error(s)",
                            new Object[] {query}));
		result.addErrorDetails
		    (smh.getLocalString(ownerClassName + ".SAXParseException",
                            "Exception occured : [{0}]",
                            new Object[] {ex.toString()}));
            }

        }
	if (hasError == false) {
	    result.addGoodDetails
		    (smh.getLocalString(ownerClassName + ".passed",
                            " Syntax and Semantics of the Queries are correct",
			    new Object[] {}));
	}
        return hasError;
    }

 private static int mapRetType(int rettype) {

    switch(rettype) {

    case 0 : return 2;
    case 1 : return 0;
    case 2 : return 1;
    default: return 2;
         
    }

 }

}
