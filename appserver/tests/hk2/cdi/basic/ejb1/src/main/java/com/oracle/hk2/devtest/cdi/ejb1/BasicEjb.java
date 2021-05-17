/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.oracle.hk2.devtest.cdi.ejb1;

import org.jvnet.hk2.annotations.Contract;

/**
 * This is a simple EJB interface that has methods used by the client
 * to ensure that each test has passed
 *
 * @author jwells
 *
 */
@Contract
public interface BasicEjb {
    /**
     * Returns true if the CDI manager was properly injected
     * into the EJB that implements this interface
     *
     * @return true if the CDI bean manager was injected into this EJB
     */
    public boolean cdiManagerInjected();

    /**
     * Returns true if an HK2 serviceLocator was properly injected
     * into the EJB that implements this interface
     * <p>
     * This demonstrates that HK2 services are being injected into
     * CDI created beans
     *
     * @return true if the EJB was injected with an HK2 service locator
     */
    public boolean serviceLocatorInjected();

    /**
     * This uses the HK2 ServiceLocator to install the
     * BasicService descriptor into the injected HK2
     * ServiceLocator
     */
    public void installHK2Service();

    /**
     * This method ensures that the HK2 service installed with
     * {@link #installHK2Service()} can be injected with
     * CDI bean instances
     * <p>
     * This demonstrates that services created with HK2 can be
     * injected with beans created with CDI
     *
     * @return true if the BasicService HK2 service was injected
     * with a CDI bean instance
     */
    public boolean hk2ServiceInjectedWithEjb();

    /**
     * Returns without throwing an error if all of the CDI extension
     * events had proper access to the ServiceLocator in JNDI
     *
     * @throws AssertionError if any of the extension points did not have
     * access to the ServiceLocator
     */
    public void isServiceLocatorAvailableInAllCDIExtensionEvents();

    /**
     * This method ensures that the CustomScopedEJB gets properly injected
     * with the HK2 service
     */
    public void isEJBWithCustomHK2ScopeProperlyInjected();

    /**
     * Tests that an implementation of PopulatorPostProcessor put into
     * META-INF/services runs properly
     */
    public void doesApplicationDefinedPopulatorPostProcessorRun();

    /**
     * Tests that a service added via an HK2 {@link JustInTimeResolver}
     * is properly added to the CDI bridge
     */
    public void isServiceAddedWithJITResolverAdded();

    /**
     * Checks that an ApplicationScoped CDI service can be injected into
     * an HK2 service
     */
    public void checkApplicationScopedServiceInjectedIntoHk2Service();


}
