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

package com.sun.enterprise.tools.verifier.tests.ejb.runtime;

import com.sun.enterprise.deployment.EjbReferenceDescriptor;
import com.sun.enterprise.deployment.EjbSessionDescriptor;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbEntityDescriptor;

import java.util.Iterator;
import java.util.Set;

/** ejb [0,n]
 *    ejb-ref [0,n]
 *        ejb-ref-name [String]
 *        jndi-name [String]
 *
 * The ejb-ref is root element that binds and ejb reference to a jndi-name.
 * The ejb-ref-name should have an entry in the ejb-jar.xml
 * The jdi-name should not be empty. It shoudl start with ejb/
 * @author
 */

public class ASEjbRef extends EjbTest implements EjbCheck {
    
   /**
     * @param descriptor the Enterprise Java Bean deployment descriptor
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {
        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        String ejbName = null, jndiName=null;
        boolean oneFailed = false;
        boolean notApplicable = false;
        boolean oneWarning = false;

        try{
            ejbName = descriptor.getName();
            Set ejbRefs = descriptor.getEjbReferenceDescriptors();
            if (ejbRefs.size()>0){
                Iterator it = ejbRefs.iterator();
                while(it.hasNext()){
                    EjbReferenceDescriptor desc = ((EjbReferenceDescriptor)it.next());
                    String refJndiName=getXPathValue("/sun-ejb-jar/enterprise-beans/ejb/ejb-ref[ejb-ref-name=\""+desc.getName()+"\"]/jndi-name");
                    String refName = desc.getName();
                    String type = desc.getType();
                    if(!desc.isLocal()){
                        if (type == null || !( (type.equals(EjbSessionDescriptor.TYPE) || type.equals(EjbEntityDescriptor.TYPE))) ){
                            oneFailed = true;
                            addErrorDetails(result, compName);
                            result.failed(smh.getLocalString(getClass().getName() + ".failed1",
                                    "FAILED [AS-EJB ejb-ref] ejb-ref-name has an invalid type in ejb-jar.xml." +
                                    " Type should be Session or Entity only"));
                        }else{
                            addGoodDetails(result, compName);
                            result.passed(smh.getLocalString(getClass().getName() + ".passed2",
                                    "PASSED [AS-EJB ejb-ref] ejb-ref-name [{0}] is valid",
                                    new Object[]{refName}));
                        }
                    }else{
                        addNaDetails(result, compName);
                        result.notApplicable(smh.getLocalString
                                (getClass().getName() + ".notApplicable",
                                        "{0} Does not define any ejb references",
                                        new Object[] {ejbName}));
                        return result;
                    }
                    
                    if (refJndiName != null){
                        if(refJndiName.length()==0){
                            oneFailed = true;
                            addErrorDetails(result, compName);
                            result.addErrorDetails(smh.getLocalString
                                (getClass().getName() + ".failed2",
                                "FAILED [AS-EJB ejb-ref] : jndi-name cannot be an empty string",
                                new Object[] {refName}));
                        }else{
                            if (!refJndiName.startsWith("ejb/")){
                                oneWarning = true;
                                addWarningDetails(result, compName);
                                result.warning(smh.getLocalString
                                    (getClass().getName() + ".warning",
                                    "WARNING [AS-EJB ejb-ref] JNDI name should start with ejb/ for an ejb reference",
                                    new Object[] {refName}));
                            }
                        }
                    }else {
                        oneFailed = true;
                        addErrorDetails(result, compName);
                        result.addErrorDetails(smh.getLocalString
                            (getClass().getName() + ".failed2",
                            "FAILED [AS-EJB ejb-ref] : jndi-name cannot be an empty string",
                            new Object[] {refName}));
                    }
                    
                    if (!oneFailed){
                        addGoodDetails(result, compName);
                        result.addGoodDetails(smh.getLocalString(
                            getClass().getName() + ".passed1",
                            "PASSED [AS-EJB ejb-ref] : ejb-ref-Name is {0} and jndi-name is {1}",
                            new Object[] {refName,refJndiName}));
                    }
                }
                
            }else{
                addNaDetails(result, compName);
                result.notApplicable(smh.getLocalString
                    (getClass().getName() + ".notApplicable",
                    "{0} Does not define any ejb references",
                    new Object[] {ejbName}));
                return result;
            }
        }catch(Exception ex){
            oneFailed = true;
            addErrorDetails(result, compName);
            result.addErrorDetails(smh.getLocalString
                (getClass().getName() + ".notRun",
                "NOT RUN [AS-EJB] : Could not create descriptor object"));
                return result;
        }
        
	if (oneFailed) 
        {
	    result.setStatus(Result.FAILED);
        }
        else if(oneWarning)
        {
            result.setStatus(Result.WARNING);
        }   
        else
        {
        addErrorDetails(result, compName);
	    result.passed
		(smh.getLocalString
		 (getClass().getName() + ".passed",
		  "PASSED [AS-EJB] :  {0} ejb refernce is verified",
		  new Object[] {ejbName, jndiName}));
	}
        return result;
        
    }
}
