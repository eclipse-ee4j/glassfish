/*
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

package org.glassfish.resources.mail.admin.cli;

import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.util.LocalStringManagerImpl;
import org.glassfish.api.I18n;
import org.glassfish.resourcebase.resources.api.ResourceStatus;
import org.glassfish.resourcebase.resources.util.BindableResourcesHelper;
import org.glassfish.resources.mail.config.MailResource;
import org.glassfish.resourcebase.resources.util.ResourceUtil;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;

import javax.inject.Inject;
import javax.resource.ResourceException;
import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.Properties;

import static org.glassfish.resources.admin.cli.ResourceConstants.*;


@Service(name = ServerTags.MAIL_RESOURCE)
@I18n("add.resources")
public class MailResourceManager implements org.glassfish.resources.admin.cli.ResourceManager {

    final private static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(MailResourceManager.class);
    private static final String DESCRIPTION = ServerTags.DESCRIPTION;

    private String mailHost = null;
    private String mailUser = null;
    private String fromAddress = null;
    private String jndiName = null;
    private String storeProtocol = null;
    private String storeProtocolClass = null;
    private String transportProtocol = null;
    private String transportProtocolClass = null;
    private String enabled = null;
    private String enabledValueForTarget = null;
    private String debug = null;
    private String description = null;

    @Inject
    private org.glassfish.resourcebase.resources.admin.cli.ResourceUtil resourceUtil;

    @Inject
    private BindableResourcesHelper resourcesHelper;

    public String getResourceType() {
        return ServerTags.MAIL_RESOURCE;
    }

    public ResourceStatus create(Resources resources, HashMap attributes, final Properties properties,
                                 String target) throws Exception {
        setAttributes(attributes, target);

        ResourceStatus validationStatus = isValid(resources, true, target);
        if(validationStatus.getStatus() == ResourceStatus.FAILURE){
            return validationStatus;
        }

        // ensure we don't already have one of this name
        if (ResourceUtil.getBindableResourceByName(resources, jndiName) != null) {
            String msg = localStrings.getLocalString(
                    "create.mail.resource.duplicate.1",
                    "A Mail Resource named {0} already exists.",
                    jndiName);
            return new ResourceStatus(ResourceStatus.FAILURE, msg, true);
        }

        try {
            ConfigSupport.apply(new SingleConfigCode<Resources>() {

                public Object run(Resources param) throws PropertyVetoException,
                        TransactionFailure {
                    MailResource newResource = createConfigBean(param, properties);
                    param.getResources().add(newResource);
                    return newResource;
                }
            }, resources);

            resourceUtil.createResourceRef(jndiName, enabledValueForTarget, target);

            String msg = localStrings.getLocalString(
                    "create.mail.resource.success",
                    "Mail Resource {0} created.", jndiName);
            return new ResourceStatus(ResourceStatus.SUCCESS, msg, true);
        } catch (TransactionFailure tfe) {
            String msg = localStrings.getLocalString("" +
                    "create.mail.resource.fail",
                    "Unable to create Mail Resource {0}.", jndiName) +
                    " " + tfe.getLocalizedMessage();
            return new ResourceStatus(ResourceStatus.FAILURE, msg, true);
        }
    }

    private ResourceStatus isValid(Resources resources, boolean validateResourceRef, String target){
        ResourceStatus status ;
        if (mailHost == null) {
            String msg = localStrings.getLocalString("create.mail.resource.noHostName",
                    "No host name defined for Mail Resource.");
            return new ResourceStatus(ResourceStatus.FAILURE, msg, true);
        }

        if (mailUser == null) {
            String msg = localStrings.getLocalString("create.mail.resource.noUserName",
                    "No user name defined for Mail Resource.");
            return new ResourceStatus(ResourceStatus.FAILURE, msg, true);
        }

        if (fromAddress == null) {
            String msg = localStrings.getLocalString("create.mail.resource.noFrom",
                    "From not defined for Mail Resource.");
            return new ResourceStatus(ResourceStatus.FAILURE, msg, true);
        }


        status = resourcesHelper.validateBindableResourceForDuplicates(resources, jndiName, validateResourceRef,
                target, MailResource.class);
        if(status.getStatus() == ResourceStatus.FAILURE){
            return status;
        }

        return status;
    }

    private MailResource createConfigBean(Resources param, Properties props) throws PropertyVetoException,
            TransactionFailure {
        MailResource newResource = param.createChild(MailResource.class);
        newResource.setJndiName(jndiName);
        newResource.setFrom(fromAddress);
        newResource.setUser(mailUser);
        newResource.setHost(mailHost);
        newResource.setEnabled(enabled);
        newResource.setStoreProtocol(storeProtocol);
        newResource.setStoreProtocolClass(storeProtocolClass);
        newResource.setTransportProtocol(transportProtocol);
        newResource.setTransportProtocolClass(
                transportProtocolClass);
        newResource.setDebug(debug);
        if (description != null) {
            newResource.setDescription(description);
        }
        if (props != null) {
            for (java.util.Map.Entry e : props.entrySet()) {
                Property prop = newResource.createChild(Property.class);
                prop.setName((String) e.getKey());
                prop.setValue((String) e.getValue());
                newResource.getProperty().add(prop);
            }
        }
        return newResource;
    }

    private void setAttributes(HashMap attributes, String target) {
        jndiName = (String) attributes.get(JNDI_NAME);
        mailHost = (String) attributes.get(MAIL_HOST);
        mailUser = (String) attributes.get(MAIL_USER);
        fromAddress = (String) attributes.get(MAIL_FROM_ADDRESS);
        storeProtocol = (String) attributes.get(MAIL_STORE_PROTO);
        storeProtocolClass = (String) attributes.get(MAIL_STORE_PROTO_CLASS);
        transportProtocol = (String) attributes.get(MAIL_TRANS_PROTO);
        transportProtocolClass = (String) attributes.get(MAIL_TRANS_PROTO_CLASS);
        debug = (String) attributes.get(MAIL_DEBUG);
        if(target != null){
            enabled = resourceUtil.computeEnabledValueForResourceBasedOnTarget((String)attributes.get(ENABLED), target);
        }else{
            enabled = (String) attributes.get(ENABLED);
        }
        enabledValueForTarget = (String) attributes.get(ENABLED);
        description = (String) attributes.get(DESCRIPTION);
    }

    public Resource createConfigBean(Resources resources, HashMap attributes, Properties properties, boolean validate)
            throws Exception {
        setAttributes(attributes, null);
        ResourceStatus status = null;
        if(!validate){
            status = new ResourceStatus(ResourceStatus.SUCCESS,"");
        }else{
            status = isValid(resources, false, null);
        }
        if(status.getStatus() == ResourceStatus.SUCCESS){
            return createConfigBean(resources, properties);
        }else{
            throw new ResourceException(status.getMessage());
        }
    }
}
