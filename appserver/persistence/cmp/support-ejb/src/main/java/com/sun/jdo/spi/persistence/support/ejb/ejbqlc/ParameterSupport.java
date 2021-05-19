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
 * ParameterSupport.java
 *
 * Created on December 07, 2001
 */

package com.sun.jdo.spi.persistence.support.ejb.ejbqlc;

import java.lang.reflect.Method;
import java.util.ResourceBundle;

import org.glassfish.persistence.common.I18NHelper;

/**
 * Helper class to handle EJBQL query parameters.
 *
 * @author  Michael Bouschen
 * @author  Shing Wai Chan
 */
public class ParameterSupport
{
    /** The types of the parameters of the finder/selector method. */
    private Class[] parameterTypes;

    /**
     * The EJB names corresponding to types of parameters of the
     * finder/selector method.
     */
    private String[] parameterEjbNames;

    /** I18N support. */
    protected final static ResourceBundle msgs = I18NHelper.loadBundle(
        ParameterSupport.class);

    /**
     * Constructor.
     * @param method the Method instance of the finder/selector method.
     */
    public ParameterSupport(Method method)
    {
        this.parameterTypes =
            (method == null) ? new Class[0] : method.getParameterTypes();
        this.parameterEjbNames = new String[this.parameterTypes.length];
    }

    /**
     * Returns type of the EJBQL parameter by input parameter declaration
     * string. The specified string denotes a parameter application in EJBQL.
     * It has the form "?<number>" where <number> is the parameter number
     * starting with 1.
     * @return class instance representing the parameter type.
     */
    public Class getParameterType(String ejbqlParamDecl)
    {
        return getParameterType(getParamNumber(ejbqlParamDecl));
    }

    /**
     * Returns the type of the EJBQL parameter by number.
     * Note, the numbering of EJBQL parameters starts with 1,
     * so the method expects 1 as the number of the first parameter.
     * @return class instance representing the parameter type.
     */
    public Class getParameterType(int paramNumber)
    {
        // InputParams are numbered starting at 1, so adjust for
        // array indexing.
        return parameterTypes[paramNumber - 1];
    }

    /**
     * Get EJB name corresponding to the EJBQL parameter by input
     * parameter declaration string.
     * @param ejbqlParamDecl denotes a parameter application in EJBQL.
     * It has the form "?<number>" where <number> is the parameter number
     * starting with 1.
     * @return class instance representing the parameter type.
     */
    public String getParameterEjbName(String ejbqlParamDecl)
    {
        return getParameterEjbName(getParamNumber(ejbqlParamDecl));
    }

    /**
     * Get EJB name corresponding to the EJBQL parameter number.
     * @param paramNumber numbering of parameters starting with 1
     * @return class instance representing the parameter type.
     */
    public String getParameterEjbName(int paramNumber)
    {
        return parameterEjbNames[paramNumber - 1];
    }

    /**
     * Set EJB name corresponding to the EJBQL parameter by input
     * parameter declaration string.
     * @param ejbqlParamDecl denotes a parameter application in EJBQL.
     * It has the form "?<number>" where <number> is the parameter number
     * starting with 1.
     * @param ejbName
     */
    public void setParameterEjbName(String ejbqlParamDecl, String ejbName)
    {
        parameterEjbNames[getParamNumber(ejbqlParamDecl) - 1] = ejbName;
    }

    /**
     * Get all EJB names corresponding to the EJBQL parameters.
     * @return class instance representing the parameter type.
     */
    public String[] getParameterEjbNames()
    {
        return parameterEjbNames;
    }

    /**
     * Returns the name of the corresponding JDO parameter.
     * The specified string denotes a parameter application in EJBQL.
     * It has the form "?<number>" where <number> is the parameter number
     * starting with 1.
     * @return name of JDOQL parameter
     */
    public String getParameterName(String ejbqlParamDecl)
    {
        return getParameterName(getParamNumber(ejbqlParamDecl));
    }

    /**
     * Returns the name of the corresponding JDO parameter by parameter number.
     * @return name of JDOQL parameter
     */
    public String getParameterName(int paramNumber)
    {
        return "_jdoParam" + String.valueOf(paramNumber);
    }

    /**
     * Returns the number of parameters.
     * @return parameter count.
     */
    public int getParameterCount()
    {
        return parameterTypes.length;
    }

    // Internal methods

    /**
     * Internal method to extract the number from a parameter application
     * in EJBQL.
     */
    private int getParamNumber(String ejbqlParamDecl)
    {
        int paramNum = 0;
        try {
            paramNum = Integer.parseInt(ejbqlParamDecl.substring(1));
        } catch(Exception ex) {
            ErrorMsg.error(I18NHelper.getMessage(
                msgs, "EXC_InvalidParameterIndex", //NOI18N
                ejbqlParamDecl, String.valueOf(parameterTypes.length)));
        }
        if (paramNum < 1 || paramNum > parameterTypes.length) {
            ErrorMsg.error(I18NHelper.getMessage(
                msgs, "EXC_InvalidParameterIndex", //NOI18N
                ejbqlParamDecl, String.valueOf(parameterTypes.length)));
        }
        return paramNum;
    }
}
