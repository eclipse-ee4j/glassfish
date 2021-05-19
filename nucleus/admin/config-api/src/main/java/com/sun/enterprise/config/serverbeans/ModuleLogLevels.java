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

package com.sun.enterprise.config.serverbeans;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.DuckTyped;

import java.beans.PropertyVetoException;
import java.util.*;

import org.glassfish.api.admin.config.PropertiesDesc;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;

import org.glassfish.quality.ToDo;

/**
 *
 */

/* @XmlType(name = "", propOrder = {
    "property"
}) */

@Configured
public interface ModuleLogLevels extends ConfigBeanProxy, PropertyBag {

    /**
     * Gets the value of the root property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "INFO")
    public String getRoot();

    /**
     * Sets the value of the root property.
     *
     * @param value allowed object is {@link String }
     */
    public void setRoot(String value) throws PropertyVetoException;

    /**
     * Gets the value of the server property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "INFO")
    public String getServer();

    /**
     * Sets the value of the server property.
     *
     * @param value allowed object is {@link String }
     */
    public void setServer(String value) throws PropertyVetoException;

    /**
     * Gets the value of the ejbContainer property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "INFO")
    public String getEjbContainer();

    /**
     * Sets the value of the ejbContainer property.
     *
     * @param value allowed object is {@link String }
     */
    public void setEjbContainer(String value) throws PropertyVetoException;

    /**
     * Gets the value of the cmpContainer property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "INFO")
    public String getCmpContainer();

    /**
     * Sets the value of the cmpContainer property.
     *
     * @param value allowed object is {@link String }
     */
    public void setCmpContainer(String value) throws PropertyVetoException;

    /**
     * Gets the value of the mdbContainer property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "INFO")
    public String getMdbContainer();

    /**
     * Sets the value of the mdbContainer property.
     *
     * @param value allowed object is {@link String }
     */
    public void setMdbContainer(String value) throws PropertyVetoException;

    /**
     * Gets the value of the webContainer property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "INFO")
    public String getWebContainer();

    /**
     * Sets the value of the webContainer property.
     *
     * @param value allowed object is {@link String }
     */
    public void setWebContainer(String value) throws PropertyVetoException;

    /**
     * Gets the value of the classloader property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "INFO")
    public String getClassloader();

    /**
     * Sets the value of the classloader property.
     *
     * @param value allowed object is {@link String }
     */
    public void setClassloader(String value) throws PropertyVetoException;

    /**
     * Gets the value of the configuration property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "INFO")
    public String getConfiguration();

    /**
     * Sets the value of the configuration property.
     *
     * @param value allowed object is {@link String }
     */
    public void setConfiguration(String value) throws PropertyVetoException;

    /**
     * Gets the value of the naming property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "INFO")
    public String getNaming();

    /**
     * Sets the value of the naming property.
     *
     * @param value allowed object is {@link String }
     */
    public void setNaming(String value) throws PropertyVetoException;

    /**
     * Gets the value of the security property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "INFO")
    public String getSecurity();

    /**
     * Sets the value of the security property.
     *
     * @param value allowed object is {@link String }
     */
    public void setSecurity(String value) throws PropertyVetoException;

    /**
     * Gets the value of the jts property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "INFO")
    public String getJts();

    /**
     * Sets the value of the jts property.
     *
     * @param value allowed object is {@link String }
     */
    public void setJts(String value) throws PropertyVetoException;

    /**
     * Gets the value of the jta property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "INFO")
    public String getJta();

    /**
     * Sets the value of the jta property.
     *
     * @param value allowed object is {@link String }
     */
    public void setJta(String value) throws PropertyVetoException;

    /**
     * Gets the value of the admin property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "INFO")
    public String getAdmin();

    /**
     * Sets the value of the admin property.
     *
     * @param value allowed object is {@link String }
     */
    public void setAdmin(String value) throws PropertyVetoException;

    /**
     * Gets the value of the deployment property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "INFO")
    public String getDeployment();

    /**
     * Sets the value of the deployment property.
     *
     * @param value allowed object is {@link String }
     */
    public void setDeployment(String value) throws PropertyVetoException;

    /**
     * Gets the value of the verifier property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "INFO")
    public String getVerifier();

    /**
     * Sets the value of the verifier property.
     *
     * @param value allowed object is {@link String }
     */
    public void setVerifier(String value) throws PropertyVetoException;

    /**
     * Gets the value of the jaxr property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "INFO")
    public String getJaxr();

    /**
     * Sets the value of the jaxr property.
     *
     * @param value allowed object is {@link String }
     */
    public void setJaxr(String value) throws PropertyVetoException;

    /**
     * Gets the value of the jaxrpc property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "INFO")
    public String getJaxrpc();

    /**
     * Sets the value of the jaxrpc property.
     *
     * @param value allowed object is {@link String }
     */
    public void setJaxrpc(String value) throws PropertyVetoException;

    /**
     * Gets the value of the saaj property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "INFO")
    public String getSaaj();

    /**
     * Sets the value of the saaj property.
     *
     * @param value allowed object is {@link String }
     */
    public void setSaaj(String value) throws PropertyVetoException;

    /**
     * Gets the value of the corba property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "INFO")
    public String getCorba();

    /**
     * Sets the value of the corba property.
     *
     * @param value allowed object is {@link String }
     */
    public void setCorba(String value) throws PropertyVetoException;

    /**
     * Gets the value of the javamail property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "INFO")
    public String getJavamail();

    /**
     * Sets the value of the javamail property.
     *
     * @param value allowed object is {@link String }
     */
    public void setJavamail(String value) throws PropertyVetoException;

    /**
     * Gets the value of the jms property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "INFO")
    public String getJms();

    /**
     * Sets the value of the jms property.
     *
     * @param value allowed object is {@link String }
     */
    public void setJms(String value) throws PropertyVetoException;

    /**
     * Gets the value of the connector property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "INFO")
    public String getConnector();

    /**
     * Sets the value of the connector property.
     *
     * @param value allowed object is {@link String }
     */
    public void setConnector(String value) throws PropertyVetoException;

    /**
     * Gets the value of the jdo property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "INFO")
    public String getJdo();

    /**
     * Sets the value of the jdo property.
     *
     * @param value allowed object is {@link String }
     */
    public void setJdo(String value) throws PropertyVetoException;

    /**
     * Gets the value of the cmp property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "INFO")
    public String getCmp();

    /**
     * Sets the value of the cmp property.
     *
     * @param value allowed object is {@link String }
     */
    public void setCmp(String value) throws PropertyVetoException;

    /**
     * Gets the value of the util property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "INFO")
    public String getUtil();

    /**
     * Sets the value of the util property.
     *
     * @param value allowed object is {@link String }
     */
    public void setUtil(String value) throws PropertyVetoException;

    /**
     * Gets the value of the resourceAdapter property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "INFO")
    public String getResourceAdapter();

    /**
     * Sets the value of the resourceAdapter property.
     *
     * @param value allowed object is {@link String }
     */
    public void setResourceAdapter(String value) throws PropertyVetoException;

    /**
     * Gets the value of the synchronization property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "INFO")
    public String getSynchronization();

    /**
     * Sets the value of the synchronization property.
     *
     * @param value allowed object is {@link String }
     */
    public void setSynchronization(String value) throws PropertyVetoException;

    /**
     * Gets the value of the nodeAgent property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "INFO")
    public String getNodeAgent();

    /**
     * Sets the value of the nodeAgent property.
     *
     * @param value allowed object is {@link String }
     */
    public void setNodeAgent(String value) throws PropertyVetoException;

    /**
     * Gets the value of the selfManagement property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "INFO")
    public String getSelfManagement();

    /**
     * Sets the value of the selfManagement property.
     *
     * @param value allowed object is {@link String }
     */
    public void setSelfManagement(String value) throws PropertyVetoException;

    /**
     * Gets the value of the groupManagementService property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "INFO")
    public String getGroupManagementService();

    /**
     * Sets the value of the groupManagementService property.
     *
     * @param value allowed object is {@link String }
     */
    public void setGroupManagementService(String value) throws PropertyVetoException;

    /**
     * Gets the value of the managementEvent property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "INFO")
    public String getManagementEvent();

    /**
     * Sets the value of the managementEvent property.
     *
     * @param value allowed object is {@link String }
     */
    public void setManagementEvent(String value) throws PropertyVetoException;

    /*
     * Get all the log levels for all the modules.
     */
    @DuckTyped
    public Map<String, String> getAllLogLevels();

    public class Duck {
        public static Map<String, String> getAllLogLevels(ModuleLogLevels me) {

            Map<String, String> moduleLevels = new HashMap<String, String>();
            moduleLevels.put("root", me.getRoot());
            moduleLevels.put("server", me.getServer());
            moduleLevels.put("ejb-container", me.getEjbContainer());
            moduleLevels.put("web-container", me.getWebContainer());
            moduleLevels.put("cmp-container", me.getCmpContainer());
            moduleLevels.put("mdb-container", me.getMdbContainer());
            moduleLevels.put("classloader", me.getClassloader());
            moduleLevels.put("configuration", me.getConfiguration());
            moduleLevels.put("naming", me.getNaming());
            moduleLevels.put("security", me.getSecurity());
            moduleLevels.put("jts", me.getJts());
            moduleLevels.put("jta", me.getJta());
            moduleLevels.put("admin", me.getAdmin());
            moduleLevels.put("deployment", me.getDeployment());
            moduleLevels.put("verifier", me.getVerifier());
            moduleLevels.put("jaxr", me.getJaxr());
            moduleLevels.put("jaxrpc", me.getJaxrpc());
            moduleLevels.put("saaj", me.getSaaj());
            moduleLevels.put("corba", me.getCorba());
            moduleLevels.put("javamail", me.getJavamail());
            moduleLevels.put("jms", me.getJms());
            moduleLevels.put("connector", me.getConnector());
            moduleLevels.put("jdo", me.getJdo());
            moduleLevels.put("cmp", me.getCmp());
            moduleLevels.put("util", me.getUtil());
            moduleLevels.put("resource-adapter", me.getResourceAdapter());
            moduleLevels.put("synchronization", me.getSynchronization());
            moduleLevels.put("node-agent", me.getNodeAgent());
            moduleLevels.put("self-management", me.getSelfManagement());
            moduleLevels.put("group-management-services", me.getGroupManagementService());
            moduleLevels.put("management-event", me.getManagementEvent());

            return moduleLevels;
        }
    }

    /**
     * Properties as per {@link PropertyBag}
     */
    @ToDo(priority = ToDo.Priority.IMPORTANT, details = "Provide PropertyDesc for legal props")
    @PropertiesDesc(props = {})
    @Element
    List<Property> getProperty();
}
