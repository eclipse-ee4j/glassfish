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
 * JDOQLElements.java
 *
 * Created on November 12, 2001
 */

package com.sun.jdo.spi.persistence.support.ejb.ejbqlc;

/**
 * An JDOQLElements instance represents the result of the EJBQLC compile step.
 *
 * @author  Michael Bouschen
 * @author  Shing Wai Chan
 */
public class JDOQLElements
{
    /** The name of the candidate class */
    private String candidateClassName;

    /** The parameter declarations string. */
    private String parameters;

    /** The variable declarations string. */
    private String variables;

    /** The filter expression string. */
    private String filter;

    /** The ordering expression string. */
    private String ordering;

    /** The result expression. */
    private String result;

    /** The result type. */
    private String resultType;

    /** Flag indicating whether the result element is of a pc class. */
    private boolean isPCResult;

    /**
     *  Flag indicating whether the result element is associated to an
     *  aggregate function.
     */
    private boolean isAggregate;

    /** String array contains ejb names corresponding to parameters */
    private String[] parameterEjbNames;

    /**
     * Constructor taking JDOQL elements.
     */
    public JDOQLElements(String candidateClassName,
                         String parameters,
                         String variables,
                         String filter,
                         String ordering,
                         String result,
                         String resultType,
                         boolean isPCResult,
                         boolean isAggregate,
                         String[] parameterEjbNames)
    {
        setCandidateClassName(candidateClassName);
        setParameters(parameters);
        setVariables(variables);
        setFilter(filter);
        setOrdering(ordering);
        setResult(result);
        setResultType(resultType);
        setPCResult(isPCResult);
        setAggregate(isAggregate);
        setParameterEjbNames(parameterEjbNames);
    }

    /** Returns the fully qulified name of the candidate class. */
    public String getCandidateClassName()
    {
        return this.candidateClassName;
    }

    /** Sets the fully qulified name of the candidate class. */
    public void setCandidateClassName(String candidateClassName)
    {
        // TBD: check non empty candidateClassName
        this.candidateClassName = candidateClassName;
    }

    /** Returns the parameter declaration string. */
    public String getParameters()
    {
        return parameters;
    }

    /** Sets the parameter declarations string. */
    public void setParameters(String parameters)
    {
        this.parameters = (parameters == null) ? "" : parameters; //NOI18N
    }

    /** Returns the variable declarations string. */
    public String getVariables()
    {
        return variables;
    }

    /** Sets the variable declarations string. */
    public void setVariables(String variables)
    {
        this.variables = (variables == null) ? "" : variables; //NOI18N
    }

    /** Returns the filter expression. */
    public String getFilter()
    {
        return filter;
    }

    /** Sets the filter expression. */
    public void setFilter(String filter)
    {
        this.filter = (filter == null) ? "" : filter; //NOI18N
    }

    /** Returns the ordering expression. */
    public String getOrdering()
    {
        return ordering;
    }

    /** Sets the ordering expression. */
    public void setOrdering(String ordering)
    {
        this.ordering = (ordering == null) ? "" : ordering; //NOI18N
    }

    /** Returns the result expression. */
    public String getResult()
    {
        return result;
    }

    /** Sets the result expression. */
    public void setResult(String result)
    {
        this.result = (result == null) ? "" : result; //NOI18N
    }

    /**
     * Returns the result type. The result type is the name of the element type
     * of the JDO query result set.
     */
    public String getResultType()
    {
        return resultType;
    }

    /**
     * Sets the result type. The result type is the name of the element type
     * of the JDO query result set.
     */
    public void setResultType(String resultType)
    {
        this.resultType = resultType;
    }

    /**
     * Returns whether the result of the JDOQL query is a collection of pc
     * instances or not.
     */
    public boolean isPCResult()
    {
        return isPCResult;
    }

    /**
     * Sets whether the result of the JDOQL query is a collection of pc
     * instances or not.
     */
    public void setPCResult(boolean isPCResult)
    {
        this.isPCResult = isPCResult;
    }

    /**
     * Returns whether the result of the JDOQL query is associated to
     * an aggregate function.
     */
    public boolean isAggregate()
    {
        return isAggregate;
    }

    /**
     * Sets whether the result of the JDOQL query is a associated to
     * an aggregate function.
     */
    public void setAggregate(boolean isAggregate)
    {
        this.isAggregate = isAggregate;
    }

    /**
     * Returns parameterEjbNames array
     */
    public String[] getParameterEjbNames()
    {
        return parameterEjbNames;
    }

    /**
     * set parameterEjbNames array
     */
    public void setParameterEjbNames(String[] parameterEjbNames)
    {
        this.parameterEjbNames = parameterEjbNames;
    }

    /** Returns a string representation of this JDOQLElements instance. */
    public String toString()
    {
        StringBuffer repr = new StringBuffer();
        repr.append("JDOQLElements("); //NOI18N
        repr.append("candidateClass: "); //NOI18N
        repr.append(candidateClassName);
        if (parameters != null && parameters.length() > 0) {
            repr.append(", parameters: "); //NOI18N
            repr.append(parameters);
        }
        if (variables != null && variables.length() > 0) {
            repr.append(", variables: "); //NOI18N
            repr.append(variables);
        }
        if (filter != null && filter.length() > 0) {
            repr.append(", filter: "); //NOI18N
            repr.append(filter);
        }
        if (ordering != null && ordering.length() > 0) {
            repr.append(", ordering: "); //NOI18N
            repr.append(ordering);
        }
        if (result != null && result.length() > 0) {
            repr.append(", result: "); //NOI18N
            repr.append(result);
            repr.append(", resultType: "); //NOI18N
            repr.append(resultType);
            repr.append(", isPCResult: "); //NOI18N
            repr.append(isPCResult);
        }
        repr.append(", isAggregate: ");
        repr.append(isAggregate);
        if (parameterEjbNames != null && parameterEjbNames.length > 0) {
            repr.append(", parameterEjbNames: "); //NOI18N
            for (int i = 0; i < parameterEjbNames.length; i++) {
                repr.append(i);
                repr.append(": ");
                repr.append(parameterEjbNames[i]);
                repr.append(", ");
            }
        }
        repr.append(")"); //NOI18N
        return repr.toString();
    }
}
