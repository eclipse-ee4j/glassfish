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

package com.sun.enterprise.container.common.spi;

import org.jvnet.hk2.annotations.Contract;

import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.ManagedBeanDescriptor;

/**
 * ManagedBeanManager provides an interface to various Java EE Managed Bean
 * component operations.
 */

@Contract
public interface ManagedBeanManager {

    public void loadManagedBeans(Application app);

    public void unloadManagedBeans(Application app);

    public Object getManagedBean(String globalJndiName) throws Exception;

    public <T> T createManagedBean(Class<T> managedBean) throws Exception;

    public <T> T createManagedBean(Class<T> managedBean, boolean invokePostConstruct) throws Exception;

    public <T> T createManagedBean(ManagedBeanDescriptor managedBeanDesc, Class<T> managedBeanClass) throws Exception;

    public <T> T createManagedBean(ManagedBeanDescriptor managedBeanDesc, Class<T> managedBeanClass,
        boolean invokePostConstruct) throws Exception;

    public boolean isManagedBean(Object object);

    public void destroyManagedBean(Object managedBean);

    public void destroyManagedBean(Object managedBean, boolean validate);

    /**
     * Register an interceptor instance for all managed beans in the given module
     *
     * @param interceptorInstance
     * @param bundle BundleDescriptor (passed as object because we can't
     *        add a dependency on the DOL
     */
    public void registerRuntimeInterceptor(Object interceptorInstance, BundleDescriptor bundle);


}
