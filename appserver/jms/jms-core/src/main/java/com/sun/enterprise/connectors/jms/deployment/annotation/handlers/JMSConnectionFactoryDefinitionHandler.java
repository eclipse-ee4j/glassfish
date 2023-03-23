/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.connectors.jms.deployment.annotation.handlers;

import com.sun.enterprise.deployment.JMSConnectionFactoryDefinitionDescriptor;
import com.sun.enterprise.deployment.MetadataSource;
import com.sun.enterprise.deployment.ResourceDescriptor;
import com.sun.enterprise.deployment.annotation.context.ResourceContainerContext;
import com.sun.enterprise.deployment.annotation.handlers.AbstractResourceHandler;
import com.sun.enterprise.deployment.annotation.handlers.ResourceAnnotationControl;

import jakarta.jms.JMSConnectionFactoryDefinition;

import java.lang.annotation.Annotation;
import java.util.Properties;
import java.util.Set;

import org.glassfish.apf.AnnotationHandlerFor;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.glassfish.deployment.common.JavaEEResourceType;
import org.jvnet.hk2.annotations.Service;

@Service
@AnnotationHandlerFor(JMSConnectionFactoryDefinition.class)
public class JMSConnectionFactoryDefinitionHandler extends AbstractResourceHandler {

    private static final ResourceAnnotationControl CTRL = new ResourceAnnotationControl(
        JMSConnectionFactoryDefinition.class);

    public JMSConnectionFactoryDefinitionHandler() {
    }

    @Override
    protected HandlerProcessingResult processAnnotation(AnnotationInfo ainfo, ResourceContainerContext[] rcContexts) throws AnnotationProcessorException {
        JMSConnectionFactoryDefinition jmsConnectionFactoryDefnAn = (JMSConnectionFactoryDefinition) ainfo.getAnnotation();
        return processAnnotation(jmsConnectionFactoryDefnAn, ainfo, rcContexts);
    }

    protected HandlerProcessingResult processAnnotation(JMSConnectionFactoryDefinition jmsConnectionFactoryDefnAn, AnnotationInfo aiInfo, ResourceContainerContext[] rcContexts) throws AnnotationProcessorException {
        Class<?> annotatedClass = (Class<?>) aiInfo.getAnnotatedElement();
        Annotation[] annotations = annotatedClass.getAnnotations();
        boolean warClass = isAWebComponentClass(annotations);
        boolean ejbClass = isAEjbComponentClass(annotations);

        for (ResourceContainerContext context : rcContexts) {
            if (!CTRL.canProcessAnnotation(annotatedClass, ejbClass, warClass, context)) {
                return getDefaultProcessedResult();
            }

            Set<ResourceDescriptor> jmscfdDescs = context.getResourceDescriptors(JavaEEResourceType.JMSCFDD);
            JMSConnectionFactoryDefinitionDescriptor desc = createDescriptor(jmsConnectionFactoryDefnAn);
            if (isDefinitionAlreadyPresent(jmscfdDescs, desc)) {
                merge(jmscfdDescs, jmsConnectionFactoryDefnAn);
                for (ResourceDescriptor jmscfdDesc : jmscfdDescs) {
                    if (jmscfdDesc instanceof JMSConnectionFactoryDefinitionDescriptor) {
                        setDefaultTransactionSupport((JMSConnectionFactoryDefinitionDescriptor) jmscfdDesc);
                    }
                }
            } else {
                setDefaultTransactionSupport(desc);
                context.addResourceDescriptor(desc);
            }
        }
        return getDefaultProcessedResult();
    }

    private void setDefaultTransactionSupport(JMSConnectionFactoryDefinitionDescriptor desc) {
        Properties props = desc.getProperties();
        if (props.get("org.glassfish.connector-connection-pool.transaction-support") == null) {
            props.put("org.glassfish.connector-connection-pool.transaction-support", "XATransaction");
        }
    }

    private boolean isDefinitionAlreadyPresent(Set<ResourceDescriptor> jmscfdDescs, JMSConnectionFactoryDefinitionDescriptor desc) {
        for (ResourceDescriptor descriptor : jmscfdDescs) {
            if (descriptor.equals(desc)) {
                return true;
            }
        }
        return false;
    }

    private void merge(Set<ResourceDescriptor> jmscfdDescs, JMSConnectionFactoryDefinition defn) {

        for (ResourceDescriptor descriptor : jmscfdDescs) {
            if (descriptor instanceof JMSConnectionFactoryDefinitionDescriptor) {
                JMSConnectionFactoryDefinitionDescriptor desc = (JMSConnectionFactoryDefinitionDescriptor) descriptor;
                if (desc.getName().equals(defn.name())) {

                    if (desc.getInterfaceName() == null) {
                        desc.setInterfaceName(defn.interfaceName());
                    }

                    if (desc.getClassName() == null) {
                        if (isValid(defn.className())) {
                            desc.setClassName(defn.className());
                        }
                    }

                    if (desc.getDescription() == null) {
                        if (isValid(defn.description())) {
                            desc.setDescription(defn.description());
                        }
                    }

                    if (desc.getResourceAdapter() == null) {
                        if (isValid(defn.resourceAdapter())) {
                            desc.setResourceAdapter(defn.resourceAdapter());
                        }
                    }

                    if (desc.getUser() == null) {
                        if (isValid(defn.user())) {
                            desc.setUser(defn.user());
                        }
                    }

                    if (desc.getPassword() == null) {
                        if (defn.password() != null /* ALLOW EMPTY PASSWORDS && !defn.password().isEmpty() */) {
                            desc.setPassword(defn.password());
                        }
                    }

                    if (desc.getClientId() == null) {
                        if (isValid(defn.clientId())) {
                            desc.setClientId(defn.clientId());
                        }
                    }

                    if (!desc.isTransactionSet()) {
                        desc.setTransactional(defn.transactional());
                    }

                    if (desc.getMaxPoolSize() < 0) {
                        if (defn.maxPoolSize() >= 0) {
                            desc.setMaxPoolSize(defn.maxPoolSize());
                        }
                    }

                    if (desc.getMinPoolSize() < 0) {
                        if (defn.minPoolSize() >= 0) {
                            desc.setMinPoolSize(defn.minPoolSize());
                        }
                    }

                    Properties properties = desc.getProperties();
                    String[] defnProperties = defn.properties();

                    if (defnProperties.length > 0) {
                        for (String property : defnProperties) {
                            int index = property.indexOf('=');
                            // found "=" and not at start or end of string
                            if (index > 0 && index < property.length() - 1) {
                                String name = property.substring(0, index).trim();
                                String value = property.substring(index + 1).trim();
                                // add to properties only when not already present
                                if (properties.get(name) == null) {
                                    properties.put(name, value);
                                }
                            }
                        }
                    }
                    break;
                }
            }
        }

    }

    private JMSConnectionFactoryDefinitionDescriptor createDescriptor(JMSConnectionFactoryDefinition defn) {

        JMSConnectionFactoryDefinitionDescriptor desc = new JMSConnectionFactoryDefinitionDescriptor();
        desc.setMetadataSource(MetadataSource.ANNOTATION);

        desc.setName(defn.name());
        desc.setInterfaceName(defn.interfaceName());

        if (isValid(defn.className())) {
            desc.setClassName(defn.className());
        }

        if (isValid(defn.description())) {
            desc.setDescription(defn.description());
        }

        if (isValid(defn.resourceAdapter())) {
            desc.setResourceAdapter(defn.resourceAdapter());
        }

        if (isValid(defn.user())) {
            desc.setUser(defn.user());
        }

        if (defn.password() != null /* ALLOW EMPTY PASSWORDS && !defn.password().isEmpty() */) {
            desc.setPassword(defn.password());
        }

        if (isValid(defn.clientId())) {
            desc.setClientId(defn.clientId());
        }

        desc.setTransactional(defn.transactional());

        if (defn.maxPoolSize() >= 0) {
            desc.setMaxPoolSize(defn.maxPoolSize());
        }
        if (defn.minPoolSize() >= 0) {
            desc.setMinPoolSize(defn.minPoolSize());
        }

        if (defn.properties() != null) {
            Properties properties = desc.getProperties();

            String[] defnProperties = defn.properties();
            if (defnProperties.length > 0) {
                for (String property : defnProperties) {
                    int index = property.indexOf("=");
                    // found "=" and not at start or end of string
                    if (index > 0 && index < property.length() - 1) {
                        String name = property.substring(0, index).trim();
                        String value = property.substring(index + 1).trim();
                        properties.put(name, value);
                    }
                }
            }
        }

        return desc;
    }

    private boolean isValid(String s) {
        return s != null && !s.isEmpty();
    }
}
