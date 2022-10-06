/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.deployment.ResourceDescriptor;
import com.sun.enterprise.deployment.annotation.context.ResourceContainerContext;
import com.sun.enterprise.deployment.annotation.handlers.AbstractResourceHandler;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.jms.JMSDestinationDefinition;
import jakarta.jms.JMSDestinationDefinitions;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import org.glassfish.apf.AnnotationHandlerFor;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.jvnet.hk2.annotations.Service;

@Service
@AnnotationHandlerFor(JMSDestinationDefinitions.class)
public class JMSDestinationDefinitionsHandler extends AbstractResourceHandler {

    private final static LocalStringManagerImpl I18N = new LocalStringManagerImpl(
        JMSDestinationDefinitionsHandler.class);

    @Override
    protected HandlerProcessingResult processAnnotation(AnnotationInfo ainfo, ResourceContainerContext[] rcContexts)
        throws AnnotationProcessorException {
        JMSDestinationDefinitions defns = (JMSDestinationDefinitions) ainfo.getAnnotation();
        JMSDestinationDefinition values[] = defns.value();
        Set<String> duplicates = new HashSet<>();
        if (values != null && values.length > 0) {
            for (JMSDestinationDefinition defn : values) {
                String defnName = ResourceDescriptor.getJavaComponentJndiName(defn.name());
                if (duplicates.contains(defnName)) {
                    String localString = I18N.getLocalString(
                        "enterprise.deployment.annotation.handlers.jmsdestinationdefinitionsduplicates",
                        "@JMSDestinationDefinition cannot have multiple definitions with same name : ''{0}''",
                        defnName);
                    throw new IllegalStateException(localString);
                } else {
                    duplicates.add(defnName);
                }
                JMSDestinationDefinitionHandler handler = new JMSDestinationDefinitionHandler();
                handler.processAnnotation(defn, ainfo, rcContexts);
            }
            duplicates.clear();
        }
        return getDefaultProcessedResult();
    }


    @Override
    public Class<? extends Annotation>[] getTypeDependencies() {
        return getEjbAndWebAnnotationTypes();
    }
}
