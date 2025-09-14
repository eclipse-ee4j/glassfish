/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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

import jakarta.inject.Inject;
import jakarta.resource.ResourceException;

import java.beans.PropertyVetoException;
import java.util.Map.Entry;
import java.util.Properties;

import org.glassfish.api.I18n;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.resourcebase.resources.api.ResourceStatus;
import org.glassfish.resourcebase.resources.util.BindableResourcesHelper;
import org.glassfish.resourcebase.resources.util.ResourceUtil;
import org.glassfish.resources.api.ResourceAttributes;
import org.glassfish.resources.mail.config.MailResource;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;

import static com.sun.enterprise.config.serverbeans.ServerTags.DESCRIPTION;
import static org.glassfish.resources.admin.cli.ResourceConstants.ENABLED;
import static org.glassfish.resources.admin.cli.ResourceConstants.JNDI_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.MAIL_DEBUG;
import static org.glassfish.resources.admin.cli.ResourceConstants.MAIL_FROM_ADDRESS;
import static org.glassfish.resources.admin.cli.ResourceConstants.MAIL_HOST;
import static org.glassfish.resources.admin.cli.ResourceConstants.MAIL_STORE_PROTO;
import static org.glassfish.resources.admin.cli.ResourceConstants.MAIL_STORE_PROTO_CLASS;
import static org.glassfish.resources.admin.cli.ResourceConstants.MAIL_TRANS_PROTO;
import static org.glassfish.resources.admin.cli.ResourceConstants.MAIL_TRANS_PROTO_CLASS;
import static org.glassfish.resources.admin.cli.ResourceConstants.MAIL_USER;


@Service(name = ServerTags.MAIL_RESOURCE)
@I18n("add.resources")
public class MailResourceManager implements org.glassfish.resources.admin.cli.ResourceManager {

    private static final LocalStringManagerImpl I18N = new LocalStringManagerImpl(MailResourceManager.class);

    private String mailHost;
    private String mailUser;
    private String fromAddress;
    private String jndiName;
    private String storeProtocol;
    private String storeProtocolClass;
    private String transportProtocol;
    private String transportProtocolClass;
    private String enabled;
    private String enabledValueForTarget;
    private String debug;
    private String description;

    @Inject
    private org.glassfish.resourcebase.resources.admin.cli.ResourceUtil resourceUtil;

    @Inject
    private BindableResourcesHelper resourcesHelper;

    @Override
    public String getResourceType() {
        return ServerTags.MAIL_RESOURCE;
    }

    @Override
    public ResourceStatus create(Resources resources, ResourceAttributes attributes, final Properties properties,
        String target) throws Exception {
        setAttributes(attributes, target);

        ResourceStatus validationStatus = isValid(resources, true, target);
        if(validationStatus.getStatus() == ResourceStatus.FAILURE){
            return validationStatus;
        }

        // ensure we don't already have one of this name
        if (ResourceUtil.getBindableResourceByName(resources, jndiName) != null) {
            String msg = I18N.getLocalString(
                    "create.mail.resource.duplicate.1",
                    "A Mail Resource named {0} already exists.",
                    jndiName);
            return new ResourceStatus(ResourceStatus.FAILURE, msg, true);
        }

        try {
            SingleConfigCode<Resources> configCode = param -> {
                MailResource newResource = createConfigBean(param, properties);
                param.getResources().add(newResource);
                return newResource;
            };
            ConfigSupport.apply(configCode, resources);
            if (!CommandTarget.TARGET_DOMAIN.equals(target)) {
                resourceUtil.createResourceRef(jndiName, enabledValueForTarget, target);
            }
            String msg = I18N.getLocalString(
                    "create.mail.resource.success",
                    "Mail Resource {0} created.", jndiName);
            return new ResourceStatus(ResourceStatus.SUCCESS, msg, true);
        } catch (TransactionFailure tfe) {
            String msg = I18N.getLocalString("" +
                    "create.mail.resource.fail",
                    "Unable to create Mail Resource {0}.", jndiName) +
                    " " + tfe.getLocalizedMessage();
            return new ResourceStatus(ResourceStatus.FAILURE, msg, true);
        }
    }

    private ResourceStatus isValid(Resources resources, boolean validateResourceRef, String target){
        ResourceStatus status ;
        if (mailHost == null) {
            String msg = I18N.getLocalString("create.mail.resource.noHostName",
                    "No host name defined for Mail Resource.");
            return new ResourceStatus(ResourceStatus.FAILURE, msg, true);
        }

        if (mailUser == null) {
            String msg = I18N.getLocalString("create.mail.resource.noUserName",
                    "No user name defined for Mail Resource.");
            return new ResourceStatus(ResourceStatus.FAILURE, msg, true);
        }

        if (fromAddress == null) {
            String msg = I18N.getLocalString("create.mail.resource.noFrom",
                    "From not defined for Mail Resource.");
            return new ResourceStatus(ResourceStatus.FAILURE, msg, true);
        }


        status = resourcesHelper.validateBindableResourceForDuplicates(resources, jndiName, validateResourceRef,
                target, MailResource.class);
        if (status.getStatus() == ResourceStatus.FAILURE) {
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
            for (Entry<?, ?> e : props.entrySet()) {
                Property prop = newResource.createChild(Property.class);
                prop.setName((String) e.getKey());
                prop.setValue((String) e.getValue());
                newResource.getProperty().add(prop);
            }
        }
        return newResource;
    }

    private void setAttributes(ResourceAttributes attributes, String target) {
        jndiName = attributes.getString(JNDI_NAME);
        mailHost = attributes.getString(MAIL_HOST);
        mailUser = attributes.getString(MAIL_USER);
        fromAddress = attributes.getString(MAIL_FROM_ADDRESS);
        storeProtocol = attributes.getString(MAIL_STORE_PROTO);
        storeProtocolClass = attributes.getString(MAIL_STORE_PROTO_CLASS);
        transportProtocol = attributes.getString(MAIL_TRANS_PROTO);
        transportProtocolClass = attributes.getString(MAIL_TRANS_PROTO_CLASS);
        debug = attributes.getString(MAIL_DEBUG);
        if (target == null) {
            enabled = attributes.getString(ENABLED);
        } else {
            enabled = resourceUtil.computeEnabledValueForResourceBasedOnTarget(attributes.getString(ENABLED), target);
        }
        enabledValueForTarget = attributes.getString(ENABLED);
        description = attributes.getString(DESCRIPTION);
    }

    @Override
    public Resource createConfigBean(Resources resources, ResourceAttributes attributes, Properties properties,
        boolean validate) throws Exception {
        setAttributes(attributes, null);
        final ResourceStatus status;
        if (validate) {
            status = isValid(resources, false, null);
        } else {
            status = new ResourceStatus(ResourceStatus.SUCCESS, "");
        }
        if (status.getStatus() == ResourceStatus.SUCCESS) {
            return createConfigBean(resources, properties);
        }
        throw new ResourceException(status.getMessage(), status.getException());
    }
}
