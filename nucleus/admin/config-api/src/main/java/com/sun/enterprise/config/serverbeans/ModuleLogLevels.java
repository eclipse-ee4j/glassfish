/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

import java.beans.PropertyVetoException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import org.glassfish.api.admin.config.PropertiesDesc;
import org.glassfish.quality.ToDo;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;

/**
 *
 */
@Configured
public interface ModuleLogLevels extends ConfigBeanProxy, PropertyBag {

    /**
     * Gets the value of the {@code root} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "INFO")
    String getRoot();

    /**
     * Sets the value of the {@code root} property.
     *
     * @param root allowed object is {@link String}
     */
    void setRoot(String root) throws PropertyVetoException;

    /**
     * Gets the value of the {@code server} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "INFO")
    String getServer();

    /**
     * Sets the value of the {@code server} property.
     *
     * @param server allowed object is {@link String}
     */
    void setServer(String server) throws PropertyVetoException;

    /**
     * Gets the value of the {@code ejbContainer} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "INFO")
    String getEjbContainer();

    /**
     * Sets the value of the {@code ejbContainer} property.
     *
     * @param ejbContainer allowed object is {@link String}
     */
    void setEjbContainer(String ejbContainer) throws PropertyVetoException;

    /**
     * Gets the value of the {@code cmpContainer} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "INFO")
    String getCmpContainer();

    /**
     * Sets the value of the {@code cmpContainer} property.
     *
     * @param cmpContainer allowed object is {@link String}
     */
    void setCmpContainer(String cmpContainer) throws PropertyVetoException;

    /**
     * Gets the value of the {@code mdbContainer} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "INFO")
    String getMdbContainer();

    /**
     * Sets the value of the {@code mdbContainer} property.
     *
     * @param mdbContainer allowed object is {@link String}
     */
    void setMdbContainer(String mdbContainer) throws PropertyVetoException;

    /**
     * Gets the value of the {@code webContainer} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "INFO")
    String getWebContainer();

    /**
     * Sets the value of the {@code webContainer} property.
     *
     * @param webContainer allowed object is {@link String}
     */
    void setWebContainer(String webContainer) throws PropertyVetoException;

    /**
     * Gets the value of the {@code classloader} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "INFO")
    String getClassloader();

    /**
     * Sets the value of the {@code classloader} property.
     *
     * @param classloader allowed object is {@link String}
     */
    void setClassloader(String classloader) throws PropertyVetoException;

    /**
     * Gets the value of the {@code configuration} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "INFO")
    String getConfiguration();

    /**
     * Sets the value of the {@code configuration} property.
     *
     * @param configuration allowed object is {@link String}
     */
    void setConfiguration(String configuration) throws PropertyVetoException;

    /**
     * Gets the value of the {@code naming} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "INFO")
    String getNaming();

    /**
     * Sets the value of the {@code naming} property.
     *
     * @param naming allowed object is {@link String}
     */
    void setNaming(String naming) throws PropertyVetoException;

    /**
     * Gets the value of the {@code security} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "INFO")
    String getSecurity();

    /**
     * Sets the value of the {@code security} property.
     *
     * @param security allowed object is {@link String}
     */
    void setSecurity(String security) throws PropertyVetoException;

    /**
     * Gets the value of the {@code jts} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "INFO")
    String getJts();

    /**
     * Sets the value of the {@code jts} property.
     *
     * @param jts allowed object is {@link String}
     */
    void setJts(String jts) throws PropertyVetoException;

    /**
     * Gets the value of the {@code jta} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "INFO")
    String getJta();

    /**
     * Sets the value of the {@code jta} property.
     *
     * @param jta allowed object is {@link String}
     */
    void setJta(String jta) throws PropertyVetoException;

    /**
     * Gets the value of the {@code admin} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "INFO")
    String getAdmin();

    /**
     * Sets the value of the {@code admin} property.
     *
     * @param admin allowed object is {@link String}
     */
    void setAdmin(String admin) throws PropertyVetoException;

    /**
     * Gets the value of the {@code deployment} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "INFO")
    String getDeployment();

    /**
     * Sets the value of the {@code deployment} property.
     *
     * @param deployment allowed object is {@link String}
     */
    void setDeployment(String deployment) throws PropertyVetoException;

    /**
     * Gets the value of the {@code verifier} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "INFO")
    String getVerifier();

    /**
     * Sets the value of the {@code verifier} property.
     *
     * @param verifier allowed object is {@link String}
     */
    void setVerifier(String verifier) throws PropertyVetoException;

    /**
     * Gets the value of the {@code jaxr} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "INFO")
    String getJaxr();

    /**
     * Sets the value of the {@code jaxr} property.
     *
     * @param jaxr allowed object is {@link String}
     */
    void setJaxr(String jaxr) throws PropertyVetoException;

    /**
     * Gets the value of the {@code jaxrpc} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "INFO")
    String getJaxrpc();

    /**
     * Sets the value of the {@code jaxrpc} property.
     *
     * @param jaxrpc allowed object is {@link String}
     */
    void setJaxrpc(String jaxrpc) throws PropertyVetoException;

    /**
     * Gets the value of the {@code saaj} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "INFO")
    String getSaaj();

    /**
     * Sets the value of the {@code saaj} property.
     *
     * @param saaj allowed object is {@link String}
     */
    void setSaaj(String saaj) throws PropertyVetoException;

    /**
     * Gets the value of the {@code corba} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "INFO")
    String getCorba();

    /**
     * Sets the value of the {@code corba} property.
     *
     * @param corba allowed object is {@link String}
     */
    void setCorba(String corba) throws PropertyVetoException;

    /**
     * Gets the value of the {@code javamail} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "INFO")
    String getJavamail();

    /**
     * Sets the value of the {@code javamail} property.
     *
     * @param javamail allowed object is {@link String}
     */
    void setJavamail(String javamail) throws PropertyVetoException;

    /**
     * Gets the value of the {@code jms} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "INFO")
    String getJms();

    /**
     * Sets the value of the {@code jms} property.
     *
     * @param jms allowed object is {@link String}
     */
    void setJms(String jms) throws PropertyVetoException;

    /**
     * Gets the value of the {@code connector} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "INFO")
    String getConnector();

    /**
     * Sets the value of the {@code connector} property.
     *
     * @param connector allowed object is {@link String}
     */
    void setConnector(String connector) throws PropertyVetoException;

    /**
     * Gets the value of the {@code jdo} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "INFO")
    String getJdo();

    /**
     * Sets the value of the {@code jdo} property.
     *
     * @param jdo allowed object is {@link String}
     */
    void setJdo(String jdo) throws PropertyVetoException;

    /**
     * Gets the value of the {@code cmp} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "INFO")
    String getCmp();

    /**
     * Sets the value of the {@code cmp} property.
     *
     * @param cmp allowed object is {@link String}
     */
    void setCmp(String cmp) throws PropertyVetoException;

    /**
     * Gets the value of the {@code util} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "INFO")
    String getUtil();

    /**
     * Sets the value of the {@code util} property.
     *
     * @param util allowed object is {@link String}
     */
    void setUtil(String util) throws PropertyVetoException;

    /**
     * Gets the value of the {@code resourceAdapter} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "INFO")
    String getResourceAdapter();

    /**
     * Sets the value of the {@code resourceAdapter} property.
     *
     * @param resourceAdapter allowed object is {@link String}
     */
    void setResourceAdapter(String resourceAdapter) throws PropertyVetoException;

    /**
     * Gets the value of the {@code synchronization} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "INFO")
    String getSynchronization();

    /**
     * Sets the value of the {@code synchronization} property.
     *
     * @param synchronization allowed object is {@link String}
     */
    void setSynchronization(String synchronization) throws PropertyVetoException;

    /**
     * Gets the value of the {@code nodeAgent} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "INFO")
    String getNodeAgent();

    /**
     * Sets the value of the {@code nodeAgent} property.
     *
     * @param nodeAgent allowed object is {@link String}
     */
    void setNodeAgent(String nodeAgent) throws PropertyVetoException;

    /**
     * Gets the value of the {@code selfManagement} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "INFO")
    String getSelfManagement();

    /**
     * Sets the value of the {@code selfManagement} property.
     *
     * @param selfManagement allowed object is {@link String}
     */
    void setSelfManagement(String selfManagement) throws PropertyVetoException;

    /**
     * Gets the value of the {@code groupManagementService} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "INFO")
    String getGroupManagementService();

    /**
     * Sets the value of the {@code groupManagementService} property.
     *
     * @param groupManagementService allowed object is {@link String}
     */
    void setGroupManagementService(String groupManagementService) throws PropertyVetoException;

    /**
     * Gets the value of the {@code managementEvent} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "INFO")
    String getManagementEvent();

    /**
     * Sets the value of the {@code managementEvent} property.
     *
     * @param managementEvent allowed object is {@link String}
     */
    void setManagementEvent(String managementEvent) throws PropertyVetoException;

    /*
     * Get all the log levels for all the modules.
     */
    default Map<String, String> getAllLogLevels() {
        Map<String, String> moduleLevels = new HashMap<>();

        moduleLevels.put("root", getRoot());
        moduleLevels.put("server", getServer());
        moduleLevels.put("ejb-container", getEjbContainer());
        moduleLevels.put("web-container", getWebContainer());
        moduleLevels.put("cmp-container", getCmpContainer());
        moduleLevels.put("mdb-container", getMdbContainer());
        moduleLevels.put("classloader", getClassloader());
        moduleLevels.put("configuration", getConfiguration());
        moduleLevels.put("naming", getNaming());
        moduleLevels.put("security", getSecurity());
        moduleLevels.put("jts", getJts());
        moduleLevels.put("jta", getJta());
        moduleLevels.put("admin", getAdmin());
        moduleLevels.put("deployment", getDeployment());
        moduleLevels.put("verifier", getVerifier());
        moduleLevels.put("jaxr", getJaxr());
        moduleLevels.put("jaxrpc", getJaxrpc());
        moduleLevels.put("saaj", getSaaj());
        moduleLevels.put("corba", getCorba());
        moduleLevels.put("javamail", getJavamail());
        moduleLevels.put("jms", getJms());
        moduleLevels.put("connector", getConnector());
        moduleLevels.put("jdo", getJdo());
        moduleLevels.put("cmp", getCmp());
        moduleLevels.put("util", getUtil());
        moduleLevels.put("resource-adapter", getResourceAdapter());
        moduleLevels.put("synchronization", getSynchronization());
        moduleLevels.put("node-agent", getNodeAgent());
        moduleLevels.put("self-management", getSelfManagement());
        moduleLevels.put("group-management-services", getGroupManagementService());
        moduleLevels.put("management-event", getManagementEvent());

        return moduleLevels;
    }

    /**
     * Properties as per {@link PropertyBag}
     */
    @ToDo(priority = ToDo.Priority.IMPORTANT, details = "Provide PropertyDesc for legal props")
    @PropertiesDesc(props = {})
    @Element
    List<Property> getProperty();
}
