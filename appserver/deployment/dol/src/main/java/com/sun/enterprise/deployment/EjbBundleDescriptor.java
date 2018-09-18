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

package com.sun.enterprise.deployment;

import java.util.Set;

import com.sun.enterprise.deployment.types.EjbReferenceContainer;
import com.sun.enterprise.deployment.types.MessageDestinationReferenceContainer;
import com.sun.enterprise.deployment.types.ResourceEnvReferenceContainer;
import com.sun.enterprise.deployment.types.ResourceReferenceContainer;
import com.sun.enterprise.deployment.types.ServiceReferenceContainer;

/**
 * I represent all the configurable deployment information contained in
 * an EJB JAR.
 *
 * @author Danny Coward
 */

public abstract class EjbBundleDescriptor extends CommonResourceBundleDescriptor
    implements WritableJndiNameEnvironment, EjbReferenceContainer,
               ResourceEnvReferenceContainer, ResourceReferenceContainer,
               ServiceReferenceContainer, MessageDestinationReferenceContainer {
 
    public abstract Set<EjbInterceptor> getInterceptors();
    public abstract EjbInterceptor getInterceptorByClassName(String className);
    public abstract EjbDescriptor getEjbByName(String name);
    public abstract boolean hasEjbByName(String name);
    public abstract Set<? extends EjbDescriptor> getEjbs();
    public abstract EjbDescriptor[] getEjbByClassName(String className);
    public abstract Set<ServiceReferenceDescriptor> getEjbServiceReferenceDescriptors();
    public abstract EjbDescriptor[] getEjbBySEIName(String className);
    public abstract Boolean getDisableNonportableJndiNames();

}
