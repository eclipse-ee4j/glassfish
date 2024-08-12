/*
 * Copyright (c) 2021, 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.connectors.util;

import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.connectors.ActiveOutboundResourceAdapter;
import com.sun.enterprise.connectors.ActiveResourceAdapter;
import com.sun.enterprise.connectors.ConnectorRegistry;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.connectors.module.ConnectorApplication;
import com.sun.enterprise.deployment.ConnectorConfigProperty;
import com.sun.logging.LogDomains;

import jakarta.resource.spi.ResourceAdapter;
import jakarta.resource.spi.ResourceAdapterAssociation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *  This is an util class containing methods for parsing connector
 *  configurations present in ra.xml.
 *
 *  @author Srikanth P
 */
public class ConnectorConfigParserUtils {

    private static final Logger LOG = LogDomains.getLogger(ConnectorConfigParserUtils.class, LogDomains.RSR_LOGGER);


    /**
     * Merges the properties obtained by introspecting the javabean and the
     * properties present in ra.xml for the corresponding javabean.
     *
     * @param ddVals Properties obtained from ra.xml for the javabean
     * @param introspectedVals Properties obtained by introspecting javabean
     * @return Merged Properties present in ra.xml and introspected properties
     *         of javabean.
     */
    public Properties mergeProps(Set<ConnectorConfigProperty> ddVals, Properties introspectedVals) {
        Properties mergedVals = new Properties(introspectedVals);
        if (ddVals != null) {
            ConnectorConfigProperty[] ddProps = ddVals.toArray(ConnectorConfigProperty[]::new);
            for (ConnectorConfigProperty ddProp : ddProps) {
                mergedVals.setProperty(ddProp.getName(),ddProp.getValue());
            }
        }
        return mergedVals;
    }


    /**
     * Merges the datatype of properties obtained by introspecting the
     * javabean and the datatypes of properties present in ra.xml for
     * the corresponding javabean. It is a Properties object consisting of
     * property name and the property data type.
     *
     * @param ddVals Properties obtained from ra.xml for the javabean
     * @param introspectedVals Properties obtained by
     *            introspecting javabean which consist of property name as key
     *            and datatype as the value.
     * @return Merged Properties present in ra.xml and introspected properties
     *         of javabean. Properties consist of property name as the key
     *         and datatype as the value.
     */
    public Properties mergePropsReturnTypes(Set<ConnectorConfigProperty> ddVals, Properties introspectedVals) {
        Properties mergedVals = new Properties(introspectedVals);
        if (ddVals != null) {
            ConnectorConfigProperty[] ddProps = ddVals.toArray(ConnectorConfigProperty[]::new);
            for (ConnectorConfigProperty ddProp : ddProps) {
                mergedVals.setProperty(ddProp.getName(), ddProp.getType());
            }
        }
        return mergedVals;
    }


    public Properties introspectJavaBean(String className, Set ddPropsSet) throws ConnectorRuntimeException {
        return introspectJavaBean(className, ddPropsSet, false, null);
    }


    public Properties introspectJavaBean(String className, Set<ConnectorConfigProperty> ddPropsSet,
        boolean associateResourceAdapter, String resourceAdapterName) throws ConnectorRuntimeException {
        Class<?> loadedClass = loadClass(className, resourceAdapterName);
        Object loadedInstance = instantiate(loadedClass);
        try {
            if (associateResourceAdapter) {
                ActiveResourceAdapter activeRA = ConnectorRegistry.getInstance()
                    .getActiveResourceAdapter(resourceAdapterName);
                if (activeRA == null) {
                    // Check and Load RAR
                    ConnectorRuntime.getRuntime().loadDeferredResourceAdapter(resourceAdapterName);
                    activeRA = ConnectorRegistry.getInstance().getActiveResourceAdapter(resourceAdapterName);
                }

                // Associate RAR
                if (activeRA instanceof ActiveOutboundResourceAdapter) {
                    ResourceAdapter raInstance = activeRA.getResourceAdapter();
                    if (loadedInstance instanceof ResourceAdapterAssociation) {
                        ((ResourceAdapterAssociation) loadedInstance).setResourceAdapter(raInstance);
                    }
                }
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "rardeployment.error_associating_ra", e);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "Exception while associating the resource adapter " + "to the JavaBean", e);
            }
        }
        return introspectJavaBean(loadedInstance, ddPropsSet);
    }



    /**
     * Introspects the javabean and returns only the introspected properties
     * not present in the configuration in ra.xml for the corresponding
     * javabean. If no definite value is obtained while introspection of
     * a method empty string is taken as the value.
     *
     * @param javaBeanInstance bean
     * @param ddPropsSet Set of Properties present in configuration in ra.xml for
     *            the corresponding javabean.
     * @return Introspected properties not present in the configuration in
     *         ra.xml for the corresponding javabean.
     * @throws ConnectorRuntimeException if the Class could not be loaded
     *             or instantiated.
     */
    public Properties introspectJavaBean(Object javaBeanInstance, Set<ConnectorConfigProperty> ddPropsSet)
        throws ConnectorRuntimeException {
        Class<?> loadedClass = javaBeanInstance.getClass();
        Method[] methods = loadedClass.getMethods();
        Properties props = new Properties();
        String name = null;
        String value = null;
        final ConnectorConfigProperty[] ddProps = ddPropsSet == null
            ? null
            : ddPropsSet.toArray(ConnectorConfigProperty[]::new);
        for (Method method : methods) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Method -> " + method.getName() + ":" + method.getReturnType());
            }
            if (isProperty(method) && !presentInDDProps(method, ddProps) && isValid(method, loadedClass)) {
                name = getPropName(method);
                value = getPropValue(method, loadedClass, javaBeanInstance);
                props.setProperty(name, value);
            }
        }
        return props;
    }


    /**
     * Introspects the javabean and returns only the introspected properties
     * and their datatypes not present in the configuration in ra.xml for
     * the corresponding javabean.
     *
     * @param className Name of the class to be introspected.
     * @param ddPropsSet Set of Properties present in configuration in ra.xml for
     *            the corresponding javabean.
     * @return Introspected properties and their datatype not present in the
     *         configuration in ra.xml for the corresponding javabean. The
     *         properties consist of property name as the key and datatype as
     *         the value
     * @throws ConnectorRuntimeException if the Class could not be loaded
     */

    public Properties introspectJavaBeanReturnTypes(String className, Set<ConnectorConfigProperty> ddPropsSet, String rarName)
        throws ConnectorRuntimeException {
        Class<?> loadedClass = loadClass(className, rarName);
        Method[] methods = loadedClass.getMethods();
        Properties props = new Properties();
        String name = null;
        String value = null;
        final ConnectorConfigProperty[] ddProps = ddPropsSet == null
            ? null
            : ddPropsSet.toArray(ConnectorConfigProperty[]::new);
        for (Method method : methods) {
            if (isProperty(method) && !presentInDDProps(method, ddProps)) {
                name = getPropName(method);
                value = getPropType(method);
                if (value != null) {
                    props.setProperty(name, value);
                }
            }
        }
        return props;
    }


    /**
     * Checks whether the property pertaining to the method is already presenti
     * in the array of Properties passed as second argument.
     * The properties already present in ra.xml for the corresponding
     * javabean is passed as the second argument.
     */
    private boolean presentInDDProps(Method method, ConnectorConfigProperty[] ddProps) {
        String name = null;
        String ddPropName = null;
        int length = "set".length();
        if (method != null) {
            name = method.getName().substring(length);
        }
        for (int i = 0; name != null && ddProps != null && i < ddProps.length; ++i) {
            ddPropName = ddProps[i].getName();
            if (name.equalsIgnoreCase(ddPropName)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Checks whether the property is valid or not.
     */
    private boolean isValid(Method setMethod, Class<?> loadedClass) {
        Method getMethod = correspondingGetMethod( setMethod, loadedClass);
        if (getMethod == null) {
            return false;
        }
        return RARUtils.isValidRABeanConfigProperty(getMethod.getReturnType());
    }


    /**
     * Checks whether the method pertains to a valid javabean property.
     * i.e it check whether the method starts with "set" and it has only
     * one parameter. It more than one parameter is present it is taken as
     * not a property
     */
    private boolean isProperty(Method method) {
        if (method == null) {
            return false;
        }
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (methodName.startsWith("set") && parameterTypes.length == 1) {
            return true;
        }
        return false;
    }


    /**
     * Gets the property name of the method passed. It strips the first three
     * charaters (size of "set") of the method name and converts the first
     * character (for the string after stripping) to upper case and returns
     * that string.
     */
    private String getPropName(Method method) {
        if (method == null) {
            return null;
        }
        String methodName = method.getName();
        int length = "set".length();
        String retValue = methodName.substring(length, length + 1).toUpperCase(Locale.getDefault())
            + methodName.substring(length + 1);
        return retValue;
    }


    /**
     * Returns the getXXX() or isXXX() for the setXXX method passed.
     * XXX is the javabean property.
     * Check is made if there are no parameters for the getXXX() and isXXX()
     * methods. If there is any parameter, null is returned.
     */
    private Method correspondingGetMethod(Method setMethod, Class<?> loadedClass) {
        Method[] allMethods = loadedClass.getMethods();
        int length = "set".length();
        String methodName = setMethod.getName();
        Class<?>[] parameterTypes = null;
        String[] possibleGetMethodNames = new String[2];
        possibleGetMethodNames[0] = "is" + methodName.substring(length);
        possibleGetMethodNames[1] = "get" + methodName.substring(length);

        for (Method method : allMethods) {
            if (method.getName().equals(possibleGetMethodNames[0])
                || method.getName().equals(possibleGetMethodNames[1])) {
                parameterTypes = method.getParameterTypes();
                if (parameterTypes.length == 0) {
                    return method;
                }
            }
        }
        return  null;
    }


    /**
     * Invokes the method passed and returns the value obtained. If method
     * invocation fails empty string is returned. If the return type is not
     * of Wrapper class of the primitive types, empty string is returned.
     */

    private String getPropValue(Method method, Class<?> loadedClass, Object loadedInstance) {
        Object retValue = null;
        Method getMethod = correspondingGetMethod(method, loadedClass);
        if (getMethod != null) {
            try {
                retValue = getMethod.invoke(loadedInstance, (java.lang.Object[]) null);
            } catch (IllegalAccessException ie) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "rardeployment.illegalaccess_error", loadedClass.getName());
                }
            } catch (InvocationTargetException ie) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "Failed to invoke the method", loadedClass.getName());
                }
            }
        }
        return convertToString(retValue);
    }

    private String getPropType(Method method) {
        Class<?>[] parameterTypeClass = method.getParameterTypes();
        if (parameterTypeClass.length != 1) {
            return null;
        }
        if (parameterTypeClass[0].isPrimitive() || parameterTypeClass[0].getName().equals("java.lang.String")) {
            return parameterTypeClass[0].getName();
        }
        return null;
    }


    /**
     * Converts the object to String if it belongs to Wrapper class of primitive
     * type or a string itself. For all other types empty String is returned.
     */
    private String convertToString(Object obj) {
        if (obj == null) {
            return "";
        }
        if (obj instanceof String) {
            return (String) obj;
        } else if (obj instanceof Integer || obj instanceof Float || obj instanceof Long || obj instanceof Double
            || obj instanceof Character || obj instanceof Boolean || obj instanceof Byte || obj instanceof Short) {
            return String.valueOf(obj);
        } else {
            return "";
        }
    }


    /**
     * Loads and instantiates the class
     * Throws ConnectorRuntimeException if loading or instantiation fails.
     */

    private Class<?> loadClass(String className, String resourceAdapterName) throws ConnectorRuntimeException {
        Class<?> loadedClass = null;
        try {
            if (ConnectorsUtil.belongsToSystemRA(resourceAdapterName)) {
                ClassLoader classLoader = ConnectorRuntime.getRuntime().getConnectorClassLoader();
                loadedClass = classLoader.loadClass(className);
            } else {
                // try loading via ClassLoader of the RAR from ConnectorRegistry
                ConnectorApplication app = ConnectorRegistry.getInstance().getConnectorApplication(resourceAdapterName);

                if (app == null) {
                    LOG.log(Level.FINE, "unable to load class [ " + className + " ] of RAR " + "[ "
                        + resourceAdapterName + " ]" + " from server instance, trying other instances' deployments");
                    // try loading via RARUtils
                    loadedClass = RARUtils.loadClassFromRar(resourceAdapterName, className);
                } else {
                    loadedClass = app.getClassLoader().loadClass(className);
                }
            }
        } catch (ClassNotFoundException e1) {
            LOG.log(Level.FINE, "rardeployment.class_not_found", className);
            throw new ConnectorRuntimeException("Class Not Found : " + className);
        }
        return loadedClass;
    }


    /**
     * Instantiates the class
     */
    private Object instantiate(Class<?> loadedClass) throws ConnectorRuntimeException {
        try {
            return loadedClass.getDeclaredConstructor().newInstance();
        } catch (IllegalAccessException ie) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "rardeployment.illegalaccess_error", loadedClass.getName());
            }
            throw new ConnectorRuntimeException("Couldnot access class : " + loadedClass.getName());
        } catch (ReflectiveOperationException ie) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "rardeployment.class_instantiation_error", loadedClass.getName());
            }
            throw new ConnectorRuntimeException("Could not instantiate class : " + loadedClass.getName());
        }
    }
}
