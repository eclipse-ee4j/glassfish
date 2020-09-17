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

/*
 * TransactionSupportExistence.java
 *
 * Created on September 28, 2000, 2:09 PM
 */

package com.sun.enterprise.tools.verifier.tests.connector;

import java.io.File;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.tools.verifier.tests.*;
import com.sun.enterprise.deployment.xml.ConnectorTagNames;

/**
 * Test the implementation of the proprer transaction support depending on the 
 * level of transaction declared in the deployment descriptor
 *
 * @author  Jerome Dochez
 * @version 
 */
public class TransactionSupportExistence
    extends ConnectorTest 
    implements ConnectorCheck 
{

    /** <p>
     * Test the implementation of the proprer transaction support depending on 
     * the level of transaction declared in the deployment descriptor :
     *  - NoTransaction    neither XAResource or LocalTransaction should be
     *                      implemented, warning if it does
     *  - LocalTransaction LocalTransaction has to be implemented
     *  - XATransaction    XAResource has to be implemented         
     * </p>
     *
     * @param descriptor deployment descriptor for the rar file
     * @return result object containing the result of the individual test
     * performed
     */
    public Result check(ConnectorDescriptor descriptor) {
                
        Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        if(!descriptor.getOutBoundDefined())
        {
          result.addNaDetails(smh.getLocalString
              ("tests.componentNameConstructor",
               "For [ {0} ]",
               new Object[] {compName.toString()}));
          result.notApplicable(smh.getLocalString
              ("com.sun.enterprise.tools.verifier.tests.connector.managed.notApplicableForInboundRA",
               "Resource Adapter does not provide outbound communication"));
          return result;
        }
        String connectorTransactionSupport =
        descriptor.getOutboundResourceAdapter().getTransSupport();
        
        // No transaction support specified, this is an error
        if (connectorTransactionSupport==null) {
	    result.addErrorDetails(smh.getLocalString
				   ("tests.componentNameConstructor",
				    "For [ {0} ]",
				    new Object[] {compName.toString()}));
	    result.failed(smh.getLocalString
			  ("com.sun.enterprise.tools.verifier.tests.connector.TransactionSupport.nonexist",
			   "Error: No Transaction support specified for resource adapter",
			   new Object[] {connectorTransactionSupport}));        
            return result;
        }        
        
        // get the rar file handle
       // File jarFile = Verifier.getJarFile(descriptor.getModuleDescriptor().getArchiveUri());
        
//        File f=Verifier.getArchiveFile(descriptor.getModuleDescriptor().getArchiveUri());
        if (connectorTransactionSupport.equals(ConnectorTagNames.DD_NO_TRANSACTION)) {
            boolean oneFailed=false;
            if (findImplementorOf(descriptor, "jakarta.resource.spi.LocalTransaction")!=null) {
                oneFailed = true;
		result.addWarningDetails(smh.getLocalString
					  ("tests.componentNameConstructor",
					   "For [ {0} ]",
					   new Object[] {compName.toString()}));
                result.warning(smh.getLocalString(getClass().getName() + ".warning",
                "Warning: Transaction support {0} is specified for resource adapter but [ {1} ] is implemented",
		new Object[] {"NoTransaction", "jakarta.resource.spi.LocalTransaction"}));     
            }
            if (findImplementorOf(descriptor, "javax.transaction.xa.XAResource")!=null) {
                oneFailed = true;
		result.addWarningDetails(smh.getLocalString
					  ("tests.componentNameConstructor",
					   "For [ {0} ]",
					   new Object[] {compName.toString()}));
                result.warning(smh.getLocalString(getClass().getName() + ".warning",
                "Warning: Transaction support {0} is specified for resource adapter but [ {1} ] is implemented",
		new Object[] {"NoTransaction", "javax.transaction.xa.XAResource"}));     
            }
            if (!oneFailed) {
                result.addGoodDetails(smh.getLocalString
					  ("tests.componentNameConstructor",
					   "For [ {0} ]",
					   new Object[] {compName.toString()}));	
		result.passed(smh.getLocalString(getClass().getName() + ".passed1",
                    "Transaction support NoTransaction is specified for resource adapter and [ {0} ] are not implemented",
                    new Object[] {"javax.transaction.xa.XAResource, jakarta.resource.spi.LocalTransaction"}));                          
            }
        }
        else {
            if (connectorTransactionSupport.equals(ConnectorTagNames.DD_LOCAL_TRANSACTION)) {
                if (findImplementorOf(descriptor, "jakarta.resource.spi.LocalTransaction")==null) {
                    result.addErrorDetails(smh.getLocalString
					  ("tests.componentNameConstructor",
					   "For [ {0} ]",
					   new Object[] {compName.toString()}));
		result.failed(smh.getLocalString(getClass().getName() + ".nonexist",
                    "Error: Transaction support {0} is specified for resource adapter but [ {1} ] is not implemented",
		    new Object[] {"LocalTransaction", "jakarta.resource.spi.LocalTransaction"}));     
                } else {                
                    if (findImplementorOf(descriptor, "javax.transaction.xa.XAResource")!=null) {
			result.addWarningDetails(smh.getLocalString
					  ("tests.componentNameConstructor",
					   "For [ {0} ]",
					   new Object[] {compName.toString()}));
                        result.addWarningDetails(smh.getLocalString(getClass().getName() + ".warning",
                        "Warning: Transaction support {0} is specified for resource adapter but [ {1} ] is implemented",
                		new Object[] {"LocalTransaction", "javax.transaction.xa.XAResource"}));     
                    } else {
                        result.addGoodDetails(smh.getLocalString
					  ("tests.componentNameConstructor",
					   "For [ {0} ]",
					   new Object[] {compName.toString()}));
			result.passed(smh.getLocalString(getClass().getName() + ".passed2",
                            "Transaction support {0} is specified for resource adapter and [ {1} ] is(are) implemented",
                		new Object[] {"LocalTransaction", "jakarta.resource.spi.LocalTransaction"}));                             
                    }
                }                            
            } else {
                if (connectorTransactionSupport.equals(ConnectorTagNames.DD_XA_TRANSACTION)) {
                    boolean oneFailed = false;
                    if (findImplementorOf(descriptor, "jakarta.resource.spi.LocalTransaction")==null) {
                        oneFailed = true;
                        result.addErrorDetails(smh.getLocalString
					  ("tests.componentNameConstructor",
					   "For [ {0} ]",
					   new Object[] {compName.toString()}));
			result.failed(smh.getLocalString(getClass().getName() + ".nonexist",
                        "Error: Transaction support {0} is specified for resource adapter but [ {1} ] is not implemented",
		        new Object[] {"XATransaction", "jakarta.resource.spi.LocalTransaction"}));                         
                    }
                    if (findImplementorOf(descriptor, "javax.transaction.xa.XAResource")==null) {
                        oneFailed = true;
                        result.addErrorDetails(smh.getLocalString
					  ("tests.componentNameConstructor",
					   "For [ {0} ]",
					   new Object[] {compName.toString()}));
			result.failed(smh.getLocalString(getClass().getName() + ".nonexist",
                        "Error: Transaction support {0} is specified for resource adapter but [ {1} ] is not implemented",
		        new Object[] {"XATransaction", "javax.transaction.xa.XAResource"}));                         
                    }
                    if (!oneFailed) {
                        result.addGoodDetails(smh.getLocalString
					  ("tests.componentNameConstructor",
					   "For [ {0} ]",
					   new Object[] {compName.toString()}));
			result.passed(smh.getLocalString(getClass().getName() + ".passed2",
                            "Transaction support {0} is specified for resource adapter and [ {1} ] is(are) implemented",
                            new Object[] {"XATransaction", "javax.transaction.xa.Transaction, jakarta.resource.spi.LocalTransaction"}));                               
                    }
                } else {
                    // unknow transaction support
	            result.addErrorDetails(smh.getLocalString
					  ("tests.componentNameConstructor",
					   "For [ {0} ]",
					   new Object[] {compName.toString()}));
		    result.failed(smh.getLocalString
	                ("com.sun.enterprise.tools.verifier.tests.connector.TransactionSupport.failed",
                        "Error: Deployment descriptor transaction-support [ {0} ] for resource adapter is not valid",
		        new Object[] {connectorTransactionSupport}));                        
                }
            }
        } 
        return result;
    }
}
