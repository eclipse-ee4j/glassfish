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

package org.glassfish.web.deployment.descriptor;

import com.sun.enterprise.deployment.AdministeredObjectDefinitionDescriptor;
import com.sun.enterprise.deployment.ConnectionFactoryDefinitionDescriptor;
import com.sun.enterprise.deployment.DataSourceDefinitionDescriptor;
import com.sun.enterprise.deployment.EjbReferenceDescriptor;
import com.sun.enterprise.deployment.EntityManagerFactoryReferenceDescriptor;
import com.sun.enterprise.deployment.EntityManagerReferenceDescriptor;
import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.deployment.JMSConnectionFactoryDefinitionDescriptor;
import com.sun.enterprise.deployment.JMSDestinationDefinitionDescriptor;
import com.sun.enterprise.deployment.JndiNameEnvironment;
import com.sun.enterprise.deployment.MailSessionDescriptor;
import com.sun.enterprise.deployment.MessageDestinationReferenceDescriptor;
import com.sun.enterprise.deployment.ResourceDescriptor;
import com.sun.enterprise.deployment.ResourceEnvReferenceDescriptor;
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.enterprise.deployment.ServiceReferenceDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.WebComponentDescriptor;
import com.sun.enterprise.deployment.web.LoginConfiguration;
import com.sun.enterprise.deployment.web.MimeMapping;
import com.sun.enterprise.deployment.web.SecurityConstraint;
import com.sun.enterprise.deployment.web.ServletFilter;
import java.util.Collections;
import java.util.Enumeration;

import java.util.Set;
import org.glassfish.api.deployment.archive.ReadableArchive;

import org.glassfish.deployment.common.JavaEEResourceType;

/**
 * I am an object that represents all the deployment information about
 * a web fragment.
 *
 * @author Shing Wai Chan
 */
public class WebFragmentDescriptor extends WebBundleDescriptorImpl {

    private static final long serialVersionUID = 1L;
    private String jarName;
    private OrderingDescriptor ordering;

    private boolean conflictLoginConfig;
    private boolean conflictDataSourceDefinition;
    private boolean conflictMailSessionDefinition;
    private boolean conflictConnectionFactoryDefinition;
    private boolean conflictAdminObjectDefinition;
    private boolean conflictJMSConnectionFactoryDefinition;
    private boolean conflictJMSDestinationDefinition;
    private boolean conflictEnvironmentEntry;
    private boolean conflictEjbReference;
    private boolean conflictServiceReference;
    private boolean conflictResourceReference;
    private boolean conflictResourceEnvReference;
    private boolean conflictMessageDestinationReference;
    private boolean conflictEntityManagerReference;
    private boolean conflictEntityManagerFactoryReference;

    /**
     * Constrct an empty web app [{0}].
     */
    public WebFragmentDescriptor() {
        super();
    }

    public String getJarName() {
        return jarName;
    }

    public void setJarName(String jarName) {
        this.jarName = jarName;
    }

    public OrderingDescriptor getOrderingDescriptor() {
        return ordering;
    }


    public void setOrderingDescriptor(OrderingDescriptor ordering) {
        this.ordering = ordering;
    }


    public boolean isConflictLoginConfig() {
        return conflictLoginConfig;
    }


    public boolean isConflictDataSourceDefinition() {
        return conflictDataSourceDefinition;
    }


    public boolean isConflictMailSessionDefinition() {
        return conflictMailSessionDefinition;
    }


    public boolean isConflictConnectionFactoryDefinition() {
        return conflictConnectionFactoryDefinition;
    }


    public boolean isConflictAdminObjectDefinition() {
        return conflictAdminObjectDefinition;
    }


    public boolean isConflictJMSConnectionFactoryDefinition() {
        return conflictJMSConnectionFactoryDefinition;
    }


    public boolean isConflictJMSDestinationDefinition() {
        return conflictJMSDestinationDefinition;
    }


    public boolean isConflictEnvironmentEntry() {
        return conflictEnvironmentEntry;
    }


    public boolean isConflictEjbReference() {
        return conflictEjbReference;
    }


    public boolean isConflictServiceReference() {
        return conflictServiceReference;
    }


    public boolean isConflictResourceReference() {
        return conflictResourceReference;
    }


    public boolean isConflictResourceEnvReference() {
        return conflictResourceEnvReference;
    }


    public boolean isConflictMessageDestinationReference() {
        return conflictMessageDestinationReference;
    }


    public boolean isConflictEntityManagerReference() {
        return conflictEntityManagerReference;
    }


    public boolean isConflictEntityManagerFactoryReference() {
        return conflictEntityManagerFactoryReference;
    }


    @Override
    protected void combineSecurityConstraints(Set<SecurityConstraint> firstScSet, Set<SecurityConstraint> secondScSet) {
        firstScSet.addAll(secondScSet);
    }


    @Override
    protected void combineServletFilters(WebBundleDescriptor webBundleDescriptor) {
        for (ServletFilter servletFilter : webBundleDescriptor.getServletFilters()) {
            ServletFilterDescriptor servletFilterDesc = (ServletFilterDescriptor) servletFilter;
            String name = servletFilter.getName();
            ServletFilterDescriptor aServletFilterDesc = null;
            for (ServletFilter sf : getServletFilters()) {
                if (name.equals(sf.getName())) {
                    aServletFilterDesc = (ServletFilterDescriptor)sf;
                    break;
                }
            }

            if (aServletFilterDesc != null) {
                if (aServletFilterDesc.isConflict(servletFilterDesc)) {
                    aServletFilterDesc.setConflict(true);
                }
            } else {
                getServletFilters().add(servletFilterDesc);
            }
        }
    }


    @Override
    protected void combineServletFilterMappings(WebBundleDescriptor webBundleDescriptor) {
        getServletFilterMappings().addAll(webBundleDescriptor.getServletFilterMappings());
    }


    @Override
    protected void combineLoginConfiguration(WebBundleDescriptor webBundleDescriptor) {
        if (getLoginConfiguration() == null) {
            setLoginConfiguration(webBundleDescriptor.getLoginConfiguration());
        } else {
            LoginConfiguration lgConf = webBundleDescriptor.getLoginConfiguration();
            if (lgConf != null && !lgConf.equals(getLoginConfiguration())) {
                conflictLoginConfig = true;
            }
        }
    }


    @Override
    protected void combinePostConstructDescriptors(WebBundleDescriptor webBundleDescriptor) {
        getPostConstructDescriptors().addAll(webBundleDescriptor.getPostConstructDescriptors());
    }


    @Override
    protected void combinePreDestroyDescriptors(WebBundleDescriptor webBundleDescriptor) {
        getPreDestroyDescriptors().addAll(webBundleDescriptor.getPreDestroyDescriptors());
    }


    @Override
    protected void combineEnvironmentEntries(JndiNameEnvironment env) {
        for (EnvironmentProperty enve : env.getEnvironmentProperties()) {
            EnvironmentProperty envProp = findEnvironmentEntryByName(enve.getName());
            if (envProp == null) {
                addEnvironmentEntry(enve);
            } else {
                if (envProp.isConflict(enve)) {
                    conflictEnvironmentEntry = true;
                }
                combineInjectionTargets(envProp, enve);
            }
        }
    }


    @Override
    protected void combineResourceReferenceDescriptors(JndiNameEnvironment env) {
        for (ResourceReferenceDescriptor resRef : env.getResourceReferenceDescriptors()) {
            ResourceReferenceDescriptor rrd = findResourceReferenceByName(resRef.getName());
            if (rrd == null) {
                addResourceReferenceDescriptor(resRef);
            } else {
                if (resRef.isConflict(rrd)) {
                    conflictResourceReference = true;
                }
                combineInjectionTargets(rrd, resRef);
            }
        }
    }


    @Override
    protected void combineEjbReferenceDescriptors(JndiNameEnvironment env) {
        for (EjbReferenceDescriptor ejbRef : env.getEjbReferenceDescriptors()) {
            EjbReferenceDescriptor ejbRefDesc = findEjbReference(ejbRef.getName());
            if (ejbRefDesc == null) {
                addEjbReferenceDescriptor(ejbRef);
            } else {
                if (ejbRefDesc.isConflict(ejbRef)) {
                    conflictEjbReference = true;
                }
                combineInjectionTargets(ejbRefDesc, ejbRef);
            }
        }
    }


    @Override
    protected void combineServiceReferenceDescriptors(JndiNameEnvironment env) {
        for (ServiceReferenceDescriptor serviceRef : env.getServiceReferenceDescriptors()) {
            ServiceReferenceDescriptor sr = findServiceReferenceByName(serviceRef.getName());
            if (sr == null) {
                addServiceReferenceDescriptor(serviceRef);
            } else {
                if (sr.isConflict(serviceRef)) {
                    conflictServiceReference = true;
                }
                combineInjectionTargets(sr, serviceRef);
            }
        }
    }


    @Override
    protected void combineResourceEnvReferenceDescriptors(JndiNameEnvironment env) {
        for (ResourceEnvReferenceDescriptor jdRef : env.getResourceEnvReferenceDescriptors()) {
            ResourceEnvReferenceDescriptor jdr = findResourceEnvReferenceByName(jdRef.getName());
            if (jdr == null) {
                addResourceEnvReferenceDescriptor(jdRef);
            } else {
                if (jdr.isConflict(jdRef)) {
                    conflictResourceEnvReference = true;
                }
                combineInjectionTargets(jdr, jdRef);
            }
        }
    }


    @Override
    protected void combineMessageDestinationReferenceDescriptors(JndiNameEnvironment env) {
        for (MessageDestinationReferenceDescriptor mdRef : env.getMessageDestinationReferenceDescriptors()) {
            MessageDestinationReferenceDescriptor mdr = findMessageDestinationReferenceByName(mdRef.getName());
            if (mdr == null) {
                addMessageDestinationReferenceDescriptor(mdRef);
            } else {
                if (mdr.isConflict(mdRef)) {
                    conflictMessageDestinationReference = true;
                }
                combineInjectionTargets(mdr, mdRef);
            }
        }
    }


    @Override
    protected void combineEntityManagerReferenceDescriptors(JndiNameEnvironment env) {
        for (EntityManagerReferenceDescriptor emRef : env.getEntityManagerReferenceDescriptors()) {
            EntityManagerReferenceDescriptor emr = findEntityManagerReferenceByName(emRef.getName());
            if (emr == null) {
                addEntityManagerReferenceDescriptor(emRef);
            } else {
                if (emr.isConflict(emRef)) {
                    conflictEntityManagerReference = true;
                }
                combineInjectionTargets(emr, emRef);
            }
        }
    }


    @Override
    protected void combineEntityManagerFactoryReferenceDescriptors(JndiNameEnvironment env) {
        for (EntityManagerFactoryReferenceDescriptor emfRef : env.getEntityManagerFactoryReferenceDescriptors()) {
            EntityManagerFactoryReferenceDescriptor emfr = findEntityManagerFactoryReferenceByName(emfRef.getName());
            if (emfr == null) {
                addEntityManagerFactoryReferenceDescriptor(emfRef);
            } else {
                if (emfr.isConflict(emfRef)) {
                    conflictEntityManagerFactoryReference = true;
                }
                combineInjectionTargets(emfr, emfRef);
            }
        }
    }


    @Override
    protected WebComponentDescriptor combineWebComponentDescriptor(WebComponentDescriptor webComponentDescriptor) {
        final WebComponentDescriptor webCompDesc = getWebComponentByCanonicalName(
            webComponentDescriptor.getCanonicalName());
        final WebComponentDescriptor resultDesc;
        if (webCompDesc == null) {
            resultDesc = webComponentDescriptor;
            this.getWebComponentDescriptors().add(webComponentDescriptor);
        } else {
            resultDesc = webCompDesc;
            if (webCompDesc.isConflict(webComponentDescriptor, false)) {
                webCompDesc.setConflict(true);
            } else {
                // combine the contents of the given one to this one
                webCompDesc.add(webComponentDescriptor, true, true);
            }
        }
        return resultDesc;
    }


    @Override
    protected void combineResourceDescriptors(JndiNameEnvironment env, JavaEEResourceType javaEEResourceType) {
        for (ResourceDescriptor ddd : env.getResourceDescriptors(javaEEResourceType)) {
            ResourceDescriptor descriptor = getResourceDescriptor(javaEEResourceType, ddd.getName());
            if (descriptor == null) {
                getResourceDescriptors(javaEEResourceType).add(ddd);
            } else {
                if (descriptor.getResourceType().equals(JavaEEResourceType.DSD)
                    && ((DataSourceDefinitionDescriptor) descriptor).isConflict((DataSourceDefinitionDescriptor) ddd)) {
                    conflictDataSourceDefinition = true;
                } else if (descriptor.getResourceType().equals(JavaEEResourceType.MSD)
                    && ((MailSessionDescriptor) descriptor).isConflict((MailSessionDescriptor) ddd)) {
                    conflictMailSessionDefinition = true;
                } else if (descriptor.getResourceType().equals(JavaEEResourceType.AODD)
                    && ((AdministeredObjectDefinitionDescriptor) descriptor)
                    .isConflict((AdministeredObjectDefinitionDescriptor) ddd)) {
                    conflictAdminObjectDefinition = true;
                } else if (descriptor.getResourceType().equals(JavaEEResourceType.CFD)
                    && ((ConnectionFactoryDefinitionDescriptor) descriptor)
                    .isConflict((ConnectionFactoryDefinitionDescriptor) ddd)) {
                    conflictConnectionFactoryDefinition = true;
                } else if (descriptor.getResourceType().equals(JavaEEResourceType.JMSCFDD)
                    && ((JMSConnectionFactoryDefinitionDescriptor) descriptor)
                    .isConflict((JMSConnectionFactoryDefinitionDescriptor) ddd)) {
                    conflictJMSConnectionFactoryDefinition = true;
                } else if (descriptor.getResourceType().equals(JavaEEResourceType.JMSDD)
                    && ((JMSDestinationDefinitionDescriptor) descriptor)
                    .isConflict((JMSDestinationDefinitionDescriptor) ddd)) {
                    conflictJMSDestinationDefinition = true;
                }
            }
        }
    }


    @Override
    protected void combineMimeMappings(Set<MimeMapping> mimeMappings) {
        // do not call getMimeMappingsSet().addAll() as there is special overriding rule
        for (MimeMapping mimeMap : mimeMappings) {
            if (!mimeMap.getMimeType().equals(addMimeMapping(mimeMap))) {
                getConflictedMimeMappingExtensions().add(mimeMap.getExtension());
            }
        }
    }


    @Override
    public void print(StringBuffer toStringBuffer) {
        super.print(toStringBuffer);
        if (jarName != null) {
            toStringBuffer.append("\njar name " + jarName);
        }
        if (ordering != null) {
            toStringBuffer.append("\nordering " + ordering);
        }
    }

    @Override
    public Enumeration<String> getArchiveFileEntries(ReadableArchive archiveFile) {
        return Collections.emptyEnumeration();
    }

}
