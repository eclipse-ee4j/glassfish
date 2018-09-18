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

package com.sun.enterprise.tools.verifier.tests.ejb.beanclass;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;


/** 
 * Enterprise Java Bean class static final fields test.  
 * An enterprise Bean must not use read/write static fields. Using read-only 
 * static fields is allowed. Therefore, it is recommended that all static 
 * fields in the enterprise bean class be declared as final. 
 * 
 * This rule is required to ensure consistent runtime semantics because while 
 * some EJB Containers may use a single JVM to execute all enterprise bean's 
 * instances, others may distribute the instances across multiple JVMs.
 */
public class EjbClassStaticFieldsFinal extends EjbTest implements EjbCheck { 



    /** 
     * Enterprise Java Bean class static final fields test.  
     * An enterprise Bean must not use read/write static fields. Using read-only 
     * static fields is allowed. Therefore, it is recommended that all static 
     * fields in the enterprise bean class be declared as final. 
     * 
     * This rule is required to ensure consistent runtime semantics because while 
     * some EJB Containers may use a single JVM to execute all enterprise bean's 
     * instances, others may distribute the instances across multiple JVMs.
     *
     * @param descriptor the Enterprise Java Bean deployment descriptor   
     *
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

	//  it is recommended that all static fields in the enterprise bean class 
	// be declared as final. 
	try {
	    Class c = Class.forName(descriptor.getEjbClassName(), false, getVerifierContext().getClassLoader());

	    boolean oneFailed = false;
	    boolean badField = false;
	    Field [] fields = c.getDeclaredFields();
	    for (int i = 0; i < fields.length; i++) {
		badField = false;
		// hack to prevent compiler bug, don't want to process fields such as
		// class$test$enforcer$hello$HelloHome, so if the field contains a "$"
		// just skip it, otherwise process normally.
		// if "$" does not occur as a substring, -1 is returned.
		if (fields[i].getName().indexOf("$") == -1) {
		    int modifiers = fields[i].getModifiers();
		    if (Modifier.isStatic(modifiers)) {
			if (Modifier.isFinal(modifiers)) {
			    continue;
			} else {
			    if (!oneFailed) {
				oneFailed = true;
			    }
			    badField = true;
			}
		    }
		}
  
		if (badField) {
		    result.addWarningDetails(smh.getLocalString
					     ("tests.componentNameConstructor",
					      "For [ {0} ]",
					      new Object[] {compName.toString()}));
		    result.warning(smh.getLocalString
				   (getClass().getName() + ".warning",
				    "Warning: Field [ {0} ] defined within bean class [ {1} ] is defined as static, but not defined as final.  An enterprise Bean must not use read/write static fields. Using read-only static fields is allowed.",
				    new Object[] {fields[i].getName(),(descriptor).getEjbClassName()}));
		}
	    }
	    if (!oneFailed) {
		result.addGoodDetails(smh.getLocalString
				      ("tests.componentNameConstructor",
				       "For [ {0} ]",
				       new Object[] {compName.toString()}));
		result.passed(smh.getLocalString
			      (getClass().getName() + ".passed",
			       "This bean class [ {0} ] has defined any and all static fields as final.",
			       new Object[] {(descriptor).getEjbClassName()}));
	    }
	} catch (ClassNotFoundException e) {
	    Verifier.debug(e);
	    result.addErrorDetails(smh.getLocalString
				   ("tests.componentNameConstructor",
				    "For [ {0} ]",
				    new Object[] {compName.toString()}));
	    result.failed(smh.getLocalString
			  (getClass().getName() + ".failedException",
			   "Error: [ {0} ] class not found.",
			   new Object[] {(descriptor).getEjbClassName()}));
	} catch (Exception e) {
	    Verifier.debug(e);
	    result.addErrorDetails(smh.getLocalString
				   ("tests.componentNameConstructor",
				    "For [ {0} ]",
				    new Object[] {compName.toString()}));
	    result.failed(smh.getLocalString
			  (getClass().getName() + ".failedException1",
			   "Error: [ {0} ] class encountered [ {1} ]",
			   new Object[] {(descriptor).getEjbClassName(),e.getMessage()}));
	} catch (Throwable t) {
	    result.addWarningDetails(smh.getLocalString
				     ("tests.componentNameConstructor",
				      "For [ {0} ]",
				      new Object[] {compName.toString()}));
	    result.warning(smh.getLocalString
			  (getClass().getName() + ".warningException",
			   "Warning: [ {0} ] class encountered [ {1} ]. Cannot access fields of class [ {2} ] which is external to [ {3} ].",
			   new Object[] {(descriptor).getEjbClassName(),t.toString(), t.getMessage(), descriptor.getEjbBundleDescriptor().getModuleDescriptor().getArchiveUri()}));
	}  
	return result;

    }
}
