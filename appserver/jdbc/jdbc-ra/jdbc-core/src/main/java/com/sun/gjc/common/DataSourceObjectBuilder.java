/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation.
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

import static com.sun.gjc.common.DataSourceSpec.CLASSNAME;
import static com.sun.gjc.common.DataSourceSpec.DATABASENAME;
import static com.sun.gjc.common.DataSourceSpec.DATASOURCENAME;
import static com.sun.gjc.common.DataSourceSpec.DELIMITER;
import static com.sun.gjc.common.DataSourceSpec.DESCRIPTION;
import static com.sun.gjc.common.DataSourceSpec.DRIVERPROPERTIES;
import static com.sun.gjc.common.DataSourceSpec.ESCAPECHARACTER;
import static com.sun.gjc.common.DataSourceSpec.INITIALPOOLSIZE;
import static com.sun.gjc.common.DataSourceSpec.LOGINTIMEOUT;
import static com.sun.gjc.common.DataSourceSpec.LOGWRITER;
import static com.sun.gjc.common.DataSourceSpec.MAXIDLETIME;
import static com.sun.gjc.common.DataSourceSpec.MAXPOOLSIZE;
import static com.sun.gjc.common.DataSourceSpec.MAXSTATEMENTS;
import static com.sun.gjc.common.DataSourceSpec.MINPOOLSIZE;
import static com.sun.gjc.common.DataSourceSpec.NETWORKPROTOCOL;
import static com.sun.gjc.common.DataSourceSpec.PASSWORD;
import static com.sun.gjc.common.DataSourceSpec.PORTNUMBER;
import static com.sun.gjc.common.DataSourceSpec.PROPERTYCYCLE;
import static com.sun.gjc.common.DataSourceSpec.ROLENAME;
import static com.sun.gjc.common.DataSourceSpec.SERVERNAME;
import static com.sun.gjc.common.DataSourceSpec.USERNAME;
import static java.util.Arrays.asList;
import static java.util.logging.Level.SEVERE;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.glassfish.internal.api.Globals;

import com.sun.enterprise.util.i18n.StringManager;
import com.sun.gjc.util.MethodExecutor;
import com.sun.logging.LogDomains;

import jakarta.resource.ResourceException;

/**
 * Utility class, which would create necessary Datasource object according to
 * the specification.
 *
 * @author Binod P.G
 * @version 1.0, 02/07/23
 * @see com.sun.gjc.common.DataSourceSpec
 * @see com.sun.gjc.util.MethodExcecutor
 */
public class DataSourceObjectBuilder implements Serializable {

    private static final long serialVersionUID = 1L;

    private static Logger _logger = LogDomains.getLogger(MethodExecutor.class, LogDomains.RSR_LOGGER);
    private static final StringManager sm = StringManager.getManager(DataSourceObjectBuilder.class);

    private final DataSourceSpec spec;
    private final MethodExecutor executor;

    /**
     * Construct a DataSource Object from the spec.
     *
     * @param spec <code> DataSourceSpec </code> object.
     */
    public DataSourceObjectBuilder(DataSourceSpec spec) {
        this.spec = spec;
        executor = new MethodExecutor();
    }

    /**
     * Construct the DataSource Object from the spec.
     *
     * @return Object constructed using the DataSourceSpec.
     * @throws ResourceException if the class is not found or some issue in executing some method.
     */
    public Object constructDataSourceObject() throws ResourceException {
        Map<String, List<String>> driverProperties = parseDriverProperties(spec, true);
        Object dataSourceObject = getDataSourceObject();

        for (Method method : dataSourceObject.getClass().getMethods()) {
            String methodName = method.getName();

            // Check for driver properties first since some jdbc properties
            // may be supported in form of driver properties
            if (driverProperties.containsKey(methodName.toUpperCase(Locale.getDefault()))) {
                executor.runMethod(method, dataSourceObject, driverProperties.get(methodName.toUpperCase(Locale.getDefault())));

            } else if (methodName.equalsIgnoreCase("setUser")) {
                executor.runJavaBeanMethod(spec.getDetail(USERNAME), method, dataSourceObject);

            } else if (methodName.equalsIgnoreCase("setPassword")) {
                executor.runJavaBeanMethod(spec.getDetail(PASSWORD), method, dataSourceObject);

            } else if (methodName.equalsIgnoreCase("setLoginTimeOut")) {
                executor.runJavaBeanMethod(spec.getDetail(LOGINTIMEOUT), method, dataSourceObject);

            } else if (methodName.equalsIgnoreCase("setLogWriter")) {
                executor.runJavaBeanMethod(spec.getDetail(LOGWRITER), method, dataSourceObject);

            } else if (methodName.equalsIgnoreCase("setDatabaseName")) {
                executor.runJavaBeanMethod(spec.getDetail(DATABASENAME), method, dataSourceObject);

            } else if (methodName.equalsIgnoreCase("setDataSourceName")) {
                executor.runJavaBeanMethod(spec.getDetail(DATASOURCENAME), method, dataSourceObject);

            } else if (methodName.equalsIgnoreCase("setDescription")) {
                executor.runJavaBeanMethod(spec.getDetail(DESCRIPTION), method, dataSourceObject);

            } else if (methodName.equalsIgnoreCase("setNetworkProtocol")) {
                executor.runJavaBeanMethod(spec.getDetail(NETWORKPROTOCOL), method, dataSourceObject);

            } else if (methodName.equalsIgnoreCase("setPortNumber")) {
                executor.runJavaBeanMethod(spec.getDetail(PORTNUMBER), method, dataSourceObject);

            } else if (methodName.equalsIgnoreCase("setRoleName")) {
                executor.runJavaBeanMethod(spec.getDetail(ROLENAME), method, dataSourceObject);

            } else if (methodName.equalsIgnoreCase("setServerName")) {
                executor.runJavaBeanMethod(spec.getDetail(SERVERNAME), method, dataSourceObject);

            } else if (methodName.equalsIgnoreCase("setMaxStatements")) {
                executor.runJavaBeanMethod(spec.getDetail(MAXSTATEMENTS), method, dataSourceObject);

            } else if (methodName.equalsIgnoreCase("setInitialPoolSize")) {
                executor.runJavaBeanMethod(spec.getDetail(INITIALPOOLSIZE), method, dataSourceObject);

            } else if (methodName.equalsIgnoreCase("setMinPoolSize")) {
                executor.runJavaBeanMethod(spec.getDetail(MINPOOLSIZE), method, dataSourceObject);

            } else if (methodName.equalsIgnoreCase("setMaxPoolSize")) {
                executor.runJavaBeanMethod(spec.getDetail(MAXPOOLSIZE), method, dataSourceObject);

            } else if (methodName.equalsIgnoreCase("setMaxIdleTime")) {
                executor.runJavaBeanMethod(spec.getDetail(MAXIDLETIME), method, dataSourceObject);

            } else if (methodName.equalsIgnoreCase("setPropertyCycle")) {
                executor.runJavaBeanMethod(spec.getDetail(PROPERTYCYCLE), method, dataSourceObject);
            }
        }

        return dataSourceObject;
    }

    /**
     * Get the extra driver properties from the DataSourceSpec object and parse them
     * to a set of methodName and parameters. Prepare a hashtable containing these
     * details and return.
     *
     * @param spec <code> DataSourceSpec </code> object.
     * @return Hashtable containing method names and parameters,
     * @throws ResourceException If delimiter is not provided and property string is
     * not null.
     */
    public Map<String, List<String>> parseDriverProperties(DataSourceSpec spec, boolean returnUpperCase) throws ResourceException {
        String delim = spec.getDetail(DELIMITER);
        String escape = spec.getDetail(ESCAPECHARACTER);
        String prop = spec.getDetail(DRIVERPROPERTIES);

        if (prop == null || prop.trim().equals("")) {
            return new HashMap<>();
        }

        if (delim == null || delim.equals("")) {
            throw new ResourceException(sm.getString("dsob.delim_not_specified"));
        }

        if (escape == null || escape.equals("")) {
            throw new ResourceException(sm.getString("dsob.escape_char_not_specified"));
        }

        return parseDriverProperties(prop, escape, delim, returnUpperCase);
    }

    /**
     * parse the driver properties and re-generate name value pairs with unescaped
     * values.
     *
     * @param values driverProperties
     * @param escape escape character
     * @param delimiter delimiter
     * @return Hashtable
     */
    public Map<String, List<String>> parseDriverProperties(String values, String escape, String delimiter, boolean returnUpperCase) {
        Map<String, List<String>> parsedDriverProperties = new HashMap<>();
        String parsedValue = "";
        String name = "";

        char escapeChar = escape.charAt(0);
        char delimiterChar = delimiter.charAt(0);
        while (values.length() > 0) {
            if (values.charAt(0) == delimiterChar) {
                if (values.length() > 1 && values.charAt(1) == delimiterChar) {
                    if (values.length() > 2 && values.charAt(2) == delimiterChar) {

                        // Check for first property that does not have a value
                        // There is no value specified for this property.
                        // Store the name or it will be lost
                        if (returnUpperCase) {
                            name = parsedValue.toUpperCase(Locale.getDefault());
                        } else {
                            name = parsedValue;
                        }

                        // no value specified for value
                        parsedValue = "";
                    }

                    parsedDriverProperties.put(name, asList(parsedValue));

                    parsedValue = "";
                    values = values.substring(2);
                } else {
                    if (returnUpperCase) {
                        name = parsedValue.toUpperCase(Locale.getDefault());
                    } else {
                        name = parsedValue;
                    }
                    parsedValue = "";
                    values = values.substring(1);
                }
            } else if (values.charAt(0) == escapeChar) {
                if (values.charAt(1) == escapeChar) {
                    parsedValue += values.charAt(1);
                } else if (values.charAt(1) == delimiterChar) {
                    parsedValue += values.charAt(1);
                }
                values = values.substring(2);
            } else if (values.charAt(0) != escapeChar) {
                parsedValue += values.charAt(0);
                values = values.substring(1);
            }
        }

        return parsedDriverProperties;
    }

    /**
     * Creates a Datasource object according to the spec.
     *
     * @return Initial DataSource Object instance.
     * @throws <code>ResourceException</code> If class name is wrong or classpath is
     * not set properly.
     */
    private Object getDataSourceObject() throws ResourceException {
        String className = spec.getDetail(CLASSNAME);

        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Class<?> dataSourceClass;
            try {
                dataSourceClass = Class.forName(className, true, classLoader);
            } catch (ClassNotFoundException cnfe) {
                // OSGi-ed apps can't see lib dir, so try using CommonClassLoader
                classLoader = Globals.get(ClassLoaderHierarchy.class).getCommonClassLoader();
                dataSourceClass = Class.forName(className, true, classLoader);
            }

            return dataSourceClass.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException cnfe) {
            _logger.log(SEVERE, "jdbc.exc_cnfe_ds", cnfe);
            throw new ResourceException(sm.getString("dsob.class_not_found", className), cnfe);
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException ce) {
            _logger.log(SEVERE, "jdbc.exc_inst", className);
            throw new ResourceException(sm.getString("dsob.error_instantiating", className), ce);
        } catch (IllegalAccessException ce) {
            _logger.log(SEVERE, "jdbc.exc_acc_inst", className);
            throw new ResourceException(sm.getString("dsob.access_error", className), ce);
        }
    }

}
