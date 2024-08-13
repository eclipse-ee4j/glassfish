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
 * The security service element defines parameters and configuration information needed
 * by the core Jakarta security service. Some container-specific security configuration
 * elements are in the various container configuration elements and not here.
 * SSL configuration is also elsewhere. At this time the security service configuration
 * consists of a set of authentication realms. A number of top-level attributes
 * are defined as well.
 */
@Configured
public interface SecurityService extends ConfigBeanProxy, PropertyBag {

    /**
     * Gets the value of the {@code defaultRealm} property.
     *
     * <p>Specifies which realm (by name) is used by default when no realm is specifically requested.
     * The file realm is the common default.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "file")
    String getDefaultRealm();

    /**
     * Sets the value of the {@code defaultRealm} property.
     *
     * @param defaultRealm allowed object is {@link String}
     */
    void setDefaultRealm(String defaultRealm) throws PropertyVetoException;

    /**
     * Gets the value of the {@code defaultPrincipal} property.
     *
     * <p>Used as the identity of default security contexts when necessary
     * and no principal is provided.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    String getDefaultPrincipal();

    /**
     * Sets the value of the {@code defaultPrincipal} property.
     *
     * @param defaultPrincipal allowed object is {@link String}
     */
    void setDefaultPrincipal(String defaultPrincipal) throws PropertyVetoException;

    /**
     * Gets the value of the {@code defaultPrincipalPassword} property.
     *
     * <p>Password of default principal.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    String getDefaultPrincipalPassword();

    /**
     * Sets the value of the {@code defaultPrincipalPassword} property.
     *
     * @param defaultPrincipalPassword allowed object is {@link String}
     */
    void setDefaultPrincipalPassword(String defaultPrincipalPassword) throws PropertyVetoException;

    /**
     * Gets the value of the {@code anonymousRole} property.
     *
     * @return possible object is {@link String}
     * @deprecated This attribute is deprecated.
     */
    @Attribute(defaultValue = "AttributeDeprecated")
    String getAnonymousRole();

    /**
     * Sets the value of the {@code anonymousRole} property.
     *
     * @param anonymousRole allowed object is {@link String}
     */
    void setAnonymousRole(String anonymousRole) throws PropertyVetoException;

    /**
     * Gets the value of the {@code auditEnabled} property.
     *
     * <p>If {@code true}, additional access logging is performed to provide audit information.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "false", dataType = Boolean.class)
    String getAuditEnabled();

    /**
     * Sets the value of the {@code auditEnabled} property.
     *
     * @param auditEnabled allowed object is {@link String}
     */
    void setAuditEnabled(String auditEnabled) throws PropertyVetoException;

    /**
     * Gets the value of the {@code jacc} property.
     *
     * <p>Specifies the name of the {@code jacc-provider} element to use for setting
     * up the JACC infrastructure. The default value {@code "default"} does not need
     * to be changed unless adding a custom JACC provider.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "default")
    String getJacc();

    /**
     * Sets the value of the {@code jacc} property.
     *
     * @param jacc allowed object is {@link String}
     */
    void setJacc(String jacc) throws PropertyVetoException;

    /**
     * Gets the value of the {@code auditModules} property.
     *
     * <p>Optional list of audit provider modules which will be used by the audit subsystem.
     * Default value refers to the internal log-based audit module.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "default")
    String getAuditModules();

    /**
     * Sets the value of the {@code auditModules} property.
     *
     * @param auditModules allowed object is {@link String}
     */
    void setAuditModules(String auditModules) throws PropertyVetoException;

    /**
     * Gets the value of the {@code activateDefaultPrincipalToRoleMapping} property.
     *
     * <p>Causes the appserver to apply a default principal to role mapping, to any
     * application that does not have an application specific mapping defined. Every role
     * is mapped to a same-named (as the role) instance of a {@link java.security.Principal}
     * implementation class (see mapped-principal-class). This behavior is similar to that
     * of Tomcat servlet container. It is off by default.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "true", dataType = Boolean.class)
    String getActivateDefaultPrincipalToRoleMapping();

    /**
     * Sets the value of the {@code activateDefaultPrincipalToRoleMapping} property.
     *
     * @param principalToRoleMapping allowed object is {@link String}
     */
    void setActivateDefaultPrincipalToRoleMapping(String principalToRoleMapping) throws PropertyVetoException;

    /**
     * Customizes the {@link java.security.Principal} implementation class used when
     * {@code activate-default-principal-to-role-mapping} is set to {@code true}.
     * Should the default be set to {@code com.sun.enterprise.deployment.Group}?
     *
     * <p>This attribute is used to customize the {@link java.security.Principal}
     * implementation class used in the default principal to role mapping.
     *
     * <p>This attribute is optional. When it is not specified,
     * {@code com.sun.enterprise.deployment.Group} implementation of
     * {@link java.security.Principal} is used. The value of this attribute is only
     * relevant when the {@code activate-default principal-to-role-mapping} attribute
     * is set to {@code true}.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    String getMappedPrincipalClass();

    /**
     * Sets the value of the {@code mappedPrincipalClass} property.
     *
     * @param mappedPrincipalClass allowed object is {@link String}
     */
    void setMappedPrincipalClass(String mappedPrincipalClass) throws PropertyVetoException;

    /**
     * Gets the value of the {@code authRealm} property.
     *
     * <p>This accessor method returns a reference to the live list, not a snapshot.
     * Therefore any modification you make to the returned list will be present inside
     * the JAXB object. This is why there is not a {@code set} method for the
     * {@code authRealm} property.
     *
     * <p>For example, to add a new item, do as follows:
     *
     * <pre>
     * getAuthRealm().add(newItem);
     * </pre>
     *
     * <p>Objects of the following type(s) are allowed in the list {@link AuthRealm}
     */
    @Element(required = true)
    List<AuthRealm> getAuthRealm();

    /**
     * Gets the value of the {@code jaccProvider} property.
     *
     * <p>This accessor method returns a reference to the live list, not a snapshot.
     * Therefore any modification you make to the returned list will be present inside
     * the JAXB object. This is why there is not a {@code set} method for the
     * {@code jaccProvider} property.
     *
     * <p>For example, to add a new item, do as follows:
     *
     * <pre>
     * getJaccProvider().add(newItem);
     * </pre>
     *
     * <p>Objects of the following type(s) are allowed in the list {@link JaccProvider}
     */
    @Element(required = true)
    List<JaccProvider> getJaccProvider();

    /**
     * Gets the value of the {@code auditModule} property.
     *
     * <p>This accessor method returns a reference to the live list, not a snapshot.
     * Therefore any modification you make to the returned list will be present inside
     * the JAXB object. This is why there is not a {@code set} method for the
     * {@code auditModule} property.
     *
     * <p>For example, to add a new item, do as follows:
     *
     * <pre>
     * getAuditModule().add(newItem);
     * </pre>
     *
     * <p>Objects of the following type(s) are allowed in the list {@link AuditModule}
     */
    @Element
    List<AuditModule> getAuditModule();

    /**
     * Gets the value of the {@code messageSecurityConfig} property.
     *
     * <p>Optional list of layer specific lists of configured message security providers.
     *
     * <p>This accessor method returns a reference to the live list, not a snapshot.
     * Therefore any modification you make to the returned list will be present inside
     * the JAXB object. This is why there is not a {@code set} method for the
     * {@code messageSecurityConfig} property.
     *
     * <p>For example, to add a new item, do as follows:
     *
     * <pre>
     * getMessageSecurityConfig().add(newItem);
     * </pre>
     *
     * <p>>Objects of the following type(s) are allowed in the list {@link MessageSecurityConfig}
     */
    @Element
    List<MessageSecurityConfig> getMessageSecurityConfig();

    /**
     * Properties as per {@link PropertyBag}
     */
    @ToDo(priority = ToDo.Priority.IMPORTANT, details = "Provide PropertyDesc for legal props")
    @PropertiesDesc(props = {})
    @Element
    List<Property> getProperty();
}
