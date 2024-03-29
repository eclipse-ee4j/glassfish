/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment.annotation.handlers;

import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.deployment.InjectionTarget;
import com.sun.enterprise.deployment.MessageDestinationReferenceDescriptor;
import com.sun.enterprise.deployment.MetadataSource;
import com.sun.enterprise.deployment.ResourceEnvReferenceDescriptor;
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.enterprise.deployment.WSDolSupport;
import com.sun.enterprise.deployment.annotation.context.ResourceContainerContext;
import com.sun.enterprise.deployment.annotation.context.ResourceContainerContextImpl;
import com.sun.enterprise.deployment.util.DOLUtils;

import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.lang.annotation.ElementType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;

import org.glassfish.apf.AnnotationHandlerFor;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.deployment.types.EnvironmentPropertyValueTypes.MAPPING;
import static com.sun.enterprise.util.StringUtils.ok;

/**
 * This handler is responsible for handling the jakarta.annotation.Resource annotation.
 */
@Service
@AnnotationHandlerFor(Resource.class)
public class ResourceHandler extends AbstractResourceHandler {

    @Inject
    private ServiceLocator habitat;

    @Inject
    private Provider<WSDolSupport> wSDolSupportProvider;


    public ResourceHandler() {
    }


    /**
     * This entry point is used both for a single @Resource and iteratively
     * from a compound @Resources processor.
     */
    @Override
    protected HandlerProcessingResult processAnnotation(AnnotationInfo ainfo, ResourceContainerContext[] rcContexts)
        throws AnnotationProcessorException {
        Resource resourceAn = (Resource) ainfo.getAnnotation();
        return processResource(ainfo, rcContexts, resourceAn);
    }


    protected HandlerProcessingResult processResource(AnnotationInfo ainfo, ResourceContainerContext[] rcContexts,
        Resource resourceAn) throws AnnotationProcessorException {

        final String defaultLogicalName;
        final Class<?> defaultResourceType;
        final InjectionTarget target;
        if (ElementType.FIELD.equals(ainfo.getElementType())) {
            Field f = (Field) ainfo.getAnnotatedElement();
            String targetClassName = f.getDeclaringClass().getName();

            defaultLogicalName = targetClassName + "/" + f.getName();
            defaultResourceType = f.getType();

            target = new InjectionTarget();
            target.setFieldName(f.getName());
            target.setClassName(targetClassName);
            target.setMetadataSource(MetadataSource.ANNOTATION);

        } else if (ElementType.METHOD.equals(ainfo.getElementType())) {

            Method m = (Method) ainfo.getAnnotatedElement();
            String targetClassName = m.getDeclaringClass().getName();

            validateInjectionMethod(m, ainfo);

            // Derive javabean property name.
            String propertyName = getInjectionMethodPropertyName(m, ainfo);

            // prefixing with fully qualified type name
            defaultLogicalName = targetClassName + "/" + propertyName;
            defaultResourceType = m.getParameterTypes()[0];

            target = new InjectionTarget();
            target.setMethodName(m.getName());
            target.setClassName(targetClassName);
            target.setMetadataSource(MetadataSource.ANNOTATION);

        } else if (ElementType.TYPE.equals(ainfo.getElementType())) {
            // name() and type() are required for TYPE-level @Resource
            if (resourceAn.name().isEmpty() || resourceAn.type() == Object.class) {
                Class<?> c = (Class<?>) ainfo.getAnnotatedElement();
                log(Level.SEVERE, ainfo, I18N.getLocalString(
                    "enterprise.deployment.annotation.handlers.invalidtypelevelresource",
                    "Invalid TYPE-level @Resource with name() = [{0}] and "
                        + "type = [{1}] in {2}. Each TYPE-level @Resource must " + "specify both name() and type().",
                    new Object[] {resourceAn.name(), resourceAn.type(), c}));
                return getDefaultFailedResult();
            }
            defaultLogicalName = null;
            defaultResourceType = null;
            target = null;
        } else {
            // can't happen
            return getDefaultFailedResult();
        }

        // NOTE that default value is Object.class, not null
        Class<?> resourceType = resourceAn.type() == Object.class ? defaultResourceType : resourceAn.type();
        String logicalName = resourceAn.name().isEmpty() ? defaultLogicalName : resourceAn.name();

        /*
         * Get corresponding class type. This does the appropriate
         * mapping for primitives. For everything else, the type is
         * unchanged. Really onlt need to do this for simple env-entries,
         * but it shouldn't hurt to do it for everything.
         */
        resourceType = MAPPING.getOrDefault(resourceType, resourceType);

        EnvironmentProperty[] descriptors = getDescriptors(resourceType, logicalName, rcContexts, resourceAn);
        for (EnvironmentProperty desc : descriptors) {
            if (target != null) {
                desc.addInjectionTarget(target);
            }

            if (!ok(desc.getName())) { // a new one
                desc.setName(logicalName);
            }
            if (!ok(desc.getInjectResourceType())) {
                // if the optional resource type is not set,
                // set it using the resource type of field/method
                desc.setInjectResourceType(resourceType.getName());
            }

            // merge description
            if (!ok(desc.getDescription()) && ok(resourceAn.description())) {
                desc.setDescription(resourceAn.description());
            }

            // merge lookup-name and mapped-name
            if (!desc.hasLookupName() && !desc.isSetValueCalled() && ok(resourceAn.lookup())) {
                desc.setLookupName(SimpleJndiName.of(resourceAn.lookup()));
            }
            if ((desc.getMappedName() == null || desc.getMappedName().isEmpty()) && ok(resourceAn.mappedName())) {
                desc.setMappedName(SimpleJndiName.of(resourceAn.mappedName()));
            }

            // merge authentication-type and shareable
            if (desc instanceof ResourceReferenceDescriptor) {
                ResourceReferenceDescriptor rdesc = (ResourceReferenceDescriptor) desc;
                if (!rdesc.hasAuthorization()) {
                    switch (resourceAn.authenticationType()) {
                        case APPLICATION:
                            rdesc.setAuthorization(ResourceReferenceDescriptor.APPLICATION_AUTHORIZATION);
                            break;
                        case CONTAINER:
                            rdesc.setAuthorization(ResourceReferenceDescriptor.CONTAINER_AUTHORIZATION);
                            break;
                        default: // should never happen
                            Class<?> c = (Class<?>) ainfo.getAnnotatedElement();
                            log(Level.SEVERE, ainfo,
                                I18N.getLocalString(
                                    "enterprise.deployment.annotation.handlers.invalidauthenticationtype",
                                    "Invalid AuthenticationType [{0}] in @Resource " + "with name() = [{1}] and "
                                        + "type = [{1}] in {2}.",
                                    new Object[] {resourceAn.authenticationType(), resourceAn.name(), resourceAn.type(),
                                        c}));
                            return getDefaultFailedResult();
                    }
                }
                if (!rdesc.hasSharingScope()) {
                    rdesc.setSharingScope(resourceAn.shareable()
                        ? ResourceReferenceDescriptor.RESOURCE_SHAREABLE
                        : ResourceReferenceDescriptor.RESOURCE_UNSHAREABLE);
                }
            }
        }

        return getDefaultProcessedResult();
    }


    private EnvironmentProperty[] getDescriptors(Class<?> resourceType, String logicalName,
        ResourceContainerContext[] rcContexts, Resource resourceAn) {
        Class<?> webServiceContext = null;
        try {
            WSDolSupport support = wSDolSupportProvider.get();
            if (support != null) {
                webServiceContext = support.getType("jakarta.xml.ws.WebServiceContext");
            }
        } catch (Exception e) {
            // we don't care, either we don't have the class, ot the bundled is not installed
        }
        if (resourceType.getName().equals("jakarta.jms.Queue") || resourceType.getName().equals("jakarta.jms.Topic")) {
            return getMessageDestinationReferenceDescriptors(logicalName, rcContexts);
        } else if (MAPPING.containsKey(resourceType) || resourceType.isEnum()) {
            return getEnvironmentPropertyDescriptors(logicalName, rcContexts, resourceAn);
        } else if (resourceType == javax.sql.DataSource.class
            || resourceType.getName().equals("jakarta.jms.ConnectionFactory")
            || resourceType.getName().equals("jakarta.jms.QueueConnectionFactory")
            || resourceType.getName().equals("jakarta.jms.TopicConnectionFactory")
            || resourceType == webServiceContext
            || resourceType.getName().equals("jakarta.mail.Session")
            || resourceType.getName().equals("java.net.URL")
            || resourceType.getName().equals("jakarta.resource.cci.ConnectionFactory")
            || resourceType == org.omg.CORBA_2_3.ORB.class
            || resourceType == org.omg.CORBA.ORB.class
            || resourceType.getName().equals("jakarta.jms.XAConnectionFactory")
            || resourceType.getName().equals("jakarta.jms.XAQueueConnectionFactory")
            || resourceType.getName().equals("jakarta.jms.XATopicConnectionFactory")
            || DOLUtils.isRAConnectionFactory(habitat, resourceType.getName(),
                ((ResourceContainerContextImpl) rcContexts[0]).getAppFromDescriptor())) {
            return getResourceReferenceDescriptors(logicalName, rcContexts);
        } else {
            return getResourceEnvReferenceDescriptors(logicalName, rcContexts);
        }
    }


    /**
     * Return ResourceReferenceDescriptors with given name if exists or a new
     * one without name being set.
     *
     * @param logicalName
     * @param rcContexts
     * @return an array of ResourceReferenceDescriptor
     */
    private ResourceReferenceDescriptor[] getResourceReferenceDescriptors(String logicalName,
        ResourceContainerContext[] rcContexts) {
        ResourceReferenceDescriptor resourceRefs[] = new ResourceReferenceDescriptor[rcContexts.length];
        for (int i = 0; i < rcContexts.length; i++) {
            ResourceReferenceDescriptor resourceRef = rcContexts[i].getResourceReference(logicalName);
            if (resourceRef == null) {
                resourceRef = new ResourceReferenceDescriptor();
                rcContexts[i].addResourceReferenceDescriptor(resourceRef);
            }
            resourceRefs[i] = resourceRef;
        }

        return resourceRefs;
    }


    /**
     * Return MessageDestinationReferenceDescriptors with given name
     * if exists or a new one without name being set.
     *
     * @param logicName
     * @param rcContexts
     * @return an array of message destination reference descriptors
     */
    private MessageDestinationReferenceDescriptor[] getMessageDestinationReferenceDescriptors(String logicName,
        ResourceContainerContext[] rcContexts) {
        MessageDestinationReferenceDescriptor msgDestRefs[] = new MessageDestinationReferenceDescriptor[rcContexts.length];
        for (int i = 0; i < rcContexts.length; i++) {
            MessageDestinationReferenceDescriptor msgDestRef = rcContexts[i].getMessageDestinationReference(logicName);
            if (msgDestRef == null) {
                msgDestRef = new MessageDestinationReferenceDescriptor();
                rcContexts[i].addMessageDestinationReferenceDescriptor(msgDestRef);
            }
            msgDestRefs[i] = msgDestRef;
        }

        return msgDestRefs;
    }


    /**
     * Return ResourceEnvReferenceDescriptors with given name
     * if exists or a new one without name being set.
     *
     * @param logicName
     * @param rcContexts
     * @return an array of resource env reference descriptors
     */
    private ResourceEnvReferenceDescriptor[] getResourceEnvReferenceDescriptors(String logicName,
        ResourceContainerContext[] rcContexts) {
        ResourceEnvReferenceDescriptor resourceEnvRefs[] = new ResourceEnvReferenceDescriptor[rcContexts.length];
        for (int i = 0; i < rcContexts.length; i++) {
            ResourceEnvReferenceDescriptor resourceEnvRef = rcContexts[i].getResourceEnvReference(logicName);
            if (resourceEnvRef == null) {
                resourceEnvRef = new ResourceEnvReferenceDescriptor();
                rcContexts[i].addResourceEnvReferenceDescriptor(resourceEnvRef);
            }
            resourceEnvRefs[i] = resourceEnvRef;
        }

        return resourceEnvRefs;
    }


    /**
     * Return EnvironmentProperty descriptors with the given name
     * if it exists or a new one without name being set.
     *
     * @param logicalName the JNDI name
     * @param rcContexts
     * @return an array of EnvironmentProperty descriptors
     */
    private EnvironmentProperty[] getEnvironmentPropertyDescriptors(String logicalName,
        ResourceContainerContext[] rcContexts, Resource annotation) {
        Collection<EnvironmentProperty> envEntries = new ArrayList<>();
        for (ResourceContainerContext rcContext : rcContexts) {
            EnvironmentProperty envEntry = rcContext.getEnvEntry(logicalName);
            // For @Resource declarations that map to env-entries, if there
            // is no corresponding deployment descriptor entry that has a
            // value and no lookup(), it's treated as if the declaration
            // doesn't exist.
            // A common case is that the @Resource is applied to a field
            // with a default value which was not overridden by the deployer.
            if (envEntry != null) {
                envEntries.add(envEntry);
            } else {
                envEntry = new EnvironmentProperty();
                envEntries.add(envEntry);
                rcContext.addEnvEntryDescriptor(envEntry);
            }
        }

        return envEntries.toArray(new EnvironmentProperty[envEntries.size()]);
    }
}
