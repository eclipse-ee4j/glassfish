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

package org.glassfish.api.jdbc;

import java.io.Serializable;

/**
 * Information related to SQL operations executed by the applications are stored in this object.
 *
 * This trace record is used to log all the sql statements in a particular format.
 *
 * @author Shalini M
 */
public class SQLTraceRecord implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Thread ID from which SQL statement originated.
     */
    private long threadID;

    /**
     * Thread Name from which SQL statement originated.
     */
    private String threadName;

    /**
     * Pool Name in which the SQL statement is executed.
     */
    private String poolName;

    /**
     * Type of SQL query. Could be PreparedStatement, CallableStatement or other object types.
     */
    private String className;

    /**
     * Method that executed the query.
     */
    private String methodName;

    /**
     * Time of execution of query.
     */
    private long timeStamp;

    /**
     * Parameters of the method that executed the SQL query. Includes information like SQL query, arguments and so on.
     */
    private Object[] params;

    /**
     * Gets the class name of the SQL query expressed as a String.
     *
     * @return The class name of the SQL query expressed as a String.
     */
    public String getClassName() {
        return className;
    }

    /**
     * Sets the class name of the SQL query expressed as a String.
     *
     * @param className class name of the SQL query.
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * Gets the method name that executed the SQL query.
     *
     * @return methodName that executed the SQL query.
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Sets the method name that executes the SQL query.
     *
     * @param methodName that executes the SQL query.
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /**
     * Gets the pool name in which the SQL statement is executed.
     *
     * @return poolName in which the SQL statement is executed.
     */
    public String getPoolName() {
        return poolName;
    }

    /**
     * Sets the poolName in which the SQL statement is executed.
     *
     * @param poolName in which the SQL statement is executed.
     */
    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }

    /**
     * Gets the thread ID from which the SQL statement originated.
     *
     * @return long threadID from which the SQL statement originated.
     */
    public long getThreadID() {
        return threadID;
    }

    /**
     * Sets the thread ID from which the SQL statement originated.
     *
     * @param threadID from which the SQL statement originated.
     */
    public void setThreadID(long threadID) {
        this.threadID = threadID;
    }

    /**
     * Gets the thread Name from which the SQL statement originated.
     *
     * @return String threadName from which the SQL statement originated.
     */
    public String getThreadName() {
        return threadName;
    }

    /**
     * Sets the thread Name from which the SQL statement originated.
     *
     * @param threadName from which the SQL statement originated.
     */
    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    /**
     * Gets the time of execution of query.
     *
     * @return long timeStamp of execution of query.
     */
    public long getTimeStamp() {
        return timeStamp;
    }

    /**
     * Sets the time of execution of query.
     *
     * @param timeStamp of execution of query.
     */
    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    /**
     * Gets the parameters of the method that executed the SQL query. Includes information like SQL query, arguments and so
     * on.
     *
     * @return Object[] params method parameters that execute SQL query.
     */
    public Object[] getParams() {
        return params;
    }

    /**
     * Sets the parameters of the method that executed the SQL query. Includes information like SQL query, arguments and so
     * on.
     *
     * @param params method parameters that execute SQL query.
     */
    public void setParams(Object[] params) {
        this.params = params;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("ThreadID=" + getThreadID() + " | ");
        sb.append("ThreadName=" + getThreadName() + " | ");
        sb.append("TimeStamp=" + getTimeStamp() + " | ");
        sb.append("ClassName=" + getClassName() + " | ");
        sb.append("MethodName=" + getMethodName() + " | ");
        if (params != null && params.length > 0) {
            int index = 0;
            for (Object param : params) {
                sb.append("arg[" + index++ + "]=" + (param != null ? param.toString() : "null") + " | ");
            }
        }
        // TODO add poolNames and other fields of this record.
        return sb.toString();
    }
}
