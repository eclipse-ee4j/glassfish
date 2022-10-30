/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.concurrent.admin;

import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;
import jakarta.resource.ResourceException;

import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.glassfish.api.I18n;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.concurrent.config.ContextService;
import org.glassfish.resourcebase.resources.admin.cli.ResourceUtil;
import org.glassfish.resourcebase.resources.api.ResourceStatus;
import org.glassfish.resourcebase.resources.util.BindableResourcesHelper;
import org.glassfish.resources.admin.cli.ResourceManager;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.ConfiguredBy;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;

import static com.sun.appserv.connectors.internal.api.ConnectorsUtil.getResourceByName;
import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.CONTEXT_INFO;
import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.CONTEXT_INFO_DEFAULT_VALUE;
import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.CONTEXT_INFO_ENABLED;
import static org.glassfish.resources.admin.cli.ResourceConstants.ENABLED;
import static org.glassfish.resources.admin.cli.ResourceConstants.JNDI_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.SYSTEM_ALL_REQ;

/**
 *
 * The context service manager allows you to create and delete
 * the context-service config element
 */
@Service (name=ServerTags.CONTEXT_SERVICE)
@I18n("context.service.manager")
@ConfiguredBy(Resources.class)
public class ContextServiceManager implements ResourceManager {

    final private static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(ContextServiceManager.class);
    private static final String DESCRIPTION = ServerTags.DESCRIPTION;

    private String jndiName = null;
    private String description = null;
    private String contextInfoEnabled = Boolean.TRUE.toString();
    private String contextInfo = CONTEXT_INFO_DEFAULT_VALUE;
    private String enabled = Boolean.TRUE.toString();
    private String enabledValueForTarget = Boolean.TRUE.toString();

    @Inject
    private ResourceUtil resourceUtil;

    @Inject
    private ServerEnvironment environment;

    @Inject
    private BindableResourcesHelper resourcesHelper;

    @Override
    public String getResourceType () {
        return ServerTags.CONTEXT_SERVICE;
    }

    @Override
    public ResourceStatus create(Resources resources, HashMap attributes, final Properties properties,
                                 String target) throws Exception {

        setAttributes(attributes, target);

        ResourceStatus validationStatus = isValid(resources, true, target);
        if(validationStatus.getStatus() == ResourceStatus.FAILURE){
            return validationStatus;
        }

        try {
            ConfigSupport.apply(new SingleConfigCode<Resources>() {

                @Override
                public Object run(Resources param) throws PropertyVetoException, TransactionFailure {
                    return createResource(param, properties);
                }
            }, resources);

                resourceUtil.createResourceRef(jndiName, enabledValueForTarget, target);
        } catch (TransactionFailure tfe) {
            String msg = localStrings.getLocalString("create.context.service.failed", "Context service {0} creation failed", jndiName) + " " + tfe.getLocalizedMessage();
            ResourceStatus status = new ResourceStatus(ResourceStatus.FAILURE, msg);
            status.setException(tfe);
            return status;
        }
        String msg = localStrings.getLocalString("create.context.service.success", "Context service {0} created successfully", jndiName);
        return new ResourceStatus(ResourceStatus.SUCCESS, msg);
    }

    private ResourceStatus isValid(Resources resources, boolean validateResourceRef, String target){
        ResourceStatus status ;


        if (jndiName == null) {
            String msg = localStrings.getLocalString("context.service.noJndiName", "No JNDI name defined for context service.");
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }

        status = resourcesHelper.validateBindableResourceForDuplicates(resources, jndiName, validateResourceRef, target, ContextService.class);

        return status;
    }

    private void setAttributes(HashMap attributes, String target) {
        jndiName = (String) attributes.get(JNDI_NAME);
        description = (String) attributes.get(DESCRIPTION);
        contextInfoEnabled = (String) attributes.get(CONTEXT_INFO_ENABLED);
        contextInfo = (String) attributes.get(CONTEXT_INFO);
        if(target != null){
            enabled = resourceUtil.computeEnabledValueForResourceBasedOnTarget((String)attributes.get(ENABLED), target);
        }else{
            enabled = (String) attributes.get(ENABLED);
        }
        enabledValueForTarget = (String) attributes.get(ENABLED);
    }

    private ContextService createResource(Resources param, Properties properties) throws PropertyVetoException,
            TransactionFailure {
        ContextService newResource = createConfigBean(param, properties);
        param.getResources().add(newResource);
        return newResource;
    }

    private ContextService createConfigBean(Resources param, Properties properties) throws PropertyVetoException,
            TransactionFailure {
        ContextService contextService = param.createChild(ContextService.class);
        contextService.setJndiName(jndiName);
        if (description != null) {
            contextService.setDescription(description);
        }
        contextService.setContextInfoEnabled(contextInfoEnabled);
        contextService.setContextInfo(contextInfo);
        contextService.setEnabled(enabled);
        if (properties != null) {
            for ( Map.Entry e : properties.entrySet()) {
                Property prop = contextService.createChild(Property.class);
                prop.setName((String)e.getKey());
                prop.setValue((String)e.getValue());
                contextService.getProperty().add(prop);
            }
        }
        return contextService;
    }

    @Override
    public Resource createConfigBean(final Resources resources, HashMap attributes, final Properties properties, boolean validate) throws Exception{
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
        throw new ResourceException(status.getMessage());
    }


    public ResourceStatus delete(final Resources resources, final String jndiName, final String target)
        throws Exception {
        if (jndiName == null) {
            String msg = localStrings.getLocalString("context.service.noJndiName", "No JNDI name defined for context service.");
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }

        SimpleJndiName simpleJndiName = new SimpleJndiName(jndiName);
        Resource resource = getResourceByName(resources, ContextService.class, simpleJndiName);

        // ensure we already have this resource
        if (resource == null){
            String msg = localStrings.getLocalString("delete.context.service.notfound", "A context service named {0} does not exist.", jndiName);
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }

        if (SYSTEM_ALL_REQ.equals(resource.getObjectType())) {
            String msg = localStrings.getLocalString("delete.concurrent.resource.notAllowed", "The {0} resource cannot be deleted as it is required to be configured in the system.", jndiName);
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }

        if (environment.isDas()) {

            if ("domain".equals(target)) {
                if (!resourceUtil.getTargetsReferringResourceRef(simpleJndiName).isEmpty()) {
                    String msg = localStrings.getLocalString("delete.context.service.resource-ref.exist", "This context service [ {0} ] is referenced in an instance/cluster target, use delete-resource-ref on appropriate target", jndiName);
                    return new ResourceStatus(ResourceStatus.FAILURE, msg);
                }
            } else {
                if (!resourceUtil.isResourceRefInTarget(simpleJndiName, target)) {
                    String msg = localStrings.getLocalString("delete.context.service.no.resource-ref", "This context service [ {0} ] is not referenced in target [ {1} ]", jndiName, target);
                    return new ResourceStatus(ResourceStatus.FAILURE, msg);
                }

                if (resourceUtil.getTargetsReferringResourceRef(simpleJndiName).size() > 1) {
                    String msg = localStrings.getLocalString("delete.context.service.multiple.resource-refs", "This context service [ {0} ] is referenced in multiple instance/cluster targets, Use delete-resource-ref on appropriate target", jndiName);
                    return new ResourceStatus(ResourceStatus.FAILURE, msg);
                }
            }
        }

        try {
            // delete resource-ref
            resourceUtil.deleteResourceRef(simpleJndiName, target);

            // delete context-service
            SingleConfigCode<Resources> configCode = (SingleConfigCode<Resources>) param -> {
                ContextService resource1 = getResourceByName(resources, ContextService.class, simpleJndiName);
                return param.getResources().remove(resource1);
            };
            if (ConfigSupport.apply(configCode, resources) == null) {
                String msg = localStrings.getLocalString("delete.context.service.failed", "Context service {0} deletion failed", jndiName);
                return new ResourceStatus(ResourceStatus.FAILURE, msg);
            }
        } catch(TransactionFailure tfe) {
            String msg = localStrings.getLocalString("delete.context.service.failed", "Context service {0} deletion failed ", jndiName);
            ResourceStatus status = new ResourceStatus(ResourceStatus.FAILURE, msg);
            status.setException(tfe);
            return status;
        }

        String msg = localStrings.getLocalString("delete.context.service.success", "Context service {0} deleted successfully", jndiName);
        return new ResourceStatus(ResourceStatus.SUCCESS, msg);
    }
}
