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

import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.EjbReferenceDescriptor;
import com.sun.enterprise.deployment.EntityManagerFactoryReferenceDescriptor;
import com.sun.enterprise.deployment.EntityManagerReferenceDescriptor;
import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.deployment.ErrorPageDescriptor;
import com.sun.enterprise.deployment.InjectionTarget;
import com.sun.enterprise.deployment.JndiNameEnvironment;
import com.sun.enterprise.deployment.JspConfigDefinitionDescriptor;
import com.sun.enterprise.deployment.LifecycleCallbackDescriptor;
import com.sun.enterprise.deployment.MessageDestinationReferenceDescriptor;
import com.sun.enterprise.deployment.MetadataSource;
import com.sun.enterprise.deployment.OrderedSet;
import com.sun.enterprise.deployment.PersistenceUnitDescriptor;
import com.sun.enterprise.deployment.ResourceDescriptor;
import com.sun.enterprise.deployment.ResourceEnvReferenceDescriptor;
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.enterprise.deployment.ServiceReferenceDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.WebComponentDescriptor;
import com.sun.enterprise.deployment.WebService;
import com.sun.enterprise.deployment.WebServicesDescriptor;
import com.sun.enterprise.deployment.runtime.web.SunWebApp;
import com.sun.enterprise.deployment.util.ComponentPostVisitor;
import com.sun.enterprise.deployment.util.ComponentVisitor;
import com.sun.enterprise.deployment.web.MimeMapping;
import com.sun.enterprise.deployment.web.SecurityConstraint;
import com.sun.enterprise.deployment.web.ServletFilter;
import com.sun.enterprise.deployment.web.ServletFilterMapping;
import com.sun.enterprise.deployment.web.WebResourceCollection;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.glassfish.api.deployment.archive.ReadableArchive;

import org.glassfish.deployment.common.DescriptorVisitor;
import org.glassfish.deployment.common.JavaEEResourceType;
import org.glassfish.deployment.common.RootDeploymentDescriptor;
import org.glassfish.web.deployment.node.WebCommonNode;
import org.glassfish.web.deployment.runtime.SunWebAppImpl;
import org.glassfish.web.deployment.util.WebBundleTracerVisitor;
import org.glassfish.web.deployment.util.WebBundleValidator;
import org.glassfish.web.deployment.util.WebBundleVisitor;

/**
 * Descriptor for a web application
 */
public class WebBundleDescriptorImpl extends WebBundleDescriptor {

    private static final long serialVersionUID = 1L;

    private AbsoluteOrderingDescriptor absOrdering;
    private SunWebApp sunWebApp;

    private final Set<String> conflictedMimeMappingExtensions = new OrderedSet<>();

    /**
     * Construct an empty web app [{0}].
     */
    public WebBundleDescriptorImpl() {
    }


    @Override
    public ComponentVisitor getBundleVisitor() {
        return new WebBundleValidator();
    }


    @Override
    public String getDefaultSpecVersion() {
        return WebCommonNode.SPEC_VERSION;
    }


    public AbsoluteOrderingDescriptor getAbsoluteOrderingDescriptor() {
        return absOrdering;
    }


    public void setAbsoluteOrderingDescriptor(AbsoluteOrderingDescriptor absOrdering) {
        this.absOrdering = absOrdering;
    }


    /**
     * @return {@link SunWebApp}, never null.
     */
    @Override
    public SunWebApp getSunDescriptor() {
        if (sunWebApp == null) {
            sunWebApp = new SunWebAppImpl();
        }
        return sunWebApp;
    }


    @Override
    public void setSunDescriptor(SunWebApp webApp) {
        this.sunWebApp = webApp;
    }


    @Override
    public DescriptorVisitor getTracerVisitor() {
        return new WebBundleTracerVisitor();
    }


    @Override
    public void visit(DescriptorVisitor aVisitor) {
        if (aVisitor instanceof WebBundleVisitor || aVisitor instanceof ComponentPostVisitor) {
            visit((ComponentVisitor) aVisitor);
        } else {
            super.visit(aVisitor);
        }
    }


    @Override
    public void visit(ComponentVisitor aVisitor) {
        super.visit(aVisitor);
        aVisitor.accept(this);
    }


    @Override
    public Collection<? extends PersistenceUnitDescriptor> findReferencedPUs() {
        Set<PersistenceUnitDescriptor> pus = new HashSet<>(findReferencedPUsViaPURefs(this));
        pus.addAll(findReferencedPUsViaPCRefs(this));
        if (extensions.containsKey(EjbBundleDescriptor.class)) {
            for (RootDeploymentDescriptor extension : extensions.get(EjbBundleDescriptor.class)) {
                pus.addAll(((EjbBundleDescriptor) extension).findReferencedPUs());
            }
        }
        return pus;
    }


    @Override
    protected void addCommonWebBundleDescriptor(WebBundleDescriptor webBundleDescriptor, boolean descriptorFragment) {
        addBundleDescriptor(webBundleDescriptor);

        for (WebComponentDescriptor webComponentDesc : webBundleDescriptor.getWebComponentDescriptors()) {
            // don't modify the original one
            WebComponentDescriptorImpl webComponentDescriptor = new WebComponentDescriptorImpl(webComponentDesc);
            // set web bundle to null so that the urlPattern2ServletName
            // of the others will not be changed,
            // see WebComponentDescriptor.getUrlPatternsSet()
            webComponentDescriptor.setWebBundleDescriptor(null);

            List<String> removeUrlPatterns = new ArrayList<>();
            for (String urlPattern : webComponentDescriptor.getUrlPatternsSet()) {
                Map<String, String> map = getUrlPatternToServletNameMap(false);
                final String servletName = map == null ? null : map.get(urlPattern);
                if (servletName != null && !servletName.equals(webComponentDescriptor.getCanonicalName())) {
                    // url pattern already exists in current bundle
                    // need to remove the url pattern in current bundle servlet
                    removeUrlPatterns.add(urlPattern);
                }
            }

            webComponentDescriptor.getUrlPatternsSet().removeAll(removeUrlPatterns);
            addWebComponentDescriptor(webComponentDescriptor);
        }

        addContextParameters(webBundleDescriptor.getContextParameters());

        if (webBundleDescriptor instanceof WebBundleDescriptorImpl) {
            addConflictedMimeMappingExtensions(
                ((WebBundleDescriptorImpl) webBundleDescriptor).getConflictedMimeMappingExtensions());
        }
        combineMimeMappings(webBundleDescriptor.getMimeMappings());

        // do not call getErrorPageDescriptorsSet.addAll() as there is special overriding rule
        for (ErrorPageDescriptor errPageDesc : webBundleDescriptor.getErrorPageDescriptors()) {
            addErrorPageDescriptor(errPageDesc);
        }
        addAppListeners(webBundleDescriptor.getAppListeners());

        if (webBundleDescriptor.isDenyUncoveredHttpMethods()) {
            setDenyUncoveredHttpMethods(true);
        }
        combineSecurityConstraints(getSecurityConstraints(), webBundleDescriptor.getSecurityConstraints());

        combineServletFilters(webBundleDescriptor);
        combineServletFilterMappings(webBundleDescriptor);

        if (getLocaleEncodingMappingListDescriptor() == null) {
            setLocaleEncodingMappingListDescriptor(webBundleDescriptor.getLocaleEncodingMappingListDescriptor());
        }

        if (webBundleDescriptor.getJspConfigDescriptor() != null) {
            JspConfigDefinitionDescriptor jspConfigDesc = getJspConfigDescriptor();
            if (jspConfigDesc == null) {
                setJspConfigDescriptor(new JspConfigDefinitionDescriptor());
            }
            addJspDescriptor(webBundleDescriptor.getJspConfigDescriptor());
        }

        // WebServices
        WebServicesDescriptor thisWebServices = getWebServices();
        WebServicesDescriptor otherWebServices = webBundleDescriptor.getWebServices();
        for (WebService ws : otherWebServices.getWebServices()) {
            thisWebServices.addWebService(new WebService(ws));
        }

        if (getSessionConfig() == null) {
            setSessionConfig(webBundleDescriptor.getSessionConfig());
        }

        // combine login config with conflict resolution check
        combineLoginConfiguration(webBundleDescriptor);

        if (descriptorFragment) {
            boolean otherDistributable = webBundleDescriptor.isDistributable();
            setDistributable(isDistributable() && otherDistributable);
        }

        combinePostConstructDescriptors(webBundleDescriptor);
        combinePreDestroyDescriptors(webBundleDescriptor);
        addJndiNameEnvironment(webBundleDescriptor);
    }


    protected void combineMimeMappings(Set<MimeMapping> mimeMappings) {
        for (MimeMapping mm : getMimeMappings()) {
            conflictedMimeMappingExtensions.remove(mm.getExtension());
        }
        if (!conflictedMimeMappingExtensions.isEmpty()) {
            throw new IllegalArgumentException(
                "There are more than one Mime mapping defined in web fragments with the same extension.");
        }
        // do not call getMimeMappingsSet().addAll() as there is special overriding rule
        for (MimeMapping mimeMap : mimeMappings) {
            addMimeMapping(mimeMap);
        }
    }


    public Set<String> getConflictedMimeMappingExtensions() {
        return conflictedMimeMappingExtensions;
    }


    protected void addConflictedMimeMappingExtensions(Set<String> conflicted) {
        conflictedMimeMappingExtensions.addAll(conflicted);
    }


    protected void combineSecurityConstraints(Set<SecurityConstraint> firstScSet, Set<SecurityConstraint> secondScSet) {
        Set<String> allUrlPatterns = new HashSet<>();
        for (SecurityConstraint sc : firstScSet) {
            for (WebResourceCollection wrc : sc.getWebResourceCollections()) {
                allUrlPatterns.addAll(wrc.getUrlPatterns());
            }
        }

        for (SecurityConstraint sc : secondScSet) {
            SecurityConstraint newSc = new SecurityConstraintImpl((SecurityConstraintImpl) sc);
            boolean addSc = false;
            Iterator<WebResourceCollection> iter = newSc.getWebResourceCollections().iterator();
            while (iter.hasNext()) {
                WebResourceCollection wrc = iter.next();
                Set<String> urlPatterns = wrc.getUrlPatterns();
                urlPatterns.removeAll(allUrlPatterns);
                boolean isEmpty = urlPatterns.isEmpty();
                addSc = addSc || !isEmpty;
                if (isEmpty) {
                    iter.remove();
                }
            }

            if (addSc) {
                firstScSet.add(newSc);
            }
        }
    }


    protected void combineServletFilters(WebBundleDescriptor webBundleDescriptor) {
        for (ServletFilter servletFilter : webBundleDescriptor.getServletFilters()) {
            ServletFilterDescriptor servletFilterDesc = (ServletFilterDescriptor) servletFilter;
            String name = servletFilter.getName();
            ServletFilterDescriptor aServletFilterDesc = null;
            for (ServletFilter sf : getServletFilters()) {
                if (name.equals(sf.getName())) {
                    aServletFilterDesc = (ServletFilterDescriptor) sf;
                    break;
                }
            }

            if (aServletFilterDesc != null) {
                if (!aServletFilterDesc.isConflict(servletFilterDesc)) {
                    if (aServletFilterDesc.getClassName().isEmpty()) {
                        aServletFilterDesc.setClassName(servletFilter.getClassName());
                    }
                    if (aServletFilterDesc.isAsyncSupported() == null) {
                        aServletFilterDesc.setAsyncSupported(servletFilter.isAsyncSupported());
                    }
                }

                String className = aServletFilterDesc.getClassName();
                if (servletFilterDesc.isConflict() && (className == null || className.isEmpty())) {
                    throw new IllegalArgumentException("Two or more web fragments define the same Filter"
                        + " with conflicting implementation class names that are not overridden by the web.xml");
                }
            } else {
                if (servletFilterDesc.isConflict()) {
                    throw new IllegalArgumentException("One or more web fragments define the same Filter"
                        + " in a conflicting way, and the Filter is not defined in web.xml");
                }
                getServletFilters().add(servletFilterDesc);
            }
        }
    }


    protected void combineServletFilterMappings(WebBundleDescriptor webBundleDescriptor) {
        Map<String, ServletFilterMappingInfo> map = new HashMap<>();
        for (ServletFilterMapping sfMapping : getServletFilterMappings()) {
            ServletFilterMappingInfo sfmInfo = map.get(sfMapping.getName());
            if (sfmInfo == null) {
                sfmInfo = new ServletFilterMappingInfo();
                sfmInfo.servletFilterMapping = sfMapping;
                map.put(sfMapping.getName(), sfmInfo);
            }
            if (!sfmInfo.hasMapping) {
                sfmInfo.hasMapping = !sfMapping.getServletNames().isEmpty() && !sfMapping.getUrlPatterns().isEmpty();
            }
            if (!sfmInfo.hasDispatcher) {
                sfmInfo.hasDispatcher = !sfMapping.getDispatchers().isEmpty();
            }
        }

        for (ServletFilterMapping sfMapping : webBundleDescriptor.getServletFilterMappings()) {
            ServletFilterMappingInfo sfmInfo = map.get(sfMapping.getName());
            if (sfmInfo != null) {
                if (!sfmInfo.hasMapping) {
                    sfmInfo.servletFilterMapping.getServletNames().addAll(sfMapping.getServletNames());
                    sfmInfo.servletFilterMapping.getUrlPatterns().addAll(sfMapping.getUrlPatterns());
                }
                if (!sfmInfo.hasDispatcher) {
                    sfmInfo.servletFilterMapping.getDispatchers().addAll(sfMapping.getDispatchers());
                }
            } else {
                addServletFilterMapping(sfMapping);
            }
        }
    }


    protected void combineLoginConfiguration(WebBundleDescriptor webBundleDescriptor) {
        if (getLoginConfiguration() == null) {
            if (webBundleDescriptor instanceof WebFragmentDescriptor) {
                WebFragmentDescriptor fragment = (WebFragmentDescriptor) webBundleDescriptor;
                if (fragment.isConflictLoginConfig()) {
                    throw new IllegalArgumentException(
                        "There are more than one login-config defined in web fragments with different values");
                }
            }
            setLoginConfiguration(webBundleDescriptor.getLoginConfiguration());
        }
    }


    protected void combinePostConstructDescriptors(WebBundleDescriptor webBundleDescriptor) {
        boolean isFromXml = false;
        for (LifecycleCallbackDescriptor lccd : getPostConstructDescriptors()) {
            isFromXml = (lccd.getMetadataSource() == MetadataSource.XML);
            if (isFromXml) {
                break;
            }
        }
        if (!isFromXml) {
            getPostConstructDescriptors().addAll(webBundleDescriptor.getPostConstructDescriptors());
        }
    }


    protected void combinePreDestroyDescriptors(WebBundleDescriptor webBundleDescriptor) {
        boolean isFromXml = false;
        for (LifecycleCallbackDescriptor lccd : getPreDestroyDescriptors()) {
            isFromXml = (lccd.getMetadataSource() == MetadataSource.XML);
            if (isFromXml) {
                break;
            }
        }
        if (!isFromXml) {
            getPreDestroyDescriptors().addAll(webBundleDescriptor.getPreDestroyDescriptors());
        }
    }


    @Override
    public void addJndiNameEnvironment(JndiNameEnvironment env) {
        // combine with conflict resolution check
        combineEnvironmentEntries(env);
        combineResourceReferenceDescriptors(env);
        combineEjbReferenceDescriptors(env);
        combineServiceReferenceDescriptors(env);
        // resource-env-ref
        combineResourceEnvReferenceDescriptors(env);
        combineMessageDestinationReferenceDescriptors(env);
        // persistence-context-ref
        combineEntityManagerReferenceDescriptors(env);
        // persistence-unit-ref
        combineEntityManagerFactoryReferenceDescriptors(env);
        combineAllResourceDescriptors(env);
    }


    protected void combineEnvironmentEntries(JndiNameEnvironment env) {
        for (EnvironmentProperty property : env.getEnvironmentProperties()) {
            EnvironmentProperty entry = findEnvironmentEntryByName(property.getName());
            if (entry != null) {
                combineInjectionTargets(entry, property);
                if (property.isSetValueCalled() && !entry.isSetValueCalled() && !entry.hasInjectionTargetFromXml()) {
                    entry.setValue(property.getValue());
                }
                continue;
            }
            if (env instanceof WebFragmentDescriptor) {
                WebFragmentDescriptor fragment = (WebFragmentDescriptor) env;
                if (fragment.isConflictEnvironmentEntry()) {
                    throw new IllegalArgumentException("There are more than one environment entries defined"
                        + " in web fragments with the same name, but not overrided in web.xml");
                }
            }
            addEnvironmentEntry(property);
        }
    }


    protected void combineResourceReferenceDescriptors(JndiNameEnvironment env) {
        for (ResourceReferenceDescriptor resRef : env.getResourceReferenceDescriptors()) {
            ResourceReferenceDescriptor rrd = findResourceReferenceByName(resRef.getName());
            if (rrd != null) {
                combineInjectionTargets(rrd, resRef);
                continue;
            }
            if (env instanceof WebFragmentDescriptor) {
                WebFragmentDescriptor fragment = (WebFragmentDescriptor) env;
                if (fragment.isConflictResourceReference()) {
                    throw new IllegalArgumentException("There are more than one resource references"
                        + " defined in web fragments with the same name, but not overrided in web.xml");
                }
            }
            addResourceReferenceDescriptor(resRef);
        }
    }


    protected void combineEjbReferenceDescriptors(JndiNameEnvironment env) {
        for (EjbReferenceDescriptor ejbRef : env.getEjbReferenceDescriptors()) {
            EjbReferenceDescriptor ejbRefDesc = findEjbReference(ejbRef.getName());
            if (ejbRefDesc != null) {
                combineInjectionTargets(ejbRefDesc, ejbRef);
                continue;
            }
            if (env instanceof WebFragmentDescriptor) {
                WebFragmentDescriptor fragment = (WebFragmentDescriptor) env;
                if (fragment.isConflictEjbReference()) {
                    throw new IllegalArgumentException("There are more than one ejb references"
                        + " defined in web fragments with the same name, but not overrided in web.xml");
                }
            }
            addEjbReferenceDescriptor(ejbRef);
        }
    }


    protected void combineServiceReferenceDescriptors(JndiNameEnvironment env) {
        for (ServiceReferenceDescriptor serviceRef : env.getServiceReferenceDescriptors()) {
            ServiceReferenceDescriptor sr = findServiceReferenceByName(serviceRef.getName());
            if (sr != null) {
                combineInjectionTargets(sr, serviceRef);
                continue;
            }
            if (env instanceof WebFragmentDescriptor) {
                WebFragmentDescriptor fragment = (WebFragmentDescriptor) env;
                if (fragment.isConflictServiceReference()) {
                    throw new IllegalArgumentException("There are more than one service references defined"
                        + " in web fragments with the same name, but not overrided in web.xml");
                }
            }
            addServiceReferenceDescriptor(serviceRef);
        }
    }


    protected void combineResourceEnvReferenceDescriptors(JndiNameEnvironment env) {
        for (ResourceEnvReferenceDescriptor jdRef: env.getResourceEnvReferenceDescriptors()) {
            ResourceEnvReferenceDescriptor jdr = findResourceEnvReferenceByName(jdRef.getName());
            if (jdr != null) {
                combineInjectionTargets(jdr, jdRef);
                continue;
            }
            if (env instanceof WebFragmentDescriptor) {
                WebFragmentDescriptor fragment = (WebFragmentDescriptor) env;
                if (fragment.isConflictResourceEnvReference()) {
                    throw new IllegalArgumentException("There are more than one resource env references"
                        + " defined in web fragments with the same name, but not overrided in web.xml");
                }
            }
            addResourceEnvReferenceDescriptor(jdRef);
        }
    }


    protected void combineMessageDestinationReferenceDescriptors(JndiNameEnvironment env) {
        for (MessageDestinationReferenceDescriptor mdRef : env.getMessageDestinationReferenceDescriptors()) {
            MessageDestinationReferenceDescriptor mdr = findMessageDestinationReferenceByName(mdRef.getName());
            if (mdr != null) {
                combineInjectionTargets(mdr, mdRef);
                continue;
            }
            if (env instanceof WebFragmentDescriptor) {
                WebFragmentDescriptor fragment = (WebFragmentDescriptor) env;
                if (fragment.isConflictMessageDestinationReference()) {
                    throw new IllegalArgumentException("There are more than one message destination references"
                        + " defined in web fragments with the same name, but not overrided in web.xml");
                }
            }
            addMessageDestinationReferenceDescriptor(mdRef);
        }
    }


    protected void combineEntityManagerReferenceDescriptors(JndiNameEnvironment env) {
        for (EntityManagerReferenceDescriptor emRef : env.getEntityManagerReferenceDescriptors()) {
            EntityManagerReferenceDescriptor emr = findEntityManagerReferenceByName(emRef.getName());
            if (emr != null) {
                combineInjectionTargets(emr, emRef);
                continue;
            }
            if (env instanceof WebFragmentDescriptor) {
                WebFragmentDescriptor fragment = (WebFragmentDescriptor) env;
                if (fragment.isConflictEntityManagerReference()) {
                    throw new IllegalArgumentException("There are more than one persistence context references"
                        + " defined in web fragments with the same name, but not overrided in web.xml");
                }
            }
            addEntityManagerReferenceDescriptor(emRef);
        }
    }


    protected void combineEntityManagerFactoryReferenceDescriptors(JndiNameEnvironment env) {
        for (EntityManagerFactoryReferenceDescriptor emfRef : env.getEntityManagerFactoryReferenceDescriptors()) {
            EntityManagerFactoryReferenceDescriptor emfr = findEntityManagerFactoryReferenceByName(emfRef.getName());
            if (emfr != null) {
                combineInjectionTargets(emfr, emfRef);
                continue;
            }
            if (env instanceof WebFragmentDescriptor) {
                WebFragmentDescriptor fragment = (WebFragmentDescriptor) env;
                if (fragment.isConflictEntityManagerFactoryReference()) {
                    throw new IllegalArgumentException("There are more than one persistence unit references"
                        + " defined in web fragments with the same name, but not overrided in web.xml");
                }
            }
            addEntityManagerFactoryReferenceDescriptor(emfRef);
        }
    }


    private void combineAllResourceDescriptors(JndiNameEnvironment env) {
        for (JavaEEResourceType javaEEResourceType : JavaEEResourceType.values()) {
            combineResourceDescriptors(env, javaEEResourceType);
        }
    }


    protected void combineResourceDescriptors(JndiNameEnvironment env, JavaEEResourceType javaEEResourceType) {
        for (ResourceDescriptor desc : env.getResourceDescriptors(javaEEResourceType)) {
            ResourceDescriptor descriptor = getResourceDescriptor(javaEEResourceType, desc.getName());
            if (descriptor == null) {
                if (env instanceof WebFragmentDescriptor) {
                    WebFragmentDescriptor fragment = (WebFragmentDescriptor) env;
                    if (javaEEResourceType.equals(JavaEEResourceType.AODD)
                        && fragment.isConflictAdminObjectDefinition()) {
                        throw new IllegalArgumentException("There are more than one administered object definitions"
                            + " defined in web fragments with the same name, but not overrided in web.xml");
                    } else if (javaEEResourceType.equals(JavaEEResourceType.MSD)
                        && fragment.isConflictMailSessionDefinition()) {
                        throw new IllegalArgumentException("There are more than one mail-session definitions"
                            + " defined in web fragments with the same name, but not overrided in web.xml");
                    } else if (javaEEResourceType.equals(JavaEEResourceType.DSD)
                        && fragment.isConflictDataSourceDefinition()) {
                        throw new IllegalArgumentException("There are more than one datasource definitions"
                            + " defined in web fragments with the same name, but not overrided in web.xml");
                    } else if (javaEEResourceType.equals(JavaEEResourceType.CFD)
                        && fragment.isConflictConnectionFactoryDefinition()) {
                        throw new IllegalArgumentException("There are more than one connection factory definitions"
                            + " defined in web fragments with the same name, but not overrided in web.xml");
                    } else if (javaEEResourceType.equals(JavaEEResourceType.JMSCFDD)
                        && fragment.isConflictJMSConnectionFactoryDefinition()) {
                        throw new IllegalArgumentException("There are more than one jms connection factory definitions"
                            + " defined in web fragments with the same name, but not overrided in web.xml");
                    } else if (javaEEResourceType.equals(JavaEEResourceType.JMSDD)
                        && fragment.isConflictJMSDestinationDefinition()) {
                        throw new IllegalArgumentException("There are more than one jms destination definitions"
                            + " defined in web fragments with the same name, but not overrided in web.xml");
                    }
                }
                if (desc.getResourceType().equals(JavaEEResourceType.DSD)
                    || desc.getResourceType().equals(JavaEEResourceType.MSD)
                    || desc.getResourceType().equals(JavaEEResourceType.CFD)
                    || desc.getResourceType().equals(JavaEEResourceType.AODD)
                    || desc.getResourceType().equals(JavaEEResourceType.JMSCFDD)
                    || desc.getResourceType().equals(JavaEEResourceType.JMSDD)) {
                    getResourceDescriptors(javaEEResourceType).add(desc);
                }
            }
        }
    }


    /**
     * This method will copy the injection targets from env2 to env1.
     *
     * @param env1
     * @param env2
     */
    protected void combineInjectionTargets(EnvironmentProperty env1, EnvironmentProperty env2) {
        for (InjectionTarget injTarget : env2.getInjectionTargets()) {
            env1.addInjectionTarget(injTarget);
        }
    }


    @Override
    public void addWebComponentDescriptor(WebComponentDescriptor webComponentDescriptor) {
        String servletName = webComponentDescriptor.getCanonicalName();
        webComponentDescriptor.setWebBundleDescriptor(this);
        WebComponentDescriptor resultDesc = combineWebComponentDescriptor(webComponentDescriptor);

        // sync up urlPattern2ServletName map
        for (String up : resultDesc.getUrlPatternsSet()) {
            String oldName = getUrlPatternToServletNameMap(true).put(up, servletName);
            if (oldName != null && !oldName.equals(servletName)) {
                throw new IllegalArgumentException(MessageFormat.format(
                    "Servlet [{0}] and Servlet [{1}] have the same url pattern: [{2}]", oldName, servletName, up));
            }
        }
    }


    /**
     * This method combines descriptor except urlPattern and add
     * to current bundle descriptor if necessary.
     * It returns the web component descriptor in the current bundle descriptor.
     *
     * @param webComponentDescriptor the new descriptor
     * @return web component descriptor in current bundle
     */
    protected WebComponentDescriptor combineWebComponentDescriptor(WebComponentDescriptor webComponentDescriptor) {
        String name = webComponentDescriptor.getCanonicalName();
        WebComponentDescriptor webCompDesc = getWebComponentByCanonicalName(name);

        final WebComponentDescriptor resultDesc;
        if (webCompDesc != null) {
            // Servlet defined in web.xml
            resultDesc = webCompDesc;
            if (!webCompDesc.isConflict(webComponentDescriptor, true)) {
                // combine the contents of the given one to this one
                // except the urlPatterns
                webCompDesc.add(webComponentDescriptor, false, false);
            }

            String implFile = webCompDesc.getWebComponentImplementation();
            if (resultDesc.isConflict() && (implFile == null || implFile.isEmpty())) {
                throw new IllegalArgumentException("Two or more web fragments define the same Servlet"
                    + " with conflicting implementation class names that are not overridden by the web.xml");
            }
            if (!resultDesc.getConflictedInitParameterNames().isEmpty()) {
                throw new IllegalArgumentException("Two or more web fragments define the same Servlet"
                    + " with conflicting init param that are not overridden by the web.xml");
            }
        } else {
            resultDesc = webComponentDescriptor;
            if (resultDesc.isConflict()) {
                throw new IllegalArgumentException("One or more web fragments define the same Servlet"
                    + " in a conflicting way, and the Servlet is not defined in web.xml");
            }
            super.addWebComponentDescriptor(resultDesc);
        }
        return resultDesc;
    }



    @Override
    public void print(StringBuffer toStringBuffer) {
        super.print(toStringBuffer);
        if (sunWebApp != null) {
            toStringBuffer.append("\n ========== Runtime Descriptors =========");
            toStringBuffer.append('\n').append(sunWebApp);
        }
    }

    public Enumeration<String> getArchiveFileEntries(ReadableArchive archiveFile) {
        return archiveFile.entries();
    }

    private static final class ServletFilterMappingInfo {

        private ServletFilterMapping servletFilterMapping;
        private boolean hasMapping;
        private boolean hasDispatcher;
    }
}
