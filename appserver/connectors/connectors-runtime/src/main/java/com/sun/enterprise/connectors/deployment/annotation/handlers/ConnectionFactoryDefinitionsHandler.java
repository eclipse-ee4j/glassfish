/*
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


import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import jakarta.resource.ConnectionFactoryDefinition;
import jakarta.resource.ConnectionFactoryDefinitions;
import org.glassfish.apf.AnnotationHandlerFor;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.jvnet.hk2.annotations.Service;

import com.sun.enterprise.deployment.ConnectionFactoryDefinitionDescriptor;
import com.sun.enterprise.deployment.annotation.context.ResourceContainerContext;
import com.sun.enterprise.deployment.annotation.handlers.AbstractResourceHandler;
import com.sun.enterprise.util.LocalStringManagerImpl;

/**
 * @author Dapeng Hu
 */
@Service
@AnnotationHandlerFor(ConnectionFactoryDefinitions.class)
public class ConnectionFactoryDefinitionsHandler extends AbstractResourceHandler {

    protected final static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(ConnectionFactoryDefinitionsHandler.class);


    public ConnectionFactoryDefinitionsHandler() {
    }


    @Override
    protected HandlerProcessingResult processAnnotation(AnnotationInfo ainfo,  ResourceContainerContext[] rcContexts)
            throws AnnotationProcessorException {
        ConnectionFactoryDefinitions defns = (ConnectionFactoryDefinitions) ainfo.getAnnotation();
        ConnectionFactoryDefinition values[] = defns.value();
        Set<String> duplicates = new HashSet<String>();
        if(values != null && values.length >0){
            for(ConnectionFactoryDefinition defn : values){
                String defnName = ConnectionFactoryDefinitionDescriptor.getJavaName(defn.name());

                if(duplicates.contains(defnName)){
                    // where is the local-string file?
                    String localString = localStrings.getLocalString(
                            "enterprise.deployment.annotation.handlers.connectionfactorydefinitionsduplicates",
                            "@ConnectionFactoryDefinitions cannot have multiple definitions with same name : ''{0}''",
                            defnName);
                    throw new IllegalStateException(localString);
                    /*
                    //TODO V3 should we throw exception or return failure result ?
                    return getFailureResult(ainfo, "@DataSourceDefinitions cannot have multiple" +
                            " definitions with same name [ "+defnName+" ]", true );
                    */
                }else{
                    duplicates.add(defnName);
                }
                ConnectionFactoryDefinitionHandler handler = new ConnectionFactoryDefinitionHandler();
                handler.processAnnotation(defn, ainfo, rcContexts);
            }
            duplicates.clear();
        }
        return getDefaultProcessedResult();
    }


    public Class<? extends Annotation>[] getTypeDependencies() {
        return getEjbAndWebAnnotationTypes();
    }

}
