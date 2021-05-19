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

package org.glassfish.jdbcruntime.deployment.annotation.handlers;


import com.sun.enterprise.deployment.annotation.handlers.*;
import org.glassfish.apf.*;
import org.jvnet.hk2.annotations.Service;
import org.glassfish.apf.impl.HandlerProcessingResultImpl;

import jakarta.annotation.sql.DataSourceDefinitions;
import jakarta.annotation.sql.DataSourceDefinition;
import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.Level;

import com.sun.enterprise.deployment.annotation.context.ResourceContainerContext;
import com.sun.enterprise.deployment.DataSourceDefinitionDescriptor;
import com.sun.enterprise.util.LocalStringManagerImpl;

/**
 * @author Jagadish Ramu
 */
@Service
@AnnotationHandlerFor(DataSourceDefinitions.class)
public class DataSourceDefinitionsHandler extends AbstractResourceHandler {

    protected final static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(DataSourceDefinitionsHandler.class);

    public DataSourceDefinitionsHandler() {
    }

    protected HandlerProcessingResult processAnnotation(AnnotationInfo ainfo, ResourceContainerContext[] rcContexts)
            throws AnnotationProcessorException {
        DataSourceDefinitions defns = (DataSourceDefinitions) ainfo.getAnnotation();

        DataSourceDefinition values[] = defns.value();
        Set duplicates = new HashSet();
        if(values != null && values.length >0){
            for(DataSourceDefinition defn : values){
                String defnName = DataSourceDefinitionDescriptor.getJavaName(defn.name());

                if(duplicates.contains(defnName)){
                String localString = localStrings.getLocalString(
                        "enterprise.deployment.annotation.handlers.datasourcedefinitionsduplicates",
                        "@DataSourceDefinitions cannot have multiple definitions with same name : ''{0}''",
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
                DataSourceDefinitionHandler handler = new DataSourceDefinitionHandler();
                handler.processAnnotation(defn, ainfo, rcContexts);
            }
            duplicates.clear();
        }
        return getDefaultProcessedResult();
    }

/*    private HandlerProcessingResultImpl getFailureResult(AnnotationInfo element, String message, boolean doLog) {
        HandlerProcessingResultImpl result = new HandlerProcessingResultImpl();
        result.addResult(getAnnotationType(), ResultType.FAILED);
        if (doLog) {
            Class c = (Class) element.getAnnotatedElement();
            String className = c.getName();
            String localString = localStrings.getLocalString(
                    "enterprise.deployment.annotation.handlers.datasourcedefinitionsfailure",
                    "failed to handle annotation [ {0} ] on class [ {1} ] due to the following exception : ",
                    element.getAnnotation(), className);
            logger.log(Level.WARNING, localString, message);
        }
        return result;
    }
*/
    public Class<? extends Annotation>[] getTypeDependencies() {
        return getEjbAndWebAnnotationTypes();
    }
}
