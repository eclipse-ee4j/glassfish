/*
 * Copyright (c) 2021, 2023 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.connectors.deployment.annotation.handlers;

import com.sun.enterprise.deployment.AdministeredObjectDefinitionDescriptor;
import com.sun.enterprise.deployment.MetadataSource;
import com.sun.enterprise.deployment.ResourceDescriptor;
import com.sun.enterprise.deployment.annotation.context.ResourceContainerContext;
import com.sun.enterprise.deployment.annotation.handlers.AbstractResourceHandler;
import com.sun.enterprise.deployment.annotation.handlers.ResourceAnnotationControl;

import jakarta.resource.AdministeredObjectDefinition;

import java.lang.annotation.Annotation;
import java.util.Properties;
import java.util.Set;

import org.glassfish.apf.AnnotationHandlerFor;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.glassfish.deployment.common.JavaEEResourceType;
import org.jvnet.hk2.annotations.Service;


/**
 * @author Dapeng Hu
 */
@Service
@AnnotationHandlerFor(AdministeredObjectDefinition.class)
public class AdministeredObjectDefinitionHandler extends AbstractResourceHandler {

    private static final ResourceAnnotationControl CTRL = new ResourceAnnotationControl(
        AdministeredObjectDefinition.class);

    public AdministeredObjectDefinitionHandler() {
    }

    @Override
    protected HandlerProcessingResult processAnnotation(AnnotationInfo ainfo, ResourceContainerContext[] rcContexts)
            throws AnnotationProcessorException {
        AdministeredObjectDefinition adminObjectDefnAn = (AdministeredObjectDefinition)ainfo.getAnnotation();
        return processAnnotation(adminObjectDefnAn, ainfo, rcContexts);
    }


    protected HandlerProcessingResult processAnnotation(AdministeredObjectDefinition adminObjectDefnAn,
        AnnotationInfo aiInfo, ResourceContainerContext[] rcContexts) throws AnnotationProcessorException {
        Class<?> annotatedClass = (Class<?>) aiInfo.getAnnotatedElement();
        Annotation[] annotations = annotatedClass.getAnnotations();
        boolean warClass = isAWebComponentClass(annotations);
        boolean ejbClass = isAEjbComponentClass(annotations);

        for (ResourceContainerContext context : rcContexts) {
            if (!CTRL.canProcessAnnotation(annotatedClass, ejbClass, warClass, context)) {
                return getDefaultProcessedResult();
            }
            Set<ResourceDescriptor> aodDescs = context.getResourceDescriptors(JavaEEResourceType.AODD);
            AdministeredObjectDefinitionDescriptor desc = createDescriptor(adminObjectDefnAn);
            if (isDefinitionAlreadyPresent(aodDescs, desc)) {
                merge(aodDescs, adminObjectDefnAn);
            } else {
                context.addResourceDescriptor(desc);
            }
        }
        return getDefaultProcessedResult();
    }



    private boolean isDefinitionAlreadyPresent(Set<ResourceDescriptor> descriptors,
        AdministeredObjectDefinitionDescriptor desc) {
        boolean result = false ;
        for(ResourceDescriptor descriptor : descriptors){
            if(descriptor.equals(desc)){
                result = true;
                break;
            }
        }
        return result;
    }

    private void merge(Set<ResourceDescriptor> aodDescs, AdministeredObjectDefinition defn) {

        for (ResourceDescriptor descriptor : aodDescs) {
            AdministeredObjectDefinitionDescriptor desc = (AdministeredObjectDefinitionDescriptor)descriptor;
            if (desc.getName().equals(defn.name())) {

                if (desc.getDescription() == null) {
                    if (defn.description() != null && !defn.description().isEmpty()) {
                        desc.setDescription(defn.description());
                    }
                }

                if (desc.getInterfaceName() == null || desc.getInterfaceName().isEmpty()) {
                    desc.setInterfaceName(defn.interfaceName());
                }

                if (desc.getClassName() == null || desc.getClassName().isEmpty()) {
                    desc.setClassName(defn.className());
                }

                if (desc.getResourceAdapter() == null || desc.getResourceAdapter().isEmpty()) {
                    desc.setResourceAdapter(defn.resourceAdapter());
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
                            //add to properties only when not already present
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

    private AdministeredObjectDefinitionDescriptor createDescriptor(AdministeredObjectDefinition defn) {

        AdministeredObjectDefinitionDescriptor desc = new AdministeredObjectDefinitionDescriptor();
        desc.setMetadataSource(MetadataSource.ANNOTATION);

        desc.setName(defn.name());
        desc.setInterfaceName(defn.interfaceName());
        desc.setClassName(defn.className());
        desc.setResourceAdapter(defn.resourceAdapter());

        if (defn.description() != null && !defn.description().equals("")) {
            desc.setDescription(defn.description());
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

}
