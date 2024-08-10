/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.glassfish.ejb.deployment.descriptor;

import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.deployment.util.DOLUtils;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.deployment.common.Descriptor;

/**
 * This class contains information about EJB-QL queries for
 * finder/selector methods of EJB2.0 CMP EntityBeans.
 * It represents the <query> XML element.
 *
 * @author Sanjeev Krishnan
 */
public final class QueryDescriptor extends Descriptor {

    // For EJB2.0: the query is either a string in EJB-QL or is empty.
    // For EJB1.1: the query empty (only sql is available)
    private String query;

    // SQL query corresponding to EJB-QL or English
    private String sql;

    private MethodDescriptor methodDescriptor;
    private transient Method method;


    // Deployment information used to specify whether ejbs
    // returned by a select query should be materialized as
    // EJBLocalObject or EJBObject.  This property is optional
    // and is only applicable for ejbSelect methods that
    // select ejbs.

    private static final int NO_RETURN_TYPE_MAPPING = 0;
    private static final int RETURN_LOCAL_TYPES     = 1;
    private static final int RETURN_REMOTE_TYPES    = 2;

    private int returnTypeMapping;

    // Create logger object per Java SDK 1.4 to log messages
    // introduced Santanu De, Sun Microsystems, March 2002

    static Logger _logger = DOLUtils.getDefaultLogger();

    public QueryDescriptor() {
        this.query = null;
        this.sql = null;
        this.returnTypeMapping = NO_RETURN_TYPE_MAPPING;
    }

    public QueryDescriptor(QueryDescriptor otherQuery, Method m) {
        this.query = otherQuery.query;
        this.sql   = otherQuery.sql;
        this.method = m;
        this.returnTypeMapping = otherQuery.returnTypeMapping;
    }

/**
    public void setQueryMethod(MethodDescriptor md)
    {
    this.methodDescriptor = md;
    }

    public MethodDescriptor getQueryMethod()
    {
    return methodDescriptor;
    }
**/

    public void setQueryMethod(Method m) {
        this.method = m;
    }


    public Method getQueryMethod() {
        return method;
    }


    public void setQueryMethodDescriptor(MethodDescriptor m) {
        methodDescriptor = m;
    }


    public MethodDescriptor getQueryMethodDescriptor() {
        return methodDescriptor;
    }


    public boolean getIsEjbQl() {
        return (query != null);
    }

    /**
     * Set the EJB-QL query (ejb-ql XML element).  If query parameter
     * is null, or has no content, getIsEjbQl will return false.
     * Otherwise, getIsEjbQl will return true.
     */
    public void setQuery(String query) {
         _logger.log(Level.FINE,"input query = '" + query + "'");

        String newQuery = (query != null) ? query.trim() : null;
        if( (newQuery != null) && newQuery.equals("") ) {
            newQuery = null;
        }
        if( newQuery == null ) {
            _logger.log(Level.FINE,"query has no content -- setting to NULL");
        } else {
            _logger.log(Level.FINE,"setting query to '" + newQuery + "'");
        }
        this.query = newQuery;
    }


    /**
     * Get the EJB-QL query (ejb-ql XML element)
     */
    public String getQuery() {
        return query;
    }


    public boolean getHasSQL() {
        return (this.sql != null);
    }


    public void setSQL(String sql) {
        this.sql = sql;
    }


    public String getSQL() {
        return sql;
    }


    // Returns true if no return type mapping has been specified
    public boolean getHasNoReturnTypeMapping() {
        return (returnTypeMapping == NO_RETURN_TYPE_MAPPING);
    }

    // Returns true only if a local return type has been specified.
    public boolean getHasLocalReturnTypeMapping() {
        return (returnTypeMapping == RETURN_LOCAL_TYPES);
    }

    // Returns true only if a remote return type has been specified.
    public boolean getHasRemoteReturnTypeMapping() {
        return (returnTypeMapping == RETURN_REMOTE_TYPES);
    }

    public void setHasNoReturnTypeMapping() {
        returnTypeMapping = NO_RETURN_TYPE_MAPPING;
    }

    public void setHasLocalReturnTypeMapping() {
        returnTypeMapping = RETURN_LOCAL_TYPES;
    }

    public void setHasRemoteReturnTypeMapping() {
        returnTypeMapping = RETURN_REMOTE_TYPES;
    }

    public int getReturnTypeMapping() {
        return returnTypeMapping;
    }


    @Override
    public void print(StringBuffer toStringBuffer) {
        toStringBuffer.append("Query ");
        if (getQueryMethodDescriptor() != null) {
            getQueryMethodDescriptor().print(toStringBuffer);
        }
        toStringBuffer.append("\n");
        if (getHasSQL()) {
            toStringBuffer.append("SQL : ").append(getSQL());
            return;
        }
        if (getIsEjbQl()) {
            toStringBuffer.append("EJB QL: ").append(query);
            return;
        }
        toStringBuffer.append(" No query associated");
    }

}

