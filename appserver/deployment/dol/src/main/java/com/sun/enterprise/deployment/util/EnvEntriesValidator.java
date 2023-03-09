/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.deployment.util;

import com.sun.enterprise.deployment.EjbReferenceDescriptor;
import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.deployment.JndiNameEnvironment;
import com.sun.enterprise.deployment.ResourceEnvReferenceDescriptor;
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.types.EjbReference;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA;
import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_APP;
import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_COMPONENT;
import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_COMPONENT_ENV;
import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_MODULE;

public class EnvEntriesValidator {
    private static final Logger LOG = DOLUtils.getDefaultLogger();

    private final Map<String, Map<String, Object>> componentNamespaces;
    private final Map<String, Map<String, Object>> appNamespaces;
    private final Map<AppModuleKey, Map<String, Object>> moduleNamespaces;
    private final Map<String, Object> globalNameSpace;

    public EnvEntriesValidator() {
        componentNamespaces = new HashMap<>();
        appNamespaces = new HashMap<>();
        moduleNamespaces = new HashMap<>();
        globalNameSpace = new HashMap<>();
    }

    public void validateEnvEntries(JndiNameEnvironment env) {
        LOG.log(Level.FINER, "validateEnvEntries: {0}", env);
        if (env instanceof WebBundleDescriptor) {
            Set<EnvironmentProperty> envEntries = ((WebBundleDescriptor) env).getEnvironmentEntrySet();
            validateSimpleEnvEntries(env, envEntries);
        } else {
            Set<EnvironmentProperty> envProperties = env.getEnvironmentProperties();
            validateSimpleEnvEntries(env, envProperties);
        }
        Set<EjbReferenceDescriptor> ejbReferences = env.getEjbReferenceDescriptors();
        validateEjbReferences(env, ejbReferences);
        Set<ResourceReferenceDescriptor> resRefs = env.getResourceReferenceDescriptors();
        validateResRefs(env, resRefs);
        Set<ResourceEnvReferenceDescriptor> resEnvRefs = env.getResourceEnvReferenceDescriptors();
        validateResEnvRefs(env, resEnvRefs);
    }


    private void validateSimpleEnvEntries(JndiNameEnvironment env, Set<EnvironmentProperty> envEntries) {
        for (EnvironmentProperty environmentProperty : envEntries) {
            SimpleEnvEntry simpleEnvEntry = new SimpleEnvEntry(environmentProperty);
            validateEnvEntry(env, simpleEnvEntry, simpleEnvEntry.getName());
        }
    }


    private void validateEjbReferences(JndiNameEnvironment env, Set<EjbReferenceDescriptor> ejbReferences) {
        for (EjbReference ejbRef : ejbReferences) {
            validateEnvEntry(env, ejbRef, ejbRef.getName());
        }
    }


    private void validateResRefs(JndiNameEnvironment env, Set<ResourceReferenceDescriptor> resRefs) {
        for (ResourceReferenceDescriptor resRef : resRefs) {
            validateEnvEntry(env, resRef, resRef.getName());
        }
    }


    private void validateResEnvRefs(JndiNameEnvironment env, Set<ResourceEnvReferenceDescriptor> resEnvRefs) {
        for (ResourceEnvReferenceDescriptor resEnvRef : resEnvRefs) {
            validateEnvEntry(env, resEnvRef, resEnvRef.getName());
        }
    }


    private void validateEnvEntry(JndiNameEnvironment env, Object curEntry, String name) {
        final String logicalJndiName = getLogicalJNDIName(name, env);
        final Map<String, Object> namespace = getNamespace(logicalJndiName, env);
        final Object preObject = namespace.get(logicalJndiName);
        LOG.log(Level.FINE, "Validating logical name: {0}, cached object: {1}, validated object: {2}",
            new Object[] {logicalJndiName, preObject, curEntry});
        if (preObject == null) {
            namespace.put(logicalJndiName, curEntry);
        } else {
            if (preObject instanceof SimpleEnvEntry && curEntry instanceof SimpleEnvEntry) {
                SimpleEnvEntry preEnvEntry = (SimpleEnvEntry) preObject;
                SimpleEnvEntry curEnvEntry = (SimpleEnvEntry) curEntry;
                if (areConflicting(preEnvEntry.getType(), curEnvEntry.getType())
                    || areConflicting(preEnvEntry.getValue(), curEnvEntry.getValue())) {
                    throwConflictException(name, namespace.toString());
                }
            } else if (preObject instanceof EjbReference && curEntry instanceof EjbReference) {
                EjbReference preRef = (EjbReference) preObject;
                EjbReference curRef = (EjbReference) curEntry;
                if (areConflicting(preRef.getType(), curRef.getType())
                    || areConflicting(preRef.getEjbHomeInterface(), curRef.getEjbHomeInterface())
                    || areConflicting(preRef.getEjbInterface(), curRef.getEjbInterface())
                    // link name is optional. compare only when they are both not null.
                    || (preRef.getLinkName() != null && curRef.getLinkName() != null
                        && !preRef.getLinkName().equals(curRef.getLinkName()))
                    || (preRef.isLocal() != curRef.isLocal())
                    || areConflicting(preRef.getLookupName(), curRef.getLookupName())) {
                    throwConflictException(name, namespace.toString());
                }
            } else if (preObject instanceof ResourceReferenceDescriptor
                && curEntry instanceof ResourceReferenceDescriptor) {
                ResourceReferenceDescriptor preRef = (ResourceReferenceDescriptor) preObject;
                ResourceReferenceDescriptor curRef = (ResourceReferenceDescriptor) curEntry;
                if (areConflicting(preRef.getType(), curRef.getType())
                    || areConflicting(preRef.getAuthorization(), curRef.getAuthorization())
                    || areConflicting(preRef.getSharingScope(), curRef.getSharingScope())
                    || areConflicting(preRef.getMappedName(), curRef.getMappedName())
                    || areConflicting(preRef.getLookupName(), curRef.getLookupName())) {
                    throwConflictException(name, namespace.toString());
                }
            } else if (preObject instanceof ResourceEnvReferenceDescriptor
                && curEntry instanceof ResourceEnvReferenceDescriptor) {
                ResourceEnvReferenceDescriptor preRef = (ResourceEnvReferenceDescriptor) preObject;
                ResourceEnvReferenceDescriptor curRef = (ResourceEnvReferenceDescriptor) curEntry;
                if (areConflicting(preRef.getType(), curRef.getType())
                    || areConflicting(preRef.getRefType(), curRef.getRefType())
                    || areConflicting(preRef.getMappedName(), curRef.getMappedName())
                    || areConflicting(preRef.getLookupName(), curRef.getLookupName())) {
                    throwConflictException(name, namespace.toString());
                }
            } else {
                throwConflictException(name, namespace.toString());
            }
        }

    }

    private Map<String, Object> getNamespace(String logicalJndiName, JndiNameEnvironment env) {
        final String appName = DOLUtils.getApplicationName(env);
        LOG.log(Level.FINE, "appName={0}", appName);
        if (logicalJndiName.startsWith(JNDI_CTX_JAVA_COMPONENT)) {
            String componentId = DOLUtils.getComponentEnvId(env);
            LOG.log(Level.FINEST, "Resolved componentId: {0}", componentId);
            Map<String, Object> namespace = componentNamespaces.get(componentId);
            if (namespace == null) {
                namespace = new HashMap<>();
                componentNamespaces.put(componentId, namespace);
            }
            return namespace;
        } else if (logicalJndiName.startsWith(JNDI_CTX_JAVA_MODULE)) {
            String moduleName = DOLUtils.getModuleName(env);
            AppModuleKey appModuleKey = new AppModuleKey(appName, moduleName);
            Map<String, Object> namespace = moduleNamespaces.get(appModuleKey);
            if (namespace == null) {
                namespace = new HashMap<>();
                moduleNamespaces.put(appModuleKey, namespace);
            }
            return namespace;
        } else if (logicalJndiName.startsWith(JNDI_CTX_JAVA_APP)) {
            Map<String, Object> namespace = appNamespaces.get(appName);
            if (namespace == null) {
                namespace = new HashMap<>();
                appNamespaces.put(appName, namespace);
            }
            return namespace;
        } else {
            // java:global
            return globalNameSpace;
        }
    }


    private void throwConflictException(String jndiName, String namespace) {
        throw new IllegalStateException("Naming binding already exists for " + jndiName + " in namespace " + namespace);
    }


    /**
     * @return similar to {@link Objects#equals(Object)} but uses equals of both objects.
     */
    private <T> boolean areConflicting(T s1, T s2) {
        LOG.log(Level.FINEST, "areConflicting? {0} ||| {1}", new Object[] {s1, s2});
        return (s1 != null && !s1.equals(s2)) || (s2 != null && !s2.equals(s1));
    }


    /**
     * If no java: prefix is specified, default to component scope.
     */
    private String rawNameToLogicalJndiName(String rawName) {
        return rawName.startsWith(JNDI_CTX_JAVA) ? rawName : JNDI_CTX_JAVA_COMPONENT_ENV + rawName;
    }


    /**
     * convert name from java:comp/xxx to java:module/xxx
     */
    private String logicalCompJndiNameToModule(String logicalCompName) {
        String tail = logicalCompName.substring(JNDI_CTX_JAVA_COMPONENT.length());
        return JNDI_CTX_JAVA_MODULE + tail;
    }


    private String getLogicalJNDIName(String name, JndiNameEnvironment env) {
        String logicalJndiName = rawNameToLogicalJndiName(name);
        boolean treatComponentAsModule = DOLUtils.getTreatComponentAsModule(env);
        if (treatComponentAsModule && logicalJndiName.startsWith(JNDI_CTX_JAVA_COMPONENT)) {
            logicalJndiName = logicalCompJndiNameToModule(logicalJndiName);
        }
        return logicalJndiName;
    }

    private static class AppModuleKey {

        private final String app;
        private final String module;

        AppModuleKey(String appName, String moduleName) {
            app = appName;
            module = moduleName;
        }


        @Override
        public boolean equals(Object o) {
            if (o instanceof AppModuleKey) {
                AppModuleKey other = (AppModuleKey) o;
                if (app.equals(other.app) && module.equals(other.module)) {
                    return true;
                }
            }
            return false;
        }


        @Override
        public int hashCode() {
            return Objects.hash(app, module);
        }


        @Override
        public String toString() {
            return "appName = " + app + " , module = " + module;
        }
    }

    private static class SimpleEnvEntry extends EnvironmentProperty {

        private static final long serialVersionUID = 1L;

        SimpleEnvEntry(EnvironmentProperty envEntry) {
            super(envEntry.getName(), envEntry.getValue(), envEntry.getDescription(), envEntry.getType());
        }
    }
}
