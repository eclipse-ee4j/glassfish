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

package com.sun.gjc.common;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Encapsulate the DataSource object details obtained from
 * ManagedConnectionFactory.
 *
 * @author Binod P.G
 * @version 1.0, 02/07/23
 */
public class DataSourceSpec implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int USERNAME = 1;
    public static final int PASSWORD = 2;
    public static final int URL = 3;
    public static final int LOGINTIMEOUT = 4;
    public static final int LOGWRITER = 5;
    public static final int DATABASENAME = 6;
    public static final int DATASOURCENAME = 7;
    public static final int DESCRIPTION = 8;
    public static final int NETWORKPROTOCOL = 9;
    public static final int PORTNUMBER = 10;
    public static final int ROLENAME = 11;
    public static final int SERVERNAME = 12;
    public static final int MAXSTATEMENTS = 13;
    public static final int INITIALPOOLSIZE = 14;
    public static final int MINPOOLSIZE = 15;
    public static final int MAXPOOLSIZE = 16;
    public static final int MAXIDLETIME = 17;
    public static final int PROPERTYCYCLE = 18;
    public static final int DRIVERPROPERTIES = 19;
    public static final int CLASSNAME = 20;
    public static final int DELIMITER = 21;

    public static final int XADATASOURCE = 22;
    public static final int DATASOURCE = 23;
    public static final int CONNECTIONPOOLDATASOURCE = 24;

    public static final int CONNECTIONVALIDATIONREQUIRED = 25;
    public static final int VALIDATIONMETHOD = 26;
    public static final int VALIDATIONTABLENAME = 27;

    public static final int TRANSACTIONISOLATION = 28;
    public static final int GUARANTEEISOLATIONLEVEL = 29;

    public static final int STATEMENTTIMEOUT = 30;
    public static final int STATEMENTWRAPPING = 31;

    public static final int JDBC30DATASOURCE = 32;
    public static final int ESCAPECHARACTER = 33;
    public static final int STATEMENTCACHESIZE = 34;
    public static final int STATEMENTCACHETYPE = 35;
    public static final int VALIDATIONCLASSNAME = 36;
    public static final int CUSTOMVALIDATION = 37;
    public static final int SQLTRACELISTENERS = 38;
    public static final int INITSQL = 39;
    public static final int POOLMONITORINGSUBTREEROOT = 40;
    public static final int NUMBEROFTOPQUERIESTOREPORT = 41;
    public static final int TIMETOKEEPQUERIESINMINUTES = 42;
    public static final int STATEMENTLEAKTIMEOUTINSECONDS = 43;
    public static final int STATEMENTLEAKRECLAIM = 44;
    public static final int POOLNAME = 45;
    public static final int APPLICATIONNAME = 46;
    public static final int MODULENAME = 47;

    private ConcurrentHashMap<Integer, String> details = new ConcurrentHashMap<Integer, String>();

    /**
     * Set the property.
     *
     * @param property Property Name to be set.
     * @param value Value of property to be set.
     */
    public void setDetail(int property, String value) {
        details.put(property, value);
    }

    /**
     * Get the value of property
     *
     * @param property whose value is required
     * @return value of the property.
     */
    public String getDetail(int property) {
        if (details.containsKey(property)) {
            return details.get(property);
        }

        return null;
    }

    /**
     * Checks whether two <code>DataSourceSpec</code> objects are equal or not.
     *
     * @param obj Instance of <code>DataSourceSpec</code> object.
     */
    public boolean equals(Object obj) {
        if (obj instanceof DataSourceSpec) {
            return this.details.equals(((DataSourceSpec) obj).details);
        }

        return false;
    }

    /**
     * Retrieves the hashCode of this <code>DataSourceSpec</code> object.
     *
     * @return hashCode of this object.
     */
    public int hashCode() {
        return this.details.hashCode();
    }
}
