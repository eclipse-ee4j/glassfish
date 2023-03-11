/*
 * Copyright (c) 2021, 2023 Contributors to the Eclipse Foundation
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

package org.glassfish.jdbcruntime.deployment.annotation.handlers;

import com.sun.enterprise.deployment.DataSourceDefinitionDescriptor;
import com.sun.enterprise.deployment.MetadataSource;
import com.sun.enterprise.deployment.ResourceDescriptor;
import com.sun.enterprise.deployment.annotation.context.ResourceContainerContext;
import com.sun.enterprise.deployment.annotation.handlers.AbstractResourceHandler;
import com.sun.enterprise.deployment.annotation.handlers.ResourceAnnotationControl;

import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.AroundTimeout;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.Interceptors;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.glassfish.apf.AnnotationHandlerFor;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.glassfish.deployment.common.JavaEEResourceType;
import org.jvnet.hk2.annotations.Service;

/**
 * @author Jagadish Ramu
 */
@Service
@AnnotationHandlerFor(DataSourceDefinition.class)
public class DataSourceDefinitionHandler extends AbstractResourceHandler {

    private static final ResourceAnnotationControl CTRL = new ResourceAnnotationControl(DataSourceDefinition.class);

    public DataSourceDefinitionHandler() {
    }

    @Override
    protected HandlerProcessingResult processAnnotation(AnnotationInfo ainfo, ResourceContainerContext[] rcContexts) throws AnnotationProcessorException {
        DataSourceDefinition dataSourceDefnAn = (DataSourceDefinition) ainfo.getAnnotation();
        return processAnnotation(dataSourceDefnAn, ainfo, rcContexts);
    }

    protected HandlerProcessingResult processAnnotation(DataSourceDefinition dataSourceDefnAn, AnnotationInfo aiInfo, ResourceContainerContext[] rcContexts) throws AnnotationProcessorException {
        Class<?> annotatedClass = (Class<?>) aiInfo.getAnnotatedElement();
        Annotation[] annotations = annotatedClass.getAnnotations();
        boolean warClass = isAWebComponentClass(annotations);
        boolean ejbClass = isAEjbComponentClass(annotations);

        for (ResourceContainerContext context : rcContexts) {
            if (!CTRL.canProcessAnnotation(annotatedClass, ejbClass, warClass, context)) {
                return getDefaultProcessedResult();
            }

            Set<ResourceDescriptor> dsdDescs = context.getResourceDescriptors(JavaEEResourceType.DSD);
            DataSourceDefinitionDescriptor desc = createDescriptor(dataSourceDefnAn);
            if (isDefinitionAlreadyPresent(dsdDescs, desc)) {
                merge(dsdDescs, dataSourceDefnAn);
            } else {
                dsdDescs.add(desc);
            }
        }
        return getDefaultProcessedResult();
    }

    private boolean isDefinitionAlreadyPresent(Set<ResourceDescriptor> dsdDescs, DataSourceDefinitionDescriptor desc) {
        boolean result = false;
        for (ResourceDescriptor descriptor : dsdDescs) {
            if (descriptor.equals(desc)) {
                result = true;
                break;
            }
        }
        return result;
    }

    @Override
    public Class<? extends Annotation>[] getTypeDependencies() {
        Class<? extends Annotation>[] annotations = getEjbAndWebAnnotationTypes();
        List<Class<? extends Annotation>> annotationsList = new ArrayList<>();
        for (Class<? extends Annotation> annotation : annotations) {
            annotationsList.add(annotation);
        }
        annotationsList.add(Interceptors.class);
        annotationsList.add(Interceptor.class);
        annotationsList.add(AroundInvoke.class);
        annotationsList.add(AroundTimeout.class);

        Class<? extends Annotation>[] result = new Class[annotationsList.size()];
        return annotationsList.toArray(result);
    }

    private void merge(Set<ResourceDescriptor> dsdDescs, DataSourceDefinition defn) {

        for (ResourceDescriptor orgdesc : dsdDescs) {
            DataSourceDefinitionDescriptor desc = (DataSourceDefinitionDescriptor) orgdesc;
            if (desc.getName().equals(defn.name())) {

                if (desc.getClassName() == null) {
                    desc.setClassName(defn.className());
                }

                if (desc.getDescription() == null) {
                    if (defn.description() != null && !defn.description().isEmpty()) {
                        desc.setDescription(defn.description());
                    }
                }

                // When either URL or Standard properties are specified in DD, annotation values
                // (of URL or standard properties) are ignored.
                // DD values will win as either of URL or standard properties will be present
                // most of the times.
                // Only when neither URL nor standard properties are not present, annotation
                // values are considered.
                // In such case, standard properties take precedence over URL.

                // try only when URL is not set
                if (!desc.isServerNameSet() && desc.getUrl() == null) {
                    // localhost is the default value (even in the descriptor)
                    if (defn.serverName() != null && !defn.serverName().equals("localhost")) {
                        desc.setServerName(defn.serverName());
                    }
                }

                // try only when URL is not set
                if (desc.getPortNumber() == -1 && desc.getUrl() == null) {
                    if (defn.portNumber() != -1) {
                        desc.setPortNumber(defn.portNumber());
                    }
                }

                // try only when URL is not set
                if (desc.getDatabaseName() == null && desc.getUrl() == null) {
                    if (defn.databaseName() != null && !defn.databaseName().isEmpty()) {
                        desc.setDatabaseName(defn.databaseName());
                    }
                }

                // try only when URL or standard properties are not set
                if (desc.getUrl() == null
                        && !(desc.getPortNumber() != -1 && desc.getServerName() != null && (desc.getDatabaseName() != null))) {
                    if (defn.url() != null && !defn.url().isEmpty()) {
                        desc.setUrl(defn.url());
                    }

                }

                if (desc.getUser() == null) {
                    if (defn.user() != null && !defn.user().isEmpty()) {
                        desc.setUser(defn.user());
                    }
                }

                if (desc.getPassword() == null) {
                    if (defn.password() != null /* ALLOW EMPTY PASSWORDS && !defn.password().isEmpty() */) {
                        desc.setPassword(defn.password());
                    }
                }

                if (desc.getIsolationLevel() == -1) {
                    if (defn.isolationLevel() != -1) {
                        desc.setIsolationLevel(String.valueOf(defn.isolationLevel()));
                    }
                }

                if (!desc.isTransactionSet()) {
                    if (defn.transactional()) {
                        desc.setTransactional(true);
                    } else {
                        desc.setTransactional(false);
                    }
                }

                if (desc.getMinPoolSize() == -1) {
                    if (defn.minPoolSize() != -1) {
                        desc.setMinPoolSize(defn.minPoolSize());
                    }
                }

                if (desc.getMaxPoolSize() == -1) {
                    if (defn.maxPoolSize() != -1) {
                        desc.setMaxPoolSize(defn.maxPoolSize());
                    }
                }

                if (desc.getInitialPoolSize() == -1) {
                    if (defn.initialPoolSize() != -1) {
                        desc.setInitialPoolSize(defn.initialPoolSize());
                    }
                }

                if (desc.getMaxIdleTime() == -1) {
                    if (defn.maxIdleTime() != -1) {
                        desc.setMaxIdleTime(String.valueOf(defn.maxIdleTime()));
                    }
                }

                if (desc.getMaxStatements() == -1) {
                    if (defn.maxStatements() != -1) {
                        desc.setMaxStatements(defn.maxStatements());
                    }
                }

                if (!desc.isLoginTimeoutSet()) {
                    if (defn.loginTimeout() != 0) {
                        desc.setLoginTimeout(String.valueOf(defn.loginTimeout()));
                    }
                }

                Properties properties = desc.getProperties();
                String[] defnProperties = defn.properties();

                if (defnProperties.length > 0) {
                    for (String property : defnProperties) {
                        int index = property.indexOf("=");
                        // found "=" and not at start or end of string
                        if (index > -1 && index != 0 && index < property.length() - 1) {
                            String name = property.substring(0, index);
                            String value = property.substring(index + 1);
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

    private DataSourceDefinitionDescriptor createDescriptor(DataSourceDefinition defn) {

        DataSourceDefinitionDescriptor desc = new DataSourceDefinitionDescriptor();
        desc.setMetadataSource(MetadataSource.ANNOTATION);

        desc.setName(defn.name());
        desc.setClassName(defn.className());

        if (defn.description() != null && !defn.description().isEmpty()) {
            desc.setDescription(defn.description());
        }

        if (defn.serverName() != null && !defn.serverName().equals("localhost")) {
            desc.setServerName(defn.serverName());
        }

        if (defn.portNumber() != -1) {
            desc.setPortNumber(defn.portNumber());
        }

        if (defn.databaseName() != null && !defn.databaseName().isEmpty()) {
            desc.setDatabaseName(defn.databaseName());
        }

        if ((desc.getPortNumber() != -1 && desc.getDatabaseName() != null && desc.getServerName() != null)) {
            // standard properties are set, ignore URL
        } else {
            if (defn.url() != null && !defn.url().isEmpty()) {
                desc.setUrl(defn.url());
            }
        }

        if (defn.user() != null && !defn.user().isEmpty()) {
            desc.setUser(defn.user());
        }

        if (defn.password() != null /* ALLOW EMPTY PASSWORDS && !defn.password().isEmpty() */) {
            desc.setPassword(defn.password());
        }

        if (defn.isolationLevel() != -1) {
            desc.setIsolationLevel(String.valueOf(defn.isolationLevel()));
        }

        if (defn.transactional()) {
            desc.setTransactional(true);
        } else {
            desc.setTransactional(false);
        }

        if (defn.minPoolSize() != -1) {
            desc.setMinPoolSize(defn.minPoolSize());
        }

        if (defn.maxPoolSize() != -1) {
            desc.setMaxPoolSize(defn.maxPoolSize());
        }
        if (defn.initialPoolSize() != -1) {
            desc.setInitialPoolSize(defn.initialPoolSize());
        }
        if (defn.maxIdleTime() != -1) {
            desc.setMaxIdleTime(String.valueOf(defn.maxIdleTime()));
        }

        if (defn.maxStatements() != -1) {
            desc.setMaxStatements(defn.maxStatements());
        }

        if (defn.loginTimeout() != 0) {
            desc.setLoginTimeout(String.valueOf(defn.loginTimeout()));
        }

        if (defn.properties() != null) {
            Properties properties = desc.getProperties();

            String[] defnProperties = defn.properties();
            if (defnProperties.length > 0) {
                for (String property : defnProperties) {
                    int index = property.indexOf("=");
                    // found "=" and not at start or end of string
                    if (index > -1 && index != 0 && index < property.length() - 1) {
                        String name = property.substring(0, index);
                        String value = property.substring(index + 1);
                        properties.put(name, value);
                    }
                }
            }
        }

        return desc;
    }
}
