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

package com.sun.ejb.codegen;

import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.logging.LogDomains;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.ejb.deployment.descriptor.ContainerTransaction;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbSessionDescriptor;


/**
 * The base class for all code generators.
 */
public abstract class Generator {

    protected static final Logger _logger = LogDomains.getLogger(Generator.class, LogDomains.EJB_LOGGER);

    protected String ejbClassSymbol;

    public abstract String getGeneratedClass();


    /**
     * Get the package name from the class name.
     * @param className class name.
     * @return the package name.
     */
    protected String getPackageName(String className) {
        int dot = className.lastIndexOf('.');
        if (dot == -1)
            return null;
        return className.substring(0, dot);
    }

    protected String getBaseName(String className) {
        int dot = className.lastIndexOf('.');
        if (dot == -1)
            return className;
        return className.substring(dot+1);
    }

    protected String printType(Class cls) {
        if (cls.isArray()) {
            return printType(cls.getComponentType()) + "[]";
        } else {
            return cls.getName();
        }
    }

    protected Method[] removeDups(Method[] orig) {
        // Remove duplicates from method array.
        // Duplicates will arise if a class/intf and super-class/intf
        // define methods with the same signature. Potentially the
        // throws clauses of the methods may be different (note Java
        // requires that the superclass/intf method have a superset of the
        // exceptions in the derived method).
        Vector nodups = new Vector();
        for ( int i=0; i<orig.length; i++ ) {
            Method m1 = orig[i];
            boolean dup = false;
            for ( Enumeration e=nodups.elements(); e.hasMoreElements(); ) {
                Method m2 = (Method)e.nextElement();

                // m1 and m2 are duplicates if they have the same signature
                // (name and same parameters).
                if ( !m1.getName().equals(m2.getName()) )
                    continue;

                Class[] m1parms = m1.getParameterTypes();
                Class[] m2parms = m2.getParameterTypes();
                if ( m1parms.length != m2parms.length )
                    continue;

                boolean parmsDup = true;
                for ( int j=0; j<m2parms.length; j++ ) {
                    if ( m1parms[j] != m2parms[j] ) {
                        parmsDup = false;
                        break;
                    }
                }
                if ( parmsDup ) {
                    dup = true;
                    // Select which of the duplicate methods to generate
                    // code for: choose the one that is lower in the
                    // inheritance hierarchy: this ensures that the generated
                    // method will compile.
                    if ( m2.getDeclaringClass().isAssignableFrom(
                                                    m1.getDeclaringClass()) ) {
                        // m2 is a superclass/intf of m1, so replace m2 with m1
                        nodups.remove(m2);
                        nodups.add(m1);
                    }
                    break;
                }
            }

            if ( !dup )
                nodups.add(m1);
        }
        return (Method[])nodups.toArray(new Method[nodups.size()]);
    }

    /**
     * Return true if method is on a jakarta.ejb.EJBObject/EJBLocalObject/
     * jakarta.ejb.EJBHome,jakarta.ejb.EJBLocalHome interface.
     */
    protected boolean isEJBIntfMethod(Class ejbIntfClz, Method methodToCheck) {
        boolean isEJBIntfMethod = false;

        Method[] ejbIntfMethods = ejbIntfClz.getMethods();
        for(int i = 0; i < ejbIntfMethods.length; i++) {
            Method next = ejbIntfMethods[i];
            if(methodCompare(methodToCheck, next)) {
                isEJBIntfMethod = true;

                String ejbIntfClzName  = ejbIntfClz.getName();
                Class methodToCheckClz = methodToCheck.getDeclaringClass();
                if( !methodToCheckClz.getName().equals(ejbIntfClzName) ) {
                    String[] logParams = {next.toString(), methodToCheck.toString()};
                    _logger.log(Level.WARNING,
                                "ejb.illegal_ejb_interface_override",
                                logParams);
                }

                break;
            }
        }

        return isEJBIntfMethod;
    }


    private boolean methodCompare(Method factoryMethod, Method homeMethod) {

        if (!factoryMethod.getName().equals(homeMethod.getName())) {
            return false;
        }

        Class[] factoryParamTypes = factoryMethod.getParameterTypes();
        Class[] beanParamTypes = homeMethod.getParameterTypes();
        if (factoryParamTypes.length != beanParamTypes.length) {
            return false;
        }
        for(int i = 0; i < factoryParamTypes.length; i++) {
            if (factoryParamTypes[i] != beanParamTypes[i]) {
                return false;
            }
        }

        // NOTE : Exceptions and return types are not part of equality check
        return true;
    }


    protected String getUniqueClassName(DeploymentContext context, String origName, String origSuffix,
        Vector existingClassNames) {
        String newClassName = null;
        boolean foundUniqueName = false;
        int count = 0;
        while (!foundUniqueName) {
            String suffix = origSuffix;
            if (count > 0) {
                suffix = origSuffix + count;
            }
            newClassName = origName + suffix;
            if (!existingClassNames.contains(newClassName)) {
                foundUniqueName = true;
                existingClassNames.add(newClassName);
            } else {
                count++;
            }
        }
        return newClassName;
    }


    protected String getTxAttribute(EjbDescriptor dd, Method method) {
        // The TX_* strings returned MUST match the TX_* constants in
        // com.sun.ejb.Container.
        if (dd instanceof EjbSessionDescriptor && ((EjbSessionDescriptor) dd).getTransactionType().equals("Bean")) {
            return "TX_BEAN_MANAGED";
        }

        String txAttr = null;
        MethodDescriptor mdesc = new MethodDescriptor(method, ejbClassSymbol);
        ContainerTransaction ct = dd.getContainerTransactionFor(mdesc);
        if (ct != null) {
            String attr = ct.getTransactionAttribute();
            if (attr.equals(ContainerTransaction.NOT_SUPPORTED)) {
                txAttr = "TX_NOT_SUPPORTED";
            } else if (attr.equals(ContainerTransaction.SUPPORTS)) {
                txAttr = "TX_SUPPORTS";
            } else if (attr.equals(ContainerTransaction.REQUIRED)) {
                txAttr = "TX_REQUIRED";
            } else if (attr.equals(ContainerTransaction.REQUIRES_NEW)) {
                txAttr = "TX_REQUIRES_NEW";
            } else if (attr.equals(ContainerTransaction.MANDATORY)) {
                txAttr = "TX_MANDATORY";
            } else if (attr.equals(ContainerTransaction.NEVER)) {
                txAttr = "TX_NEVER";
            }
        }

        if (txAttr == null) {
            throw new RuntimeException("Transaction Attribute not found for method " + method);
        }
        return txAttr;
    }

    protected String getSecurityAttribute(EjbDescriptor dd, Method m) {
        // The SEC_* strings returned MUST match the SEC_* constants in
        // com.sun.ejb.Container.
        MethodDescriptor thisMethodDesc = new MethodDescriptor(m, ejbClassSymbol);
        Set unchecked = dd.getUncheckedMethodDescriptors();
        if (unchecked != null) {
            Iterator i = unchecked.iterator();
            while (i.hasNext()) {
                MethodDescriptor md = (MethodDescriptor) i.next();
                if (thisMethodDesc.equals(md)) {
                    return "SEC_UNCHECKED";
                }
            }
        }

        Set excluded = dd.getExcludedMethodDescriptors();
        if (excluded != null) {
            Iterator i = excluded.iterator();
            while (i.hasNext()) {
                MethodDescriptor md = (MethodDescriptor) i.next();
                if (thisMethodDesc.equals(md)) {
                    return "SEC_EXCLUDED";
                }
            }
        }

        return "SEC_CHECKED";
    }
}
