/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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
import java.util.Optional;

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
     * Application Name which executed the SQL statement
     */
    private String applicationName;

    /**
     * Module Name which executed the SQL statement
     */
    private String moduleName;

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
     * The SQL query
     */
    private String sqlQuery;

    /**
     * Info about the method in the application that triggered the SQL query
     */
    private StackWalker.StackFrame callingApplicationMethod;

    /**
     * Gets the class name in the application which executed the SQL query, expressed as a String.
     * If it's not possible to detect the application, it will be the class name which directly invoked the connection.
     *
     * @return The class name of the SQL query expressed as a String.
     */
    public String getClassName() {
        return className;
    }

    /**
     * Set the class name which executed the SQL query.
     *
     * @param className class name of the SQL query.
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * Gets the method name in the application which executed the SQL query, expressed as a String.
     * If it's not possible to detect the application, it will be the method name which directly invoked the connection.
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

    /**
     * SQL query related to the database operation if applicable.
     *
     * @return SQL query
     */
    public Optional<String> getSqlQuery() {
        return Optional.ofNullable(sqlQuery);
    }

    /**
     * Set the SQL query related to the database operation
     *
     * @param sqlQuery
     */
    public void setSqlQuery(String sqlQuery) {
        this.sqlQuery = sqlQuery;
    }

    /**
     * Application Name which executed the SQL statement
     *
     * @return Application name
     */
    public String getApplicationName() {
        return applicationName;
    }

    /**
     * Set the application name which executed the SQL statement
     *
     * @param applicationName
     */
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    /**
     * Module name of an application which executed the SQL statement.
     * If the application doesn't have modules it will be equal to the application name
     *
     * @return Module name
     */
    public String getModuleName() {
        return moduleName;
    }

    /**
     * Set the module name which executed the SQL statement
     *
     * @param moduleName
     */
    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    /**
     * Get the stack frame for the method call in the application that triggered SQL execution, if the call comes from a deployed application.
     *
     * This returns a frame which returns a class from the {@link StackWalker.StackFrame#getDeclaringClass()} method
     * ({@link StackWalker.Option.RETAIN_CLASS_REFERENCE} is enabled).
     *
     * @return Stack frame that represents a call to a server component that triggered SQL execution
     */
    public Optional<StackWalker.StackFrame> getCallingApplicationMethod() {
        return Optional.ofNullable(callingApplicationMethod);
    }

    /**
     * Set the stack frame for the method call in the application that triggered SQL execution.
     * The {@link StackWalker.StackFrame#getDeclaringClass()} should return a class and never throw {@link UnsupportedOperationException}.
     */
    public void setCallingApplicationMethod(StackWalker.StackFrame callingApplicationMethod) {
        this.callingApplicationMethod = callingApplicationMethod;
    }



    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PoolName=").append(getPoolName()).append(" | ");
        sb.append("ThreadID=").append(getThreadID()).append(" | ");
        sb.append("ThreadName=").append(getThreadName()).append(" | ");
        sb.append("TimeStamp=").append(getTimeStamp()).append(" | ");
        sb.append("SQL=").append(getSqlQuery()).append(" | ");
        sb.append("AppName=").append(getApplicationName()).append(" | ");
        sb.append("ModuleName=").append(getModuleName()).append(" | ");
        sb.append("ClassName=").append(getClassName()).append(" | ");
        sb.append("MethodName=").append(getMethodName()).append(" | ");
        if (params != null && params.length > 0) {
            int index = 0;
            for (Object param : params) {
                sb.append("arg[").append(index++).append("]=")
                        .append(param != null ? param.toString() : "null").append(" | ");
            }
        }
        return sb.toString();
    }
}
