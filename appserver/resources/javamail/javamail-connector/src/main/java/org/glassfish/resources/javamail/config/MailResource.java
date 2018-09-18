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

package org.glassfish.resources.javamail.config;

import com.sun.enterprise.config.serverbeans.BindableResource;
import com.sun.enterprise.config.serverbeans.Resource;
import org.glassfish.admin.cli.resources.ResourceConfigCreator;
import org.glassfish.admin.cli.resources.UniqueResourceNameConstraint;
import org.glassfish.resourcebase.resources.ResourceDeploymentOrder;
import org.glassfish.resourcebase.resources.ResourceTypeOrder;
import org.jvnet.hk2.config.*;

import java.beans.PropertyVetoException;
import java.util.List;


import org.glassfish.api.admin.config.PropertiesDesc;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;
import org.glassfish.api.admin.RestRedirects;
import org.glassfish.api.admin.RestRedirect;

import org.glassfish.quality.ToDo;

import javax.validation.constraints.NotNull;

import com.sun.enterprise.config.serverbeans.customvalidators.JavaClassName;

/* @XmlType(name = "", propOrder = {
    "description",
    "property"
}) */

@Configured
@ResourceConfigCreator(commandName="create-javamail-resource")
@RestRedirects({
 @RestRedirect(opType = RestRedirect.OpType.POST, commandName = "create-javamail-resource"),
 @RestRedirect(opType = RestRedirect.OpType.DELETE, commandName = "delete-javamail-resource")
})
@ResourceTypeOrder(deploymentOrder= ResourceDeploymentOrder.MAIL_RESOURCE)
@UniqueResourceNameConstraint(message="{resourcename.isnot.unique}", payload=MailResource.class)
/**
 * The mail-resource element describes a javax.mail.Session resource 
 */
public interface MailResource extends ConfigBeanProxy, Resource, PropertyBag, BindableResource {


    /**
     * Gets the value of the storeProtocol property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="imap")
    public String getStoreProtocol();

    /**
     * Sets the value of the storeProtocol property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setStoreProtocol(String value) throws PropertyVetoException;

    /**
     * Gets the value of the storeProtocolClass property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="com.sun.mail.imap.IMAPStore")
    @JavaClassName
    public String getStoreProtocolClass();

    /**
     * Sets the value of the storeProtocolClass property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setStoreProtocolClass(String value) throws PropertyVetoException;

    /**
     * Gets the value of the transportProtocol property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="smtp")
    public String getTransportProtocol();

    /**
     * Sets the value of the transportProtocol property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTransportProtocol(String value) throws PropertyVetoException;

    /**
     * Gets the value of the transportProtocolClass property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="com.sun.mail.smtp.SMTPTransport")
    @JavaClassName
    public String getTransportProtocolClass();

    /**
     * Sets the value of the transportProtocolClass property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTransportProtocolClass(String value) throws PropertyVetoException;

    /**
     * Gets the value of the host property.
     *
     * ip V6 or V4 address or hostname
     * 
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    @NotNull
    public String getHost();

    /**
     * Sets the value of the host property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setHost(String value) throws PropertyVetoException;

    /**
     * Gets the value of the user property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    @NotNull
    public String getUser();

    /**
     * Sets the value of the user property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setUser(String value) throws PropertyVetoException;

    /**
     * Gets the value of the from property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    @NotNull
    public String getFrom();

    /**
     * Sets the value of the from property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setFrom(String value) throws PropertyVetoException;

    /**
     * Gets the value of the debug property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="false",dataType=Boolean.class)
    public String getDebug();

    /**
     * Sets the value of the debug property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDebug(String value) throws PropertyVetoException;

    /**
     * Gets the value of the enabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="true",dataType=Boolean.class)
    public String getEnabled();

    /**
     * Sets the value of the enabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setEnabled(String value) throws PropertyVetoException;

    /**
     * Gets the value of the description property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getDescription();

    /**
     * Sets the value of the description property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDescription(String value) throws PropertyVetoException;
    
    /**
    	Properties as per {@link PropertyBag}
     */
    @ToDo(priority=ToDo.Priority.IMPORTANT, details="Provide PropertyDesc for legal props" )
    @PropertiesDesc(props={})
    @Element
    List<Property> getProperty();

    @DuckTyped
    String getIdentity();

    class Duck {
        public static String getIdentity(MailResource resource){
            return resource.getJndiName();
        }
    }
}
