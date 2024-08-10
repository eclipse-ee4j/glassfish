/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.deployment;

import com.sun.enterprise.deployment.web.InitializationParameter;
import com.sun.enterprise.deployment.web.MultipartConfig;
import com.sun.enterprise.deployment.web.SecurityRoleReference;

import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Set;

import org.glassfish.deployment.common.Descriptor;


/**
 * Common data and behavior of the deployment
 * information about a JSP or JavaServlet in J2EE.
 *
 * @author Jerome Dochez
 */
public abstract class WebComponentDescriptor extends Descriptor {

    private static final long serialVersionUID = 1L;

    public abstract Set<InitializationParameter> getInitializationParameterSet();

    public abstract Enumeration<InitializationParameter> getInitializationParameters();

    public abstract InitializationParameter getInitializationParameterByName(String name);

    public abstract void addInitializationParameter(InitializationParameter initializationParameter);

    public abstract void removeInitializationParameter(InitializationParameter initializationParameter);

    public abstract Set<String> getUrlPatternsSet();

    public abstract Enumeration<String> getUrlPatterns();

    public abstract void addUrlPattern(String urlPattern);

    public abstract void removeUrlPattern(String urlPattern);

    public abstract void setWebBundleDescriptor(WebBundleDescriptor webBundleDescriptor);

    public abstract WebBundleDescriptor getWebBundleDescriptor();

    public abstract String getCanonicalName();

    public abstract void setCanonicalName(String canonicalName);

    public abstract Integer getLoadOnStartUp();

    public abstract void setLoadOnStartUp(Integer loadOnStartUp);

    public abstract void setLoadOnStartUp(String loadOnStartUp) throws NumberFormatException;

    public abstract Set<SecurityRoleReference> getSecurityRoleReferenceSet();

    public abstract Enumeration<SecurityRoleReference> getSecurityRoleReferences();

    public abstract SecurityRoleReference getSecurityRoleReferenceByName(String roleReferenceName);

    public abstract void addSecurityRoleReference(SecurityRoleReference securityRoleReference);

    public abstract void removeSecurityRoleReference(SecurityRoleReference securityRoleReference);

    public abstract void setRunAsIdentity(RunAsIdentityDescriptor runAs);

    public abstract RunAsIdentityDescriptor getRunAsIdentity();

    public abstract boolean getUsesCallerIdentity();

    public abstract void setUsesCallerIdentity(boolean isCallerID);

    public abstract MultipartConfig getMultipartConfig();

    public abstract void setMultipartConfig(MultipartConfig multipartConfig);

    public abstract Application getApplication();

    public abstract void setWebComponentImplementation(String implFile);

    public abstract String getWebComponentImplementation();

    public abstract boolean isServlet();

    public abstract void setServlet(boolean isServlet);

    public abstract boolean isEnabled();

    public abstract void setEnabled(boolean enabled);

    public abstract void setAsyncSupported(Boolean asyncSupported);

    public abstract Boolean isAsyncSupported();

    public abstract void setConflict(boolean conflict);

    public abstract boolean isConflict();

    public abstract Method[] getUserDefinedHttpMethods();

    public abstract void add(WebComponentDescriptor other);

    public abstract void add(WebComponentDescriptor other, boolean combineUrlPatterns, boolean combineConflict);

    public abstract boolean isConflict(WebComponentDescriptor other, boolean allowNullImplNameOverride);

    public abstract Set<String> getConflictedInitParameterNames();
}
